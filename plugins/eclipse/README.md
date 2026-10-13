# Eclipse formatter

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the Eclipse formatter.

```properties
# jenesis.plugins.properties
eclipse+format=build.jenesis.plugin.eclipse
```

```properties
# build.jenesis/plugin-eclipse.properties
@profile/eclipse-formatter.xml=../formatter.xml
```

It takes no values.

It reads an `eclipse-formatter.xml`, the formatter profile Eclipse exports; the project binds it with an `@<input>[/<target>]=<path>` line.
