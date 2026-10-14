# Groovy

Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
them with Groovydoc.

```properties
# jenesis.plugins.properties
groovy+language=build.jenesis.plugin.groovy
```

```properties
# build.jenesis/plugin-groovy.properties
include-java=true
```

| Value | Meaning |
|---|---|
| `include-java` | `true` to document the Java sources of a module with Groovydoc as well, `false` by default |

Any other value is refused.

It runs in every module that holds a `.groovy` file, which the build compiles only with this plugin, and needs no
`plugin-groovy.properties`. It reads nothing a project binds: groovyc compiles the module's `.groovy` sources, and
Groovydoc documents them at the root of the module's documentation, or in a `groovy/` folder of it beside the Javadoc
of the module's Java sources. Groovy and Groovydoc resolve in the group named after the plugin, `groovy/…`, and a
`process-groovyc.properties` or `process-groovydoc.properties` configures the JVM each runs in.
