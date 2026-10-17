# Jenesis Plugins

![build](https://github.com/jenesis/jenesis-plugins/actions/workflows/build.yml/badge.svg)

> ### [Jenesis](https://jenesis.build) - a modern Java build tool
> _Java-native config, everything the JDK provides built in, with `module-info.java` treated as a feature, not an afterthought._

**The plugins of the Jenesis build tool that run a tool it has to download anyway.** Each plugin is a Java module
that provides a `build.jenesis.BuildModuleFactory`, an `ObservabilityFactory` for one that observes the tests, or a
`LanguageFactory` for one that compiles a language: the build that discovers it hands it its settings, the resolved
values of the file that switched it on, its process permits, its name as the group of its own closure, and its
repositories and resolvers, and the plugin adds the steps that resolve the tool and run it. What the JDK ships stays built into the build tool; what
needs a download lives here, so the tool itself stays small and nothing is fetched that a project does not use.

📖 **The user documentation lives at [jenesis.build](https://jenesis.build).** What follows is for people
working *on* this repository.

## The plugins

Each folder's README names the file that switches the plugin on, the values it takes, the files it reads and the
setting that switches it off.

| Plugin | What it does |
|---|---|
| [`antlr`](plugins/antlr/) | Generates a lexer and parser from ANTLR `.g4` grammars into the sources of a module. |
| [`avro`](plugins/avro/) | Generates Java sources from Avro schemas (`.avsc`) and protocols (`.avpr`). |
| [`checkstyle`](plugins/checkstyle/) | Checks the Java sources of a module with Checkstyle. |
| [`codenarc`](plugins/codenarc/) | Checks the Groovy sources of a module with CodeNarc. |
| [`detekt`](plugins/detekt/) | Checks the Kotlin sources of a module with detekt. |
| [`eclipse`](plugins/eclipse/) | Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the Eclipse formatter. |
| [`forbiddenapis`](plugins/forbiddenapis/) | Checks the compiled classes of a module with forbiddenapis against bundled signatures and signatures files of the project. |
| [`googlejavaformat`](plugins/googlejavaformat/) | Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with google-java-format. |
| [`groovy`](plugins/groovy/) | Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents them with Groovydoc. |
| [`jacoco`](plugins/jacoco/) | Measures the code coverage of a module's tests with the JaCoCo agent and reports it, failing the build where a limit is not met. |
| [`japicmp`](plugins/japicmp/) | Compares the API of a module with a released baseline using japicmp. |
| [`jreleaser`](plugins/jreleaser/) | Releases what the build staged with JReleaser, run in the build's own JVM rather than as a command. |
| [`kotlin`](plugins/kotlin/) | Compiles the Kotlin sources of a module with kotlinc ahead of javac and documents them with Dokka. |
| [`ksp`](plugins/ksp/) | Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module names. |
| [`ktfmt`](plugins/ktfmt/) | Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Kotlin sources of a module with ktfmt. |
| [`ktlint`](plugins/ktlint/) | Checks the Kotlin sources of a module with ktlint (`@check`), or verifies and rewrites their layout (`@format`). |
| [`openapi`](plugins/openapi/) | Generates Java sources from an OpenAPI specification. |
| [`palantir`](plugins/palantir/) | Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with Palantir Java Format. |
| [`pitest`](plugins/pitest/) | Runs PIT mutation testing over the code a test module tests, reporting which mutants its tests kill. |
| [`pmd`](plugins/pmd/) | Checks the Java sources of a module with PMD. |
| [`protoc`](plugins/protoc/) | Generates Java sources from Protocol Buffers definitions (`.proto`). |
| [`revapi`](plugins/revapi/) | Analyses the API of a module against a released baseline with revapi. |
| [`scala`](plugins/scala/) | Compiles the Scala sources of a module with the Scala 3 compiler before javac, jointly with its Java sources, and documents them with Scaladoc. |
| [`scalafmt`](plugins/scalafmt/) | Checks the layout of the Scala sources of a module with scalafmt (`@check`), or verifies and rewrites it (`@format`). |
| [`scalastyle`](plugins/scalastyle/) | Checks the Scala sources of a module with Scalastyle. |
| [`spotbugs`](plugins/spotbugs/) | Analyses the compiled classes of a module with SpotBugs. |
| [`wsimport`](plugins/wsimport/) | Generates a web service client from WSDL descriptions. |
| [`xjc`](plugins/xjc/) | Generates Java sources from XML schemas. |

Each is the module `build.jenesis.plugin.<folder>`, published as `build.jenesis:build.jenesis.plugin.<folder>`. A
project does not name it: the build tool discovers each plugin by itself, as it discovered the tool when the tool was
built in, where a configuration folder of a module holds the tool's own file - an `antlr.properties`, a
`checkstyle.xml`, a `javaformat.properties` naming its formatter - or, for a language, where a module holds a source of
it. The build resolves the plugin there by its module name and pins it in the group of its name, `antlr` for this
file:

```properties
# build.jenesis/antlr.properties
package=demo.grammar
arguments=-visitor
@grammars/antlr=../grammar
```

A setting of its tool switches a plugin off, as `-Djenesis.generate.antlr=false` does this one; a language plugin has
none. `jenesis.plugins.properties` and `plugin-<name>.properties` remain for a plugin of the project's own or of a
third party, and name none of these.

A plugin reads only the files the build binds for it: the tool's own file and what it names beside it - the
`${config_loc}/<path>` files of a `checkstyle.xml`, the `signatures` of a `forbiddenapis.properties`, the `profile` of
an Eclipse `javaformat.properties` - and, for a generator, what its file binds with `@<input>[/<target>]=<path>` lines,
each path resolved against the module's own folder; a folder stays within the project, while a file may lie
outside it, so that several projects share one. A value is resolved before the plugin sees it, `@<key>` standing
for `-Djenesis.variable.<key>`, and a plugin refuses a value it does not take, naming those it does. Checkstyle takes
every line of a `checkstyle.properties` beside its `checkstyle.xml` as a property its configuration expands.

A plugin that offers more than one provider names each, and the build wires each at its own hook point, as ktlint's
`@check` at `check` and its `@format` at `format`. A plugin resolves its tool in a group named after it - `antlr:tool`
for the file above - so the tool is pinned as `antlr:tool/maven/…` beside the project's own pins, and whatever else it
resolves in another `antlr:<purpose>`.

## Layout

One folder per plugin under `plugins/`, each with a short `README.md`, `sources/` (the plugin module), `tests/` (its
`@jenesis.test` module) and `it/`, a test module that builds a small example project with a Jenesis build of its
own, which loads the plugin from a local module repository as the build of a project would. `harness/` holds what
those example tests share; it is released beside the plugins as `build.jenesis.plugin.harness`, so that a plugin of
anyone's is tested the same way. The build tool is the `build/.upstream` git submodule, and `build/jenesis` links into it:

```bash
git submodule update --init --depth 1
java build/jenesis/Make.java              # build and test every plugin
java build/jenesis/Make.java stage        # lay out target/stage/maven/output
```

Classes named `*RunTest` download and run the real tool, so they need the network.

## Releasing

A release is a manual run of the release workflow from the Actions tab: its optional `sha` input names the
commit, its optional `tag` input the version (`vX.Y.Z`; default: the next minor of the latest tag), and its
optional `plugin` input what is released - `all`, the default, releases every plugin and the harness under the
tag `vX.Y.Z`, while the name of a folder under `plugins/`, or `harness`, releases that one alone under the tag
`<name>-vX.Y.Z`. Every release takes a version of its own, so a plugin released alone never shares a version
with a different build of itself.
