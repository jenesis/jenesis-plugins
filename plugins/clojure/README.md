# Clojure

Compiles the Clojure namespaces of a module ahead of time, after javac and against the classes it wrote, with the
Clojure the module depends on, and ships their sources beside the classes.

The build discovers the plugin by itself in every module that holds a `.clj` source, and resolves it there as
`build.jenesis.plugin.clojure`; `-Djenesis.language.clojure=false` leaves it out, so a module holding a `.clj` source
then fails the build. A build whose Jenesis does not discover it yet names it in `jenesis.plugins.properties` for the
hook point `language`, beside an empty `plugin-clojure.properties` in its configuration folder:

```properties
clojure+language=build.jenesis.plugin.clojure
```

The module declares its own Clojure, as every Clojure project does, and the plugin compiles with it, so the classes
match the Clojure they run on; without `org.clojure:clojure` on its compile class path, the build fails naming it. A
`pom.xml` module keeps its namespaces in `src/main/clojure`, which `jenesis.maven.sources=clojure` adds to its sources.

The plugin the build discovers takes the defaults below. A build that sets a value declares the plugin, as above,
and writes the value into its `plugin-clojure.properties`, switching the discovered one off with
`-Djenesis.language.clojure=false` in its `jenesis.properties`:

| Value | Default | Effect |
|---|---|---|
| `aot` | `true` | Compile every namespace with `clojure.lang.Compile`; `false` ships the sources alone, as a library that is loaded from source does. |

It reads nothing a project binds. Each namespace is named after the path of its source, a `data_readers.clj` or
`data_readers.cljc` at the root ships as it is, and the classes Clojure writes for the namespaces of a dependency,
which it compiles as the module's namespaces require them, are left out, as that dependency's jar serves them. A
`process-clojure.properties` configures the JVM the compiler runs in.
