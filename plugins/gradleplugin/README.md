# Gradle plugin

Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID with
`plugins { id '<id>' version '<version>' }` from the Maven repository the project is published to.

```properties
# jenesis.plugins.properties
gradleplugin+stage/transform=build.jenesis.plugin.gradleplugin
```

```properties
# src/main/resources/META-INF/gradle-plugins/com.example.greet.properties
implementation-class=example.GreetPlugin
```

The plugin is declared rather than discovered: the line in `jenesis.plugins.properties` resolves it as
`build.jenesis.plugin.gradleplugin` and wires it after the stock staging where the project's configuration folder holds
a `plugin-gradleplugin.properties`, which stays empty. A Gradle plugin is an ordinary jar the build compiles - against
the Gradle API, which a `pom.xml` declares in scope `provided` and a `module-info.java` by `requires static` - and the
jar names each plugin it implements by a `META-INF/gradle-plugins/<id>.properties` among its resources, which a build
that puts the jar on its `buildscript` class path applies without a marker. For every such file of a jar the Maven
tree stages, the plugin stages the marker `<id>:<id>.gradle.plugin` that the `plugins` block looks the ID up by: a POM
that depends on the jar and carries the name, description, URL, licences, developers and SCM of the jar's POM, as a
repository such as Maven Central requires.

It takes no values. A plugin ID that is not made of letters, digits and `-` in segments separated by single dots, an
ID in a namespace Gradle reserves (`org.gradle`, `com.gradleware`), a descriptor naming no `implementation-class` or a
class its jar does not hold, and an ID two jars declare each fail the build. Publishing to the Gradle Plugin Portal,
which takes its own credentials, is left to the portal's own tooling.

`-Djenesis.plugin.gradleplugin=false` switches it off, as for every plugin `jenesis.plugins.properties` names.
