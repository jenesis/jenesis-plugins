# detekt

Checks the Kotlin sources of a module with detekt.

```yaml
# build.jenesis/detekt.yml
style:
  active: true
  MaxLineLength:
    active: true
    maxLineLength: 120
```

The build discovers the plugin by itself in every module where a configuration folder holds a `detekt.yml`, and
resolves it there as `build.jenesis.plugin.detekt`.

It takes no values.

It reads the `detekt.yml`, which the build binds.

`-Djenesis.source.detekt` sets how findings are treated, as for every checker of the build.
