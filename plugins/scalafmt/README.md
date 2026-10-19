# scalafmt

Checks the layout of the Scala sources of a module with scalafmt (`@check`), or verifies and rewrites it (`@format`).

```hocon
# build.jenesis/.scalafmt.conf
version = "3.8.3"
runner.dialect = scala213
```

```properties
# build.jenesis/scalafmt.properties
configuration=.scalafmt.conf
```

The build discovers the plugin by itself in every module where a configuration folder holds a `scalafmt.properties`,
and resolves it there as `build.jenesis.plugin.scalafmt`, running both of its providers: `@check` at the hook point
`check` and `@format` at the hook point `format`. Where none is found, a `.scalafmt.conf` of a configuration folder
switches it on alone, as if the properties named it. The file names it as
`configuration=<path>`, relative to `scalafmt.properties`, which is required: properties that leave it out fail the
build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the file that `configuration` names, handed to scalafmt as `.scalafmt.conf` whatever its name, which the build
binds for both.

`-Djenesis.source.scalafmt` sets how findings of the check are treated, as for every checker of the build, and
`-Djenesis.format.scalafmt=false` switches the formatting off.
