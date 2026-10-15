# KSP

Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module names.

```properties
# build.jenesis/ksp.properties
moshi.generated=javax.annotation.processing.Generated
```

The build discovers the plugin by itself in every module where a configuration folder holds a `ksp.properties`, and
resolves it there as `build.jenesis.plugin.ksp`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| <any key> | an option handed to the processors as `<key>=<value>`, as Gradle's `ksp { arg(...) }` hands it |

The processors are named in the processor group of the plugin, as `@jenesis.plugin ksp:processor
maven/<groupId>/<artifactId>` in a `module-info.java` or `<!--jenesis.plugin ksp:processor maven/<groupId>/<artifactId>-->`
in a `pom.xml`, and pinned as `ksp:processor/maven/…`. KSP itself resolves apart from them, in `ksp:tool`, because a
processor brings a Kotlin standard library of its own.

It reads the sources of the module and resolves their types against the module's dependencies; it binds no file.
What the processors write is compiled with the module's sources, and the resources they write join its jar.

KSP runs incrementally, as Gradle runs it: a build hands it the sources that changed or were removed since the last
one and the classes of the module's dependencies whose bytes changed, so a processor handles only the sources a change
reaches and KSP keeps what the others generated. A change to the processors, their options or KSP itself processes
the module from scratch. Under the build daemon, KSP runs in a JVM the build keeps warm for the next module.

`-Djenesis.generate.ksp=false` switches it off.
