# Palantir Java Format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with Palantir Java Format.

```properties
# build.jenesis/javaformat.properties
formatter=palantir
reflow=false
```

The build discovers the plugin by itself in every module where a configuration folder holds a `javaformat.properties`
naming `formatter=palantir`, and resolves it there as `build.jenesis.plugin.palantir`. The other lines of the file are
its values:

| Value | Meaning |
|---|---|
| `reflow` | reflows long string literals, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and a file with no other line formats with the defaults.

It reads no file.

`-Djenesis.format.java=false` switches it off.
