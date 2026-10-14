# KSP

Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module names.

```properties
# jenesis.plugins.properties
ksp+binary/generated=build.jenesis.plugin.ksp
```

```properties
# build.jenesis/plugin-ksp.properties
moshi.generated=javax.annotation.processing.Generated
```

| Value | Meaning |
|---|---|
| <any key> | an option handed to the processors as `<key>=<value>`, as Gradle's `ksp { arg(...) }` hands it |

The processors are named in the group of the plugin's name, as `@jenesis.plugin ksp maven/<groupId>/<artifactId>` in a
`module-info.java` or `<!--jenesis.plugin ksp maven/<groupId>/<artifactId>-->` in a `pom.xml`, and pinned as
`ksp/maven/…`. KSP itself resolves apart from them, in `ksp:runtime`, because a processor brings a Kotlin standard
library of its own.

It reads the sources of the module and resolves their types against the module's dependencies; it binds no file.
What the processors write is compiled with the module's sources, and the resources they write join its jar.
