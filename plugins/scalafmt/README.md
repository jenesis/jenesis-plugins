# scalafmt

Checks the layout of the Scala sources of a module with scalafmt (`@check`), or verifies and rewrites it (`@format`).

```properties
# jenesis.plugins.properties
scalafmt+check=build.jenesis.plugin.scalafmt@check
```

```properties
# build.jenesis/plugin-scalafmt.properties
@rules=../.scalafmt.conf
```

It takes no values.

It reads a `.scalafmt.conf`; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.scalafmt` sets how findings are treated, as for every checker of the build.
