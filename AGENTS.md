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
  named at the hook point `observe`: its `Observation` pairs the `ObservabilityEngine` that joins the tests' JVM, named
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
- A value of `plugin-<name>.properties` the plugin does not take is refused with an
  `IllegalArgumentException` that names it and what would be valid; a plugin configured by settings alone
  takes no values. A value arrives resolved, and never as a path: a file the plugin reads is one the project binds
  as `@<input>[/<target>]=<path>`, which the plugin finds among its arguments by the name its tool gives it
  (`pmd.xml`, `eclipse-formatter.xml`, `revapi.json`), so it reads nothing a line does not name.
- The plugin compiles against a published `build.jenesis`, pinned in its descriptor, never against the
  vendored engine that runs the build: a plugin needs a release of the tool that carries the API it uses.

- Every plugin folder carries a short `README.md`: one sentence on what it does, the line naming it and an example
  `plugin-<name>.properties`, a table of the values it takes, and the file it reads. The root `README.md` lists
  every plugin with that sentence and a link to its folder; a new plugin is added to both.

## Tests

- `it/` per plugin builds an example project through `ExampleProject` of the shared `testing/` module: it installs
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

A release is a manual run of the release workflow from the Actions tab, so any commit is releasable; every
plugin is released with one version. The build tool pin is moved by checking out the new commit in
`build/.upstream`, building, and committing the submodule pointer; the plugins' `build.jenesis` pin is moved by
`pin` once the release they need is published.
