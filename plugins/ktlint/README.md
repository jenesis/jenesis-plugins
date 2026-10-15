# ktlint

Checks the Kotlin sources of a module with ktlint (`@check`), or verifies and rewrites their layout (`@format`).

```ini
# build.jenesis/.editorconfig
root = true
[*.kt]
indent_size = 4
```

The build discovers the plugin by itself in every module where a configuration folder holds an `.editorconfig`, and
resolves it there as `build.jenesis.plugin.ktlint`, running both of its providers: `@check` at the hook point
`check` and `@format` at the hook point `format`.

It takes no values.

It reads the `.editorconfig`, which the build binds for both.

`-Djenesis.source.ktlint` sets how findings of the check are treated, as for every checker of the build, and
`-Djenesis.format.ktlint=false` switches the formatting off.
