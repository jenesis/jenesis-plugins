# Jenesis Plugins

![build](https://github.com/jenesis/jenesis-plugins/actions/workflows/build.yml/badge.svg)

> ### [Jenesis](https://jenesis.build) - a modern Java build tool
> _Java-native config, plugin-free, with `module-info.java` treated as a feature, not an afterthought._

**The plugins of the Jenesis build tool that run a tool it has to download anyway.** Each plugin is a Java module
that provides a `build.jenesis.BuildModuleFactory`, or an `ObservabilityFactory` for one that observes the tests:
the build that loads it hands it its settings, the resolved values of the `plugin-<name>.properties` that switched
it on, its process permits, the dependency group to resolve its tool in, and its repositories and resolvers, and the
plugin adds the steps that resolve the tool and run it. What the JDK ships stays built into the build tool; what
needs a download lives here, so the tool itself stays small and nothing is fetched that a project does not use.

📖 **The user documentation lives at [jenesis.build](https://jenesis.build).** What follows is for people
working *on* this repository.

## The plugins

Each folder's README names the values its `plugin-<name>.properties` takes and the file it reads.

| Plugin | What it does |
|---|---|
| [`antlr`](plugins/antlr/) | Generates a lexer and parser from ANTLR `.g4` grammars into the sources of a module. |
| [`avro`](plugins/avro/) | Generates Java sources from Avro schemas (`.avsc`) and protocols (`.avpr`). |
| [`checkstyle`](plugins/checkstyle/) | Checks the Java sources of a module with Checkstyle. |
| [`codenarc`](plugins/codenarc/) | Checks the Groovy sources of a module with CodeNarc. |
| [`detekt`](plugins/detekt/) | Checks the Kotlin sources of a module with detekt. |
| [`eclipse`](plugins/eclipse/) | Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the Eclipse formatter. |
| [`jacoco`](plugins/jacoco/) | Measures the code coverage of a module's tests with the JaCoCo agent and reports it, failing the build where a limit is not met. |
| [`japicmp`](plugins/japicmp/) | Compares the API of a module with a released baseline using japicmp. |
| [`jreleaser`](plugins/jreleaser/) | Releases what the build staged with JReleaser, run in the build's own JVM rather than as a command. |
| [`ktlint`](plugins/ktlint/) | Checks the Kotlin sources of a module with ktlint (`@check`), or verifies and rewrites their layout (`@format`). |
| [`openapi`](plugins/openapi/) | Generates Java sources from an OpenAPI specification. |
| [`pmd`](plugins/pmd/) | Checks the Java sources of a module with PMD. |
| [`protoc`](plugins/protoc/) | Generates Java sources from Protocol Buffers definitions (`.proto`). |
| [`revapi`](plugins/revapi/) | Analyses the API of a module against a released baseline with revapi. |
| [`scalafmt`](plugins/scalafmt/) | Checks the layout of the Scala sources of a module with scalafmt (`@check`), or verifies and rewrites it (`@format`). |
| [`scalastyle`](plugins/scalastyle/) | Checks the Scala sources of a module with Scalastyle. |
| [`spotbugs`](plugins/spotbugs/) | Analyses the compiled classes of a module with SpotBugs. |
| [`wsimport`](plugins/wsimport/) | Generates a web service client from WSDL descriptions. |
| [`xjc`](plugins/xjc/) | Generates Java sources from XML schemas. |

Each is the module `build.jenesis.plugin.<folder>`, published as `build.jenesis:build.jenesis.plugin.<folder>`, and,
once named in a project, resolved by the build tool like
any other plugin:

```properties
# jenesis.plugins.properties
antlr+binary/generated=build.jenesis.plugin.antlr
```

```properties
# build.jenesis/plugin-antlr.properties
package=demo.grammar
arguments=-visitor
@grammars/antlr=../grammar
```

A plugin reads a file only where its `plugin-<name>.properties` binds one, and finds it there by the name its tool
gives it - `pmd.xml`, `checkstyle.xml`, `eclipse-formatter.xml`. A file of another name is bound under that name. A
path is resolved against the module's own folder, and a file may lie outside the project, so that several projects
share one configuration:

```properties
# build.jenesis/plugin-checkstyle.properties
@rules/checkstyle.xml=../../shared/company.xml
@rules/suppressions.xml=../../shared/suppressions.xml
checkstyle.severity=warning
```

A value is resolved before the plugin sees it, `@<key>` standing for `-Djenesis.variable.<key>`, and a plugin
refuses a value it does not take, naming those it does. Checkstyle takes every value as a property its configuration
expands.

A plugin that offers more than one provider names each, and the line picks one, as
`ktlint+check=build.jenesis.plugin.ktlint@check`. A plugin resolves its tool in the group named after it - `antlr` above - so the tool is pinned as
`antlr/maven/…` beside the project's own pins.

## Layout

One folder per plugin under `plugins/`, each with a short `README.md`, `sources/` (the plugin module), `tests/` (its
`@jenesis.test` module) and `it/`, a test module that builds a small example project with a Jenesis build of its
own, which loads the plugin from a local module repository as a project that names it would. `testing/` holds what
those example tests share. The build tool is the `build/.upstream` git submodule, and `build/jenesis` links into it:

```bash
git submodule update --init --depth 1
java build/jenesis/Make.java              # build and test every plugin
java build/jenesis/Make.java stage        # lay out target/stage/maven/output
```

Classes named `*RunTest` download and run the real tool, so they need the network.

## Releasing

A release is a manual run of the release workflow from the Actions tab: its optional `sha` input names the
commit and its optional `tag` input the tag (`vX.Y.Z`; default: the next minor of the latest tag). Every
plugin is released with the same version.
