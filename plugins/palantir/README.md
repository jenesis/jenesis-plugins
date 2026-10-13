# Palantir Java Format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with Palantir Java Format.

```properties
# jenesis.plugins.properties
palantir+format=build.jenesis.plugin.palantir
```

```properties
# build.jenesis/plugin-palantir.properties
reflow=false
```

| Value | Meaning |
|---|---|
| `reflow` | reflows long string literals, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and an empty file formats with the defaults.

It reads no file.
