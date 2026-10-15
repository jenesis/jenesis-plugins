# scalafmt

Checks the layout of the Scala sources of a module with scalafmt (`@check`), or verifies and rewrites it (`@format`).

```hocon
# build.jenesis/.scalafmt.conf
version = "3.8.3"
runner.dialect = scala213
```

The build discovers the plugin by itself in every module where a configuration folder holds a `.scalafmt.conf`, and
resolves it there as `build.jenesis.plugin.scalafmt`, running both of its providers: `@check` at the hook point
`check` and `@format` at the hook point `format`.

It takes no values.

It reads the `.scalafmt.conf`, which the build binds for both.

`-Djenesis.source.scalafmt` sets how findings of the check are treated, as for every checker of the build, and
`-Djenesis.format.scalafmt=false` switches the formatting off.
