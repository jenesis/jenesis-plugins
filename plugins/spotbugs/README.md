# SpotBugs

Analyses the compiled classes of a module with SpotBugs.

```properties
# jenesis.plugins.properties
spotbugs+binary/validate=build.jenesis.plugin.spotbugs
```

```properties
# build.jenesis/plugin-spotbugs.properties
@rules=../spotbugs-exclude.xml
```

It takes no values.

It reads a `spotbugs-exclude.xml` filter, if one is bound; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.validator.spotbugs` sets how findings are treated, as for every checker of the build.
