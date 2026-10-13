# ktfmt

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Kotlin sources of a module with ktfmt.

```properties
# jenesis.plugins.properties
ktfmt+format=build.jenesis.plugin.ktfmt
```

```properties
# build.jenesis/plugin-ktfmt.properties
style=kotlinlang
```

| Value | Meaning |
|---|---|
| `style` | `meta` (the default), `google` or `kotlinlang` |
| `imports` | `sort,remove` (the default) removes the unused imports as well, `sort` keeps them; ktfmt always sorts |

Any other value is refused, and an empty file formats with the defaults.

It reads no file.
