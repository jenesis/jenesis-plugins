# AGENTS.md

The plugins of the Jenesis build tool that run a tool the build has to download anyway: one folder per plugin
under `plugins/`, each a module that provides a `build.jenesis.BuildModuleFactory`. `README.md` covers the
plugins, the layout and releasing. The build tool's own `AGENTS.md` (under `build/.upstream`) states how its code is written, and every
rule there applies here as well; this file adds what is particular to the plugins.

## Build & test

- **JDK 25 or newer.** The build tool is the `build/.upstream` git submodule (`build/jenesis` links into it):
  `git submodule update --init --depth 1` once, then `java build/jenesis/Make.java` builds and tests every plugin.
- One test class: `java -Djenesis.test.filter='.*PmdModuleTest' -Djenesis.test.force=true
  -Djenesis.print.tests=true build/jenesis/Make.java`. Classes named `*RunTest` run the real tool and need the
  network.
- CI builds under strict pinning; after changing a dependency, run `java build/jenesis/Make.java pin` and commit
  the rewritten pins.

## How a plugin is written

- A plugin is `plugins/<tool>/sources/module-info.java`, module `build.jenesis.plugin.<tool>`, which `requires
  build.jenesis`, exports its package and `provides build.jenesis.BuildModuleFactory`. Its tests are
  `plugins/<tool>/tests/`, the `@jenesis.test build.jenesis.plugin.<tool>` module.
- A plugin that observes the tests, as JaCoCo does, provides a `build.jenesis.ObservabilityFactory` instead and is
  wired at the hook point `observe`: its `Observation` pairs the `ObservabilityEngine` that joins the tests' JVM, named
  after the group it was handed so that the agent it names resolves in `<group>:agent`, with the module that reports
  over what it recorded.
- The module is the build tool's module of the same tool, moved: the same steps, the same folder conventions,
  the same settings. Its `ofEnvironment` reads every setting, its pinning included, from the `Environment` it
  is given, and every setting is a wither of its own.
- The factory is the one entry point. It builds the module with `ofEnvironment` from what it is handed: the
  permits it was handed (`permits(permits)`), so the tool runs within the loading build's limit of concurrent
  processes, and the tool group of the group it was handed (`tool(group + ":tool")`), so the tool is resolved and
  pinned where the loading build says. It resolves the tool with the repositories and resolvers it was handed, never
  with its own.
- The group a plugin is handed is its name, which its own closure is pinned in; everything the plugin resolves is
  resolved in a group `<group>:<purpose>` - its tool in `<group>:tool`, and what must not negotiate one version with
  it in a group of its own, `kotlin:dokka`, `scala:zinc`, `protoc:grpc-java` - so its pins sort with the plugin's and
  no name of a group can be taken by another plugin. A hyphen never joins them, as a plugin may be named
  `kotlin-dokka` itself. The loading build fails a step of the plugin whose resolution names any other group. What a
  project names for the plugin to load beside its tool, as Checkstyle's custom checks or Kotlin's compiler plugins, is
  `@jenesis.plugin <group>:tool`, and KSP's processors `@jenesis.plugin ksp:processor`. A `.properties` file is a
  key with a `:` escaped, `kotlin\:dokka/maven/…`, as `pin` writes it, so a test that writes its pins by hand
  escapes it too.
- A plugin's values are the lines of whichever file the build reads them from - the tool's own `antlr.properties`,
  the `checkstyle.properties` beside a `checkstyle.xml` - and the factory validates them itself, because the build
  checks none: it refuses a value it does not take with an `IllegalArgumentException` that names it and what would
  be valid, and requires every value it needs; a plugin configured by settings alone takes no values. A value
  arrives resolved, and never as a path: a file the plugin reads is one the build binds - the tool's own file and
  what it names beside it, or what a line binds as `@<input>[/<target>]=<path>` - which the plugin finds among its
  arguments by the name its tool gives it (`pmd.xml`, `checkstyle.xml`, `revapi.json`), so it reads nothing the
  build did not bind.
- The plugin compiles against a published `build.jenesis`, pinned in its descriptor, never against the
  vendored engine that runs the build: a plugin needs a release of the tool that carries the API it uses.
- A tool runs through `ProcessHandler.OfProcess.ofJavaMain()`, so the build keeps its JVM warm. A tool whose main
  method calls `System.exit` on a call the build accepts - on every call, or on findings it only reports - is run
  through a driver instead: a class `<Tool>Driver` of the plugin with a `public static int main(String[])` that calls
  the entry point the tool offers short of its main method and returns the code. The driver is typed against the
  tool's API by `requires static` where that entry point is public, so the plugin's own closure does not carry the
  tool, and reaches it by reflection where it is private or has a shape Java cannot name. The step hands the folder
  `ProcessHandler.OfProcess.driver(<Tool>Driver.class)` writes ahead of the tool's jars, which holds the driver's own
  class files alone, so the driver touches nothing of the plugin beside it.
- A step that keeps state for its own next run - the caches of Kotlin's incremental compiler, Zinc's analysis, KSP's
  caches - keeps it in `context.persistent()` and never in its output, which every dependent would be handed. It
  starts from an empty folder whenever the build hands no previous output, clears it when the configuration it
  recorded there no longer matches, and writes the same output from an empty folder as from a full one.

- Every plugin folder carries a short `README.md`: one sentence on what it does, the file that activates it with an
  example, a table of the values it takes, the file it reads, and the setting that switches it off. The root
  `README.md` lists every plugin with that sentence and a link to its folder; a new plugin is added to both.

## Tests

- `it/` per plugin builds an example project through `ExampleProject` of `harness/`, the module
  `build.jenesis.plugin.harness` released beside the plugins so that a plugin of anyone's is tested the same way: it installs
  the plugin and `build.jenesis` into a local module repository, writes the project's files - a
  `jenesis.plugins.properties` naming the plugin, its `plugin-<name>.properties`, the sources - and runs
  `jenesis-make` in process with a selector that narrows the build to the plugin's hook point where that suffices. It
  proves that the published plugin loads and runs, not what the tool finds, which `tests/` covers.
- CI builds each plugin in a job of its own, `+plugins+<tool>+sources`, `+plugins+<tool>+tests` and
  `+plugins+<tool>+it`, so a new folder under `plugins/` joins the matrix by itself.
- `tests/` per plugin, on JUnit Jupiter with AssertJ. A test method is a sentence in `snake_case` stating the
  behaviour it proves; a test of the factory proves what the plugin does with each thing it is handed.
- A `*RunTest` pins the tool's closure in a `versions.properties` it writes, and resolves under strict pinning.

## Releasing and the build tool

A release is a manual run of the release workflow from the Actions tab, so any commit is releasable; it releases
every plugin and the harness, or the one its `plugin` input names, and nothing in `jreleaser.yml` names a plugin,
so a new folder under `plugins/` is released without a change there. The build tool pin is moved by checking out the new commit in
`build/.upstream`, building, and committing the submodule pointer; the plugins' `build.jenesis` pin is moved by
`pin` once the release they need is published.
