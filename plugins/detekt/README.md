# detekt

Checks the Kotlin sources of a module with detekt.

```properties
# jenesis.plugins.properties
detekt+check=build.jenesis.plugin.detekt
```

```properties
# build.jenesis/plugin-detekt.properties
@rules=../detekt.yml
```

It takes no values.

It reads a `detekt.yml`; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.detekt` sets how findings are treated, as for every checker of the build.
