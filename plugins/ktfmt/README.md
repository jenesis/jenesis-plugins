# ktfmt

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Kotlin sources of a module with ktfmt.

```properties
# build.jenesis/ktfmt.properties
style=kotlinlang
```

The build discovers the plugin by itself in every module where a configuration folder holds a `ktfmt.properties`, and
resolves it there as `build.jenesis.plugin.ktfmt`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `style` | `meta` (the default), `google` or `kotlinlang` |
| `imports` | `sort,remove` (the default) removes the unused imports as well, `sort` keeps them; ktfmt always sorts |

Any other value is refused, and an empty file formats with the defaults.

It reads no file. A `ktfmt.properties` beside an `.editorconfig` is refused, as ktlint formats there, unless
`-Djenesis.format.ktlint=false` switches ktlint's formatting off.

`-Djenesis.format.ktfmt=false` switches it off.
