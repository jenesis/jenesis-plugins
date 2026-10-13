# ktlint

Checks the Kotlin sources of a module with ktlint (`@check`), or verifies and rewrites their layout (`@format`).

```properties
# jenesis.plugins.properties
ktlint+check=build.jenesis.plugin.ktlint@check
```

```properties
# build.jenesis/plugin-ktlint.properties
@rules=../.editorconfig
```

It takes no values.

It reads an `.editorconfig`; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.ktlint` sets how findings are treated, as for every checker of the build.
