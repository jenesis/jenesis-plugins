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

| Folder              | Module                          | Runs                                                     |
|---------------------|---------------------------------|----------------------------------------------------------|
| `plugins/antlr/`    | `build.jenesis.plugin.antlr`    | ANTLR, generating a lexer and parser from `.g4` grammars |
| `plugins/pmd/`      | `build.jenesis.plugin.pmd`      | PMD, checking Java sources against a `pmd.xml` rule set  |
| `plugins/spotbugs/` | `build.jenesis.plugin.spotbugs` | SpotBugs, analysing compiled classes                     |

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

A plugin resolves its tool in the group named after it - `antlr` above - so the tool is pinned as
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
