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

```properties
# build.jenesis/detekt.properties
configuration=detekt.yml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `detekt.properties`,
and resolves it there as `build.jenesis.plugin.detekt`; where none is found, a `detekt.yml` of a configuration
folder switches it on alone, as if the properties named it. The file names the
configuration as `configuration=<path>`, relative to `detekt.properties`, which is required: properties that leave it out
fail the build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the configuration that `configuration` names, handed to detekt as `detekt.yml` whatever its name, which the
build binds.

`-Djenesis.source.detekt` sets how findings are treated, as for every checker of the build.
