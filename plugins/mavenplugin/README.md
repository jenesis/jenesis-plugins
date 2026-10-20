# Maven plugin

Writes the descriptor of a Maven plugin into a module's jar, with the generator of Maven's own plugin tools, so that
Jenesis builds Maven plugins.

```properties
# jenesis.plugins.properties
mavenplugin+binary/transform=build.jenesis.plugin.mavenplugin
```

```properties
# src/main/build.jenesis/plugin-mavenplugin.properties
goalprefix=greet
```

The plugin is declared rather than discovered: the line in `jenesis.plugins.properties` resolves it as
`build.jenesis.plugin.mavenplugin` and wires it after compilation, in every module where a configuration folder holds a
`plugin-mavenplugin.properties`, which may be empty. It reads the mojos of the module's classes, the javadoc of their
sources, the coordinate, name and description of the module's POM and the dependencies it was resolved with, and adds
`META-INF/maven/plugin.xml` and `META-INF/maven/<groupId>/<artifactId>/plugin-help.xml` to its classes, as
`maven-plugin-plugin` writes them. The mojos compile against `maven-plugin-api` and `maven-plugin-annotations`, which
a `pom.xml` declares in scope `provided` and a `module-info.java` by `requires static`; the published POM may keep the
packaging `jar`, as Maven reads a plugin from its descriptor.

| Value | Meaning |
|---|---|
| `goalprefix` | the prefix a goal is called by, as `greet` in `greet:hello`; by default the one Maven derives from the artifact ID |
| `maven` | the release of Maven whose API the generator reads the mojos with, `3.9.11` by default |
| `tools` | the release of Maven's plugin tools that generate the descriptor, `3.15.1` by default |

Any other value is refused. A dependency resolved for compiling alone is provided by Maven and not listed among the
descriptor's dependencies; the release of `maven-plugin-api` names the Maven version the plugin requires, and the
classes the Java version.

`-Djenesis.plugin.mavenplugin=false` switches it off, as for every plugin `jenesis.plugins.properties` names.
