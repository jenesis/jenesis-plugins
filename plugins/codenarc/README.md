# CodeNarc

Checks the Groovy sources of a module with CodeNarc.

```properties
# jenesis.plugins.properties
codenarc+check=build.jenesis.plugin.codenarc
```

```properties
# build.jenesis/plugin-codenarc.properties
@rules=../codenarc.xml
```

It takes no values.

It reads a `codenarc.xml` rule set; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.codenarc` sets how findings are treated, as for every checker of the build.
