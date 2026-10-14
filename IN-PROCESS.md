# Running plugin tools in process - follow-up notes

Research notes, not a plan of record. Sources are cited inline; anything not checked against a source is
marked **unverified**. Engine references are to `sources/build/jenesis` of the build tool at the commit `build/.upstream`
pins; tool sources were read on their default branch or at the version the plugin's
`*RunTest` pins, as stated.

## Question

1. Should the plugins call the tools' APIs in the build's own JVM instead of starting their `main` methods
   in another JVM, as Maven and Gradle plugins mostly do?
2. Does Gradle keep daemons for plugin, worker and compiler JVMs, and how do they relate to the Gradle daemon
   and `--no-daemon`?

Short answer: in-process is a good fit for roughly half the plugins and needs an engine placement API first.
The more urgent finding is that the warm worker gives most `ofJavaMain` plugins no reuse at all today,
because their `main` methods always call `System.exit` (see *Today*). Gradle does not run Checkstyle or PMD
in process either; it runs them in worker processes that end with the build.

## Today

How a plugin's tool runs, from `ProcessHandler.java`, `ProcessBuildStep.java`, `JavaWorker.java`,
`daemon/Worker.java` and the plugin sources:

| Mode | Handler | Warm-eligible | Plugins |
|---|---|---|---|
| `main` in a JVM | `OfProcess.ofJavaMain()` | yes, under `jenesis.process.warm` | antlr, avro, checkstyle, codenarc, detekt, forbiddenapis, jacoco (report), japicmp, ksp, ktlint (check), openapi, pmd, revapi (launcher source), scalafmt (check), scalastyle, spotbugs, wsimport, xjc |
| `main` in a JVM, shared | `ofJavaMain().shared(true)` | yes | kotlin, incremental only: the plugin's own `IncrementalKotlinCompiler` drives the Kotlin Build Tools API (`createInProcessExecutionPolicy`) inside that worker |
| always forked | `OfProcess.ofJavaHome("bin/java")` | no | groovy (compile, groovydoc), kotlin (non-incremental, Dokka), scala (compile, incremental Zinc via the plugin's `IncrementalScalaCompiler`, scaladoc), pitest |
| always forked | `FormatBuildStep` (engine) uses `ofJavaHome` | no | eclipse, googlejavaformat, palantir, ktfmt, ktlint (format), scalafmt (format) |
| native binary | `OfProcess.ofStaged()` | no | protoc |
| build JVM | `URLClassLoader` on the platform loader, `ToolProvider`, context class loader set, `Permits` acquired | n/a | jreleaser |

Corrections to the background statement: the eclipse launcher is a format step and is therefore always
forked, never warm. "Parts of Kotlin run inside the build JVM" is not borne out by the sources: the
class-loader code in `IncrementalKotlinCompiler` runs inside the warm worker (or a fork), not the build JVM;
only JReleaser runs in the build JVM. The engine's own JDK tools (`javac`, `javadoc`, `jar`, `jlink`) run in
the build JVM through `ProcessHandler.OfTool` by default (`jenesis.process.factory=tool`).

Facts about the warm worker that matter here:

- `JavaWorker.call` declines a call whose JVM options include `--add-exports`, `--add-opens`, `-p` and the
  rest of its `TAKING` set, so such a call is forked even when warm is on. googlejavaformat and palantir pass
  `--add-exports jdk.compiler/...`, and are format steps besides.
- A worker runs one call at a time unless `shared`; output is routed per call through an
  `InheritableThreadLocal` behind `System.setOut`/`setErr` (`Worker.Routed`).
- A `main` that calls `System.exit` ends the worker. `JavaWorker.execute` then reads the exit code from the
  process and destroys it, so the next call starts a fresh JVM. Nothing intercepts the exit (Java 25 has no
  security manager).
- Idle workers end after ten minutes (`Worker.IDLE = 600_000`); the build daemon idles out after 10800 s
  (`daemon.idle` in the catalogue).

Which tool `main` methods exit on success, read from source:

| Exits on every run | Exits only on failure or findings | Returns normally |
|---|---|---|
| Checkstyle `Main` (`Runtime.getRuntime().exit(exitStatus)`), PMD `PmdCli` (`System.exit(mainWithoutExit(args))`), ANTLR `Tool.main` (`exit(0)` unless `-XdbgST`), Avro tools `Main`, JaCoCo CLI `Main`, detekt 1.23.7 `Main` (`exitProcess`), ktlint 1.5.0 (`exitKtLintProcess(0/1)`), xjc `Driver.main`, wsimport `WsImport.main`, scalastyle `Main`, scalafmt 3.8.3 `Cli.main`, google-java-format and palantir `Main`, ktfmt 0.64 `Main` | forbiddenapis `CliMain`, CodeNarc (`systemExit(1)` on error), OpenAPI generator, japicmp `JApiCmp`, eclipse launcher (verify failures) | revapi launcher, Kotlin's `IncrementalKotlinCompiler` |

SpotBugs: `FindBugs.java` has three `System.exit` calls on its run path; whether success exits is
**unverified**. KSP's `KSPJvmMain`: **unverified**.

Consequence: under the daemon, Checkstyle, PMD, ANTLR, Avro, JaCoCo, detekt, ktlint, xjc, wsimport, scalastyle
and scalafmt start a new worker for every call and get no warm reuse. Fixing that does not need in-process
execution: a small launcher per plugin that calls the tool's non-exiting entry (the eclipse/revapi pattern)
makes the warm worker effective, and the same launcher is what an in-process placement would load.

Plugins receive `build.jenesis.Permits` (`acquire()`/`release()`, a `Semaphore` behind `Permits.of(n)`),
resolved from `jenesis.process.concurrency` (default: available processors, `0` = unlimited) and shared per
count; `ProcessBuildStep.execute` holds one permit per tool call. JReleaser acquires one around its in-process
call, which is the precedent an in-process placement should follow.

Plugins themselves load in a `ModuleLayer` defined with one loader parented on the platform class loader
(`JenesisClassLoaderBridge`, `defineModulesWithOneLoader(..., ClassLoader.getPlatformClassLoader())`).

## How Maven and Gradle do it

### Maven

| Claim | Verdict | Source |
|---|---|---|
| Plugins run in a per-plugin class realm; the context class loader is set to it while the plugin runs | Verified ("Each plugin ... has its own classloader that imports the Project classloader"; "the thread's context classloader is set to the plugin classloader"). The guide does not say "in the Maven JVM" in words; that follows from mojos being invoked as Java calls | [Maven class loading guide](https://maven.apache.org/guides/mini/guide-maven-classloading.html) |
| Checkstyle plugin calls the API | Verified: `new Checker()`, `setModuleClassLoader(TCCL)`, `addListener` | [DefaultCheckstyleExecutor](https://github.com/apache/maven-checkstyle-plugin/blob/master/src/main/java/org/apache/maven/plugins/checkstyle/exec/DefaultCheckstyleExecutor.java) |
| PMD plugin calls the API | Verified, with a correction: `PmdAnalysis.create(...)` in process, but it **forks** a JVM when a JDK toolchain is configured (`PmdServiceExecutor` calls `executor.fork(javaExecutable)`) | [PmdExecutor](https://github.com/apache/maven-pmd-plugin/blob/master/src/main/java/org/apache/maven/plugins/pmd/exec/PmdExecutor.java), [PmdServiceExecutor](https://github.com/apache/maven-pmd-plugin/blob/master/src/main/java/org/apache/maven/plugins/pmd/exec/PmdServiceExecutor.java) |
| JaCoCo report in process | Verified: `ExecFileLoader`, `Analyzer`, `CoverageBuilder` | [ReportSupport](https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportSupport.java) |
| ANTLR in process | Verified: `CustomTool extends Tool`, `addListener(...)`, `processGrammarsOnCommandLine()`, output writers overridden | [Antlr4Mojo](https://github.com/antlr/antlr4/blob/dev/antlr4-maven-plugin/src/main/java/org/antlr/mojo/antlr4/Antlr4Mojo.java) |
| Avro in process | Verified: `new SpecificCompiler(schemas)` | [AbstractAvroMojo](https://github.com/apache/avro/blob/main/lang/java/maven-plugin/src/main/java/org/apache/avro/mojo/AbstractAvroMojo.java) |
| OpenAPI generator in process | Verified: `CodegenConfigurator`, `new DefaultGenerator(dryRun).opts(input).generate()` | [CodeGenMojo](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-maven-plugin/src/main/java/org/openapitools/codegen/plugin/CodeGenMojo.java) |
| japicmp in process | Verified: `JarArchiveComparator` | [JApiCmpProcessor](https://github.com/siom79/japicmp/blob/master/japicmp-maven-plugin/src/main/java/japicmp/maven/JApiCmpProcessor.java) |
| revapi in process | Verified: `org.revapi.Revapi`, `AnalysisContext` | [revapi-maven-plugin Analyzer](https://github.com/revapi/revapi/blob/main/revapi-maven-plugin/src/main/java/org/revapi/maven/Analyzer.java) |
| Surefire forks | Verified: `forkCount` defaults to `1` | [AbstractSurefireMojo](https://github.com/apache/maven-surefire/blob/master/maven-surefire-common/src/main/java/org/apache/maven/plugin/surefire/AbstractSurefireMojo.java) |
| SpotBugs plugin forks by default | Correction: on its current default branch it **always** forks (`java -Xmx${maxHeap}m ... edu.umd.cs.findbugs.FindBugs2`, `maxHeap` default 512); there is no in-process option there | [SpotBugsMojo.groovy](https://github.com/spotbugs/spotbugs-maven-plugin/blob/master/src/main/groovy/org/codehaus/mojo/spotbugs/SpotBugsMojo.groovy) |

The Maven daemon is the closest analogue to Jenesis under its daemon: "The classloaders holding classes of
Maven plugins are cached over multiple builds", JIT output is kept, SNAPSHOT plugins are not cached
([apache/maven-mvnd README](https://github.com/apache/maven-mvnd)).

### Gradle

Gradle source read at `gradle/gradle` master `dedc5d14` (2026-10-14); older behaviour from release tags.

| Claim | Verdict | Source |
|---|---|---|
| Worker API isolation modes `noIsolation`, `classLoaderIsolation`, `processIsolation` | Verified ("in a thread with minimal isolation", "in a thread with an isolated classloader", "executing the work in a separate process") | [Worker API](https://docs.gradle.org/current/userguide/worker_api.html) |
| Checkstyle and PMD tasks run in worker processes, hence `maxHeapSize` | Verified: `Checkstyle`, `Pmd` and `CodeNarc` use `getWorkerExecutor().processIsolation(...)`; `AbstractCodeQualityTask` copies `maxHeapSize` into the fork options. Inside the worker they drive the tools' **Ant tasks** (`CheckstyleAntTask`, `org.codenarc.ant.CodeNarcTask`), not `Checker` directly | `platforms/jvm/code-quality/.../Checkstyle.java`, `Pmd.java`, `CodeNarc.java`, `AbstractCodeQualityTask.java`; `platforms/jvm/code-quality-workers/.../CheckstyleInvoker.java` |
| JaCoCo report | Not in the previous analysis: `JacocoReport` and `JacocoCoverageVerification` use `classLoaderIsolation()`, i.e. **in the Gradle daemon's JVM**, through the JaCoCo Ant task | `platforms/jvm/jacoco/.../JacocoReport.java`, `platforms/jvm/jacoco-workers/.../AntJacocoReport.java` |
| ANTLR | Not in the previous analysis: a dedicated worker process per task (`multiRequestWorker(AntlrExecuter.class)`, stopped after the task), calling `org.antlr.v4.Tool` reflectively | `platforms/jvm/antlr/.../AntlrTask.java`, `AntlrExecuter.java` |
| Kotlin uses its own daemon | Verified: `kotlin.compiler.execution.strategy` defaults to `daemon`; KGP uses the Build Tools API by default for Kotlin/JVM | [Compiler execution strategy](https://kotlinlang.org/docs/compiler-execution-strategy.html), [Build Tools API](https://kotlinlang.org/docs/build-tools-api.html) |
| Scala uses Zinc | Verified: `DaemonScalaCompiler` in a compiler daemon with a `zincClasspath` | `platforms/jvm/scala/.../ScalaBasePlugin.java`, `DaemonScalaCompiler.java` |
| Dokka's Gradle plugin runs in a separate process by default | Verified: `dokkaGeneratorIsolation.convention(dokkaExtension.ProcessIsolation())`; `ClassLoaderIsolation()` runs it in the Gradle daemon | [DokkaBasePlugin.kt](https://github.com/Kotlin/dokka/blob/master/dokka-runners/dokka-gradle-plugin/src/main/kotlin/DokkaBasePlugin.kt), [DokkaExtension.kt](https://github.com/Kotlin/dokka/blob/master/dokka-runners/dokka-gradle-plugin/src/main/kotlin/DokkaExtension.kt) |
| javac | In process when the toolchain is the daemon's own JVM, otherwise a forked compiler daemon ("Compilation mode: in-process compilation" / "default, forking compiler") | `platforms/jvm/language-java/.../AbstractJavaCompileSpecFactory.java` |

### Gradle: which JVMs outlive a build

Every worker and compiler daemon has a `KeepAliveMode`: `SESSION` ("Keep alive until the end of the build
session") or `DAEMON` ("Keep alive until the daemon stops")
(`platforms/core-execution/workers/.../KeepAliveMode.java`). `WorkerDaemonClientsManager` stops `SESSION`
clients in a `BuildSessionLifecycleListener.beforeComplete()`, stops idle workers under memory pressure, and,
being a `Stoppable` in the user-home service scope, stops the rest when the Gradle process shuts that scope.

| JVM | Keep-alive today | Since | Lives across builds? |
|---|---|---|---|
| Worker API `processIsolation` (Checkstyle, PMD, CodeNarc, Dokka by default, third-party tasks) | `SESSION` (`DefaultWorkerExecutor.getWorkerRequirement`) | 8.0; 7.6 had `DAEMON` | **No** - reused within one build only |
| javac compiler daemon | `DAEMON` (`DaemonJavaCompiler`) | 8.3 on Linux and macOS, 8.4 on Windows ([8.4 notes](https://docs.gradle.org/8.4/release-notes.html), [8.3 notes](https://docs.gradle.org/8.3/release-notes.html)) | Yes, while the Gradle daemon lives |
| Groovy compiler daemon | `SESSION` (`DaemonGroovyCompiler`) | - | No |
| Scala (Zinc) compiler daemon | `SESSION` by convention, settable to `DAEMON` (`ScalaBasePlugin`, `org.gradle.language.scala.tasks.KeepAliveMode`) | - | Only if set to `DAEMON` |
| Test workers | started per test task and stopped at its end (`ForkingTestDefinitionProcessor.stop()`); `forkEvery` recycles within a task | - | No |
| ANTLR worker | per task, `client.stop()` afterwards | - | No |
| Kotlin daemon | its own process, shareable "between different build system processes and multiple parallel compilations"; "stops either with the Gradle daemon or after two idle hours"; `shutdownDelayMilliseconds` 1 s after all clients disconnect | - | Yes, while a Gradle daemon is connected |

Sources for the Kotlin row: [Kotlin compilation and caches](https://kotlinlang.org/docs/gradle-compilation-and-caches.html),
[Kotlin daemon](https://kotlinlang.org/docs/kotlin-daemon.html),
[Compiler execution strategy](https://kotlinlang.org/docs/compiler-execution-strategy.html). Current KGP API docs
list only `DAEMON` and `IN_PROCESS`
([KotlinCompilerExecutionStrategy](https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.tasks/-kotlin-compiler-execution-strategy/));
the 2.3.0 docs still listed `OUT_OF_PROCESS` ("a new forked process for each compilation", without incremental
compilation). Whether it was removed or only undocumented: **unverified**. With the Build Tools API,
`in-process` "now supports incremental compilation".

The Gradle user guide is inconsistent: its Worker API page says both "Worker daemons will remain running until
the build daemon that started them is stopped" and that they "live only for the duration of a single build"
([Worker API](https://docs.gradle.org/current/userguide/worker_api.html)). The code settles it as above.

`--no-daemon`: the build runs in the client JVM when its JVM arguments match `org.gradle.jvmargs`, otherwise
in a single-use daemon "created, used, and then stopped at the end of the build"
([Gradle daemon](https://docs.gradle.org/current/userguide/gradle_daemon.html)). Either way the Gradle process
ends with the build, so every worker and compiler daemon, `DAEMON`-mode ones included, ends with it (inferred
from the manager's scope and `stop()`; the docs do not say it). The Kotlin daemon then shuts down about 1 s
after its last client disconnects unless another client is connected (inferred from the documented defaults).
The Gradle daemon idles out after 3 hours, the same as Jenesis's `daemon.idle` default.

So Gradle's answer to "warm tools without a daemon" is: no tool JVM survives a `--no-daemon` build, and even
with the daemon only javac (and Kotlin's own daemon, and Scala if asked) survive between builds. Jenesis's
current split - warm only under the daemon - is the same policy, and keeping workers for the next build under
the daemon goes further than Gradle does for Checkstyle or PMD.

## Plugin by plugin

Version is the one the plugin's `*RunTest` pins; the plugins themselves resolve `RELEASE` until a project pins.
"Main exits" is from the table above. Recommendation: **build JVM** (in process, isolated class loader),
**worker** (API call through a launcher, in a warm worker), **fork** (keep as is).

| Plugin | Tool (test pin) | Runs today | In-process entry point | Obstacles | Recommendation |
|---|---|---|---|---|---|
| checkstyle | Checkstyle 10.21.0 | `ofJavaMain`, `Main` exits always | `Checker` + `AuditListener` (`XMLLogger` for the report), `ConfigurationLoader` | `setModuleClassLoader`; property expansion from our map, not `System.getProperties()` | build JVM |
| pmd | PMD 7.7.0 | `ofJavaMain`, `PmdCli` exits always | `PmdAnalysis.create(PMDConfiguration)`, `performAnalysisAndCollectReport()` | reporter/renderer API exists; `mainWithoutExit` is package-private | build JVM |
| forbiddenapis | 3.11 | `ofJavaMain` | `de.thetaphi.forbiddenapis.Checker(Logger, ClassLoader, Option...)`, `parseSignaturesFile`, `addClassesToCheck`, `run` | needs a loader over the checked class path | build JVM |
| japicmp | 0.26.2 | `ofJavaMain` with forked JVM options | `JarArchiveComparator` + output generators | japicmp's own `ForkedOptions` (heap) lose meaning in process | build JVM, fork when JVM options are set |
| revapi | 0.15.1 | launcher source, `ofJavaMain` | `org.revapi.Revapi` (launcher already uses it) | extension discovery via `ServiceLoader` on the loader | build JVM |
| jacoco (report) | 0.8.14 | `ofJavaMain`, CLI `Main` exits always | `ExecFileLoader`, `Analyzer`, `CoverageBuilder`, `XMLFormatter`/`HTMLFormatter` | none known; agent side stays in the test JVM | build JVM |
| antlr | 4.13.2 | `ofJavaMain`, `Tool.main` exits always | `new Tool(args)`, `addListener(ANTLRToolListener)`, `processGrammarsOnCommandLine()`; override `exit` | `Tool.exit` calls `System.exit`; error output via listener | build JVM |
| avro | 1.12.2 (avro-tools) | `ofJavaMain`, `Main` exits always | `org.apache.avro.compiler.specific.SpecificCompiler` | would resolve `avro-compiler` rather than the avro-tools CLI bundle | build JVM |
| openapi | 7.25.0 | `ofJavaMain` | `CodegenConfigurator` + `DefaultGenerator.opts(...).generate()` | large closure (Swagger parser, templating), memory per run **unverified** | build JVM, measure |
| codenarc | CodeNarc 3.5.0 (Groovy 3.0.9) | `ofJavaMain` | `org.codenarc.CodeNarcRunner.execute()` | brings a Groovy runtime; Groovy metaclass caches per loader | worker, build JVM after measuring |
| eclipse | JDT core 3.47.0 | format step, forked, launcher source | `ToolFactory.createCodeFormatter(options, M_FORMAT_EXISTING)` (launcher already uses it) | JDT core outside OSGi; statics in `JavaCore` **unverified** | build JVM |
| ktfmt | 0.64 | format step, forked | `com.facebook.ktfmt.format.Formatter.format(FormattingOptions, String)` | embeds `kotlin-compiler-embeddable` 2.3.20 (PSI, IntelliJ core statics) | worker |
| scalafmt | 3.8.3 | check `ofJavaMain`, format forked; `Cli.main` exits always | `org.scalafmt.interfaces.Scalafmt.create(ClassLoader)` (`scalafmt-dynamic` resolves the core per configured version through its own Maven download unless configured otherwise - **unverified** whether it can be kept off the network), or `Cli.mainWithOptions` | dynamic module downloads conflict with Jenesis resolution and pinning | worker via `mainWithOptions` |
| scalastyle | 1.5.1 | `ofJavaMain`, `Main` exits always | `new ScalastyleChecker(cl).checkFiles(configuration, files)` (Scala API) | Scala API from Java is awkward; small tool | worker via launcher |
| googlejavaformat | 1.35.0 | format step, forked, `--add-exports` | `Formatter.formatSource(String)`, or its `GoogleJavaFormatToolProvider` | needs `jdk.compiler` internals exported; a boot-layer module's exports cannot be widened at run time without command-line options or an agent | fork, or a worker started with the exports (needs `JavaWorker` to accept them) |
| palantir | 2.91.0 | format step, forked, `--add-exports` | `com.palantir.javaformat.java.Formatter` | as googlejavaformat | as googlejavaformat |
| ktlint | 1.5.0 (Kotlin 2.1.0) | check `ofJavaMain`, format forked; exits always | `KtLintRuleEngine(ruleProviders, editorConfigOverride).lint/format` | rule providers via `ServiceLoader`; embedded Kotlin compiler | worker |
| detekt | 1.23.7 (Kotlin 2.0.10) | `ofJavaMain`, exits always | `DetektProvider.load().get(ProcessingSpec).run()` (detekt-tooling) | embedded Kotlin compiler, heap | worker |
| xjc | JAXB 4.0.9 | `ofJavaMain`, `Driver.main` exits always | `com.sun.tools.xjc.Driver.run(String[], PrintStream, PrintStream)` or `run(args, XJCListener)` | JAXB statics and context class loader **unverified** in detail | worker via `Driver.run` |
| wsimport | JAX-WS 4.0.5 | `ofJavaMain`, exits always | `new WsimportTool(OutputStream).run(String[])` | compiles generated sources itself; statics **unverified** | worker via `WsimportTool` |
| ksp | 2.3.12 | `ofJavaMain` | `com.google.devtools.ksp.impl.KotlinSymbolProcessing(config, processors, logger).execute()` | Kotlin Analysis API, heavy, processors from the project | worker |
| kotlin | Kotlin 2.3.21 | incremental: shared warm worker with BTA in process there; otherwise forked | BTA `KotlinToolchains`, `createInProcessExecutionPolicy` (already used) | memory; the shared worker already isolates it | keep (worker) |
| groovy | Groovy 6.0.0 | forked (`ofJavaHome`) | `CompilationUnit`, `GroovyDocTool` | Groovy global state; never warm today | worker (make it warm-eligible first) |
| scala | Scala 3.10.0, Zinc 1.12.1 | forked (`ofJavaHome`), Zinc driven by the plugin's own class | Zinc `IncrementalCompiler` (already used), `dotty.tools.dotc.Main` | `IncrementalScalaCompiler` calls `System.exit(1)` on `CompileFailed`; never warm today | worker (shared, like Kotlin) |
| kotlin (Dokka) | Dokka 2.2.0 | forked (`ofJavaHome`) | `DokkaGenerator(configuration, logger).generate()` | heavy memory; Gradle's own default is a separate process | fork |
| spotbugs | 4.9.6 | `ofJavaMain` | `FindBugs2` | `DetectorFactoryCollection.theInstance` static, heap; Maven and Gradle plugins fork it | fork |
| pitest | `RELEASE` | forked (`ofJavaHome`) | `EntryPoint` (**unverified**) | starts its own minion JVMs anyway | fork |
| protoc | native | `ofStaged` | - | native binary | fork |
| jreleaser | `RELEASE` | build JVM, `ToolProvider` | already in process | - | keep |

Notes:

- The "build JVM" candidates have listener or writer APIs, so nothing they report needs `System.out`. A tool
  that only prints should stay out of the build JVM, because the engine never swaps the JVM's streams (and
  `MakeTool` runs inside another program's JVM, where it must not).
- Every plugin already reads its results from files the tool writes (reports, generated sources), so moving
  the call does not change how results cross.
- A launcher per plugin that calls the API and returns an exit code without `System.exit` is useful whatever
  the placement: it fixes warm reuse today, and is the code the build JVM would load later.

## Engine design sketch

Consistent with the build tool's conventions (immutable records with withers, settings read in
`ofEnvironment`, fail loudly, no third-party dependency in the engine):

1. **A task, not a `main`.** A plugin ships a task source (as `EclipseFormatter.source` and
   `RevapiCheck.source` do) implementing a JDK-only contract, for example a `ToolProvider`
   (`run(PrintWriter out, PrintWriter err, String... args)`), which is in `java.base` and already used for
   JReleaser and the engine's JDK tools. The engine compiles it once against the resolved tool jars with the
   in-process `javac`, keyed by the source digest and the jars' identities, as `Worker.invoke` already does for
   `.java` launchers.
2. **A loader per tool closure.** A `URLClassLoader` over the tool jars plus the compiled task, parented on
   the platform loader, so neither the engine nor the plugin layer leaks in. Cached for the life of the JVM
   under the same key `JavaWorker` uses (java, options, jar identities); context class loader set for the
   call, as JReleaser does.
3. **Results cross as files and strings.** Arguments are strings, output goes to the two writers the step
   already tees (`ProcessHandler.Tee`), results are files in `context.next()`. No object of the tool's crosses
   the loader boundary.
4. **One engine call decides placement.** A third `ProcessHandler` (say `OfTask`) carries the task, jars and
   arguments and declares the isolation it tolerates, mirroring Gradle:
   - `classloader` - may run in the build JVM;
   - `worker` - must run in a JVM of its own, which may be warm;
   - `process` - must run in a fresh JVM.
   The engine maps that to a placement: `classloader` runs in the build JVM; `worker` runs in the warm worker
   when `jenesis.process.warm` is on and forks otherwise; `process` always forks. A task with JVM options
   (heap, `--add-exports`) is never placed in the build JVM. Whether a user may force a stronger isolation
   (for memory, or to work around a misbehaving tool) is an open question below; if added it is one
   `jenesis.process.*` property, in the `configuration` catalogue and the documentation in the same commit.
5. **Interaction with `jenesis.process.warm`.** In-process placement leaves no JVM running, so it is
   compatible with the default of no warm workers outside the daemon, and it gives a build without the daemon
   reuse within the build (one class loading and JIT warm-up for all modules), which it lacks today. Under the
   daemon the cached loaders persist across builds, as mvnd caches plugin realms. The worker path stays for
   `worker`-isolation tools.
6. **Permits.** `ProcessBuildStep.execute` already wraps the handler in `permits.acquire()`/`release()`, so an
   in-process task holds a permit like a process does; nothing new is needed.
7. **Lifetime.** Without the daemon, the build JVM ends with the build and nothing leaks beyond it. Under the
   daemon a cached loader keeps its metaspace and any threads the tool started; the cache needs an idle
   eviction (closing the loader) comparable to the worker's ten minutes.

## Risks

- **Output.** `System.out`/`System.err` are JVM-global; parallel in-process steps would interleave and the
  engine would have to swap the JVM's streams, which it never does. Only tools with writer, listener or
  reporter APIs qualify for the build JVM.
- **`System.exit`.** Most CLIs exit (table above). The in-process path calls the API, never `main`; a tool
  that exits from inside its API would end the build or the daemon.
- **Global state.** System properties, `java.util.logging` configuration, JAXB and Groovy caches, the context
  class loader, static singletons (SpotBugs). A tool that writes a system property changes it for every
  other step and, under the daemon, for later builds.
- **JVM options.** Heap limits, `--add-exports`/`--add-opens`, `-XX` flags cannot be set per call; the build
  JVM's heap is whatever launched it.
- **Memory and leaks.** Embedded Kotlin compilers, the Analysis API, Dokka and SpotBugs are large. Metaspace
  and threads that a tool leaves behind accumulate in a long-lived daemon; a crash or `OutOfMemoryError` in
  process takes the build down with it instead of failing one step.
- **Cache key.** The step's serialised form is its cache key; the placement must not enter it, or switching
  placement would re-run steps (placement is not an input of the result).
- **Tool runs in other JVMs.** `jenesis-make` as a `ToolProvider` runs inside another program; in-process
  tools would then load in that program's JVM. The platform-parented loader isolates classes but not the
  global state above.

## Measurement plan

Goal: decide whether in-process placement is worth an engine API, and fix the warm path either way.

1. **Project.** A mid-size multi-module Java project, around 20 modules and 1,500-3,000 source files, with
   Checkstyle and PMD configured; repeat with ANTLR and JaCoCo report for a generator and a report tool. The
   build tool's own sources are a candidate.
2. **Placements.** For Checkstyle and PMD: (a) fork, warm off (today's default); (b) warm worker with today's
   exiting `main`; (c) warm worker with a non-exiting launcher; (d) in process, loader cached per build.
3. **Modes.** Each placement without the daemon (cold build JVM) and with the daemon (second and tenth build
   in the same daemon), `jenesis.process.concurrency` at the default and at 1.
4. **Measure.** Wall time of the selector narrowed to the check steps (`-Djenesis.test.skip=true`, forced
   re-run of the steps, cache off), total CPU time, peak RSS of the build JVM and of all children, metaspace
   of the daemon after ten builds, number of JVMs started (count `java` processes, or log handler starts).
5. **Repetitions.** At least five runs per cell after one warm-up, median and spread reported; same machine,
   idle, fixed CPU governor.
6. **Expectation to check, not assume.** (b) should be no faster than (a), because the worker dies after every
   call; (c) should close most of the gap to (d) under the daemon; (d) should be the only placement that
   helps a build without the daemon.

## Open questions

- Should a non-exiting launcher per plugin (warm path fix) land first, independent of in-process placement?
- Should users be able to force a stronger isolation per tool (Dokka's `dokkaGeneratorIsolation` is the
  Gradle precedent), or should the plugin's declaration be final?
- Should format steps (`FormatBuildStep`, always `ofJavaHome`) become warm-eligible, and should `JavaWorker`
  accept `--add-exports` as part of the worker key so googlejavaformat and palantir can be warm?
- Groovy, Scala and Dokka are never warm today; is that deliberate (memory, `System.exit` in
  `IncrementalScalaCompiler`)?
- How long should a cached in-process loader live under the daemon, and should the daemon cap metaspace?
- SpotBugs success-path exit, KSP's `KSPJvmMain` exit, PIT's in-process entry, xjc/wsimport statics and
  scalafmt-dynamic's network behaviour: **unverified**, to be checked before any of them moves.
