# PMD

Checks the Java sources of a module with PMD.

```properties
# jenesis.plugins.properties
pmd+check=build.jenesis.plugin.pmd
```

```properties
# build.jenesis/plugin-pmd.properties
@rules=../pmd.xml
```

It takes no values.

It reads a `pmd.xml` rule set; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.pmd` and `jenesis.pmd.priority` sets how findings are treated, as for every checker of the build.
