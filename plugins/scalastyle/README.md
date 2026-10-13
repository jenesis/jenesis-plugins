# Scalastyle

Checks the Scala sources of a module with Scalastyle.

```properties
# jenesis.plugins.properties
scalastyle+check=build.jenesis.plugin.scalastyle
```

```properties
# build.jenesis/plugin-scalastyle.properties
@rules=../scalastyle-config.xml
```

It takes no values.

It reads a `scalastyle-config.xml`; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.scalastyle` sets how findings are treated, as for every checker of the build.
