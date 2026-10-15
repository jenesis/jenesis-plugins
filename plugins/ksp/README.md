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

`-Djenesis.generate.ksp=false` switches it off.
