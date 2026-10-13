# Jenesis Plugins

![build](https://github.com/jenesis/jenesis-plugins/actions/workflows/build.yml/badge.svg)

> ### [Jenesis](https://jenesis.build) - a modern Java build tool
> _Java-native config, plugin-free, with `module-info.java` treated as a feature, not an afterthought._

**The plugins of the Jenesis build tool that run a tool it has to download anyway.** Each plugin is a Java
module that provides a `build.jenesis.BuildModuleFactory`: the build that loads it hands it its settings, the
file that switched it on, its process permits, the dependency group to resolve its tool in, and its
repositories and resolvers, and the plugin adds the steps that resolve the tool and run it. What the JDK
ships stays built into the build tool; what needs a download lives here, so the tool itself stays small and
nothing is fetched that a project does not use.

📖 **The user documentation lives at [jenesis.build](https://jenesis.build).** What follows is for people
working *on* this repository.

## The plugins

| Folder                | Module                            | Runs                                                                    |
|-----------------------|-----------------------------------|-------------------------------------------------------------------------|
| `plugins/antlr/`      | `build.jenesis.plugin.antlr`      | ANTLR, generating a lexer and parser from `.g4` grammars                |
| `plugins/avro/`       | `build.jenesis.plugin.avro`       | Avro, generating Java sources from schemas and protocols                |
| `plugins/checkstyle/` | `build.jenesis.plugin.checkstyle` | Checkstyle, checking Java sources against a `checkstyle.xml`            |
| `plugins/codenarc/`   | `build.jenesis.plugin.codenarc`   | CodeNarc, checking Groovy sources against a rule set                    |
| `plugins/detekt/`     | `build.jenesis.plugin.detekt`     | detekt, checking Kotlin sources against a `detekt.yml`                  |
| `plugins/eclipse/`    | `build.jenesis.plugin.eclipse`    | the Eclipse formatter, formatting Java sources with an exported profile |
| `plugins/japicmp/`    | `build.jenesis.plugin.japicmp`    | japicmp, comparing the API of a module with its released baseline       |
| `plugins/ktlint/`     | `build.jenesis.plugin.ktlint`     | ktlint, checking or formatting Kotlin sources (`@check`, `@format`)     |
| `plugins/openapi/`    | `build.jenesis.plugin.openapi`    | OpenAPI Generator, generating Java sources from a specification         |
| `plugins/pmd/`        | `build.jenesis.plugin.pmd`        | PMD, checking Java sources against a `pmd.xml` rule set                 |
| `plugins/protoc/`     | `build.jenesis.plugin.protoc`     | protoc, generating Java sources from Protocol Buffers definitions       |
| `plugins/revapi/`     | `build.jenesis.plugin.revapi`     | revapi, analysing the API of a module against its released baseline     |
| `plugins/scalafmt/`   | `build.jenesis.plugin.scalafmt`   | scalafmt, checking or formatting Scala sources (`@check`, `@format`)    |
| `plugins/scalastyle/` | `build.jenesis.plugin.scalastyle` | Scalastyle, checking Scala sources against a `scalastyle-config.xml`    |
| `plugins/spotbugs/`   | `build.jenesis.plugin.spotbugs`   | SpotBugs, analysing compiled classes                                    |
| `plugins/wsimport/`   | `build.jenesis.plugin.wsimport`   | wsimport, generating a web service client from WSDL descriptions        |
| `plugins/xjc/`        | `build.jenesis.plugin.xjc`        | XJC, generating Java sources from XML schemas                           |

Each is published as `build.jenesis:<module>` and, once named in a project, resolved by the build tool like
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

A plugin that offers more than one provider names each, and the line picks one, as
`ktlint+check=build.jenesis.plugin.ktlint@check`. A plugin resolves its tool in the group named after it - `antlr` above - so the tool is pinned as
`antlr/maven/…` beside the project's own pins.

## Layout

One folder per plugin under `plugins/`, each with `sources/` (the plugin module) and `tests/` (its
`@jenesis.test` module). The build tool is the `build/.upstream` git submodule, and `build/jenesis` links into it:

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
