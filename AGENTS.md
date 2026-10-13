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
- The module is the build tool's module of the same tool, moved: the same steps, the same folder conventions,
  the same settings. Its `ofEnvironment` reads every setting, its pinning included, from the `Environment` it
  is given, and every setting is a wither of its own.
- The factory is the one entry point. It builds the module with `ofEnvironment` from what it is handed: the
  permits it was handed (`permits(permits)`), so the tool runs within the loading build's limit of concurrent
  processes, and the group it was handed (`tool(group)`), so the tool is resolved and pinned where the loading
  build says. It resolves the tool with the repositories and resolvers it was handed, never with its own.
- A value of `plugin-<name>.properties` the plugin does not take is refused with an
  `IllegalArgumentException` that names it and what would be valid; a plugin configured by settings alone
  takes no values.
- The plugin compiles against a published `build.jenesis`, pinned in its descriptor, never against the
  vendored engine that runs the build: a plugin needs a release of the tool that carries the API it uses.

## Tests

- `tests/` per plugin, on JUnit Jupiter with AssertJ. A test method is a sentence in `snake_case` stating the
  behaviour it proves; a test of the factory proves what the plugin does with each thing it is handed.
- A `*RunTest` pins the tool's closure in a `versions.properties` it writes, and resolves under strict pinning.

## Releasing and the build tool

A release is a manual run of the release workflow from the Actions tab, so any commit is releasable; every
plugin is released with one version. The build tool pin is moved by checking out the new commit in
`build/.upstream`, building, and committing the submodule pointer; the plugins' `build.jenesis` pin is moved by
`pin` once the release they need is published.
