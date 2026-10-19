# ktlint

Checks the Kotlin sources of a module with ktlint (`@check`), or verifies and rewrites their layout (`@format`).

```ini
# build.jenesis/.editorconfig
root = true
[*.kt]
indent_size = 4
```

```properties
# build.jenesis/ktlint.properties
configuration=.editorconfig
```

The build discovers the plugin by itself in every module where a configuration folder holds a `ktlint.properties`,
and resolves it there as `build.jenesis.plugin.ktlint`, running both of its providers: `@check` at the hook point
`check` and `@format` at the hook point `format`. Where none is found, an `.editorconfig` of a configuration folder
switches it on alone, as if the properties named it. The file names it as
`configuration=<path>`, relative to `ktlint.properties`, which is required: properties that leave it out fail the
build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the file that `configuration` names, handed to ktlint as `.editorconfig` whatever its name, which the build
binds for both.

`-Djenesis.source.ktlint` sets how findings of the check are treated, as for every checker of the build, and
`-Djenesis.format.ktlint=false` switches the formatting off.
