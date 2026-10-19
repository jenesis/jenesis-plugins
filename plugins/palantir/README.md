# Palantir Java Format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with Palantir Java Format.

```properties
# build.jenesis/palantir.properties
reflow=false
```

The build discovers the plugin by itself in every module where a configuration folder holds a `palantir.properties`,
and resolves it there as `build.jenesis.plugin.palantir`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `reflow` | reflows long string literals, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and an empty file formats with the defaults. A build that finds the properties of a second
Java formatter fails, naming both.

It reads no file.

`-Djenesis.format.palantir=false` switches it off.
