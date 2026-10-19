# google-java-format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with google-java-format.

```properties
# build.jenesis/googlejavaformat.properties
style=aosp
imports=sort
```

The build discovers the plugin by itself in every module where a configuration folder holds a
`googlejavaformat.properties`, and resolves it there as `build.jenesis.plugin.googlejavaformat`. The lines of the file
are its values:

| Value | Meaning |
|---|---|
| `style` | `google` (the default) or `aosp`, which indents by four |
| `reflow` | reflows long string literals, `true` by default |
| `javadoc` | formats Javadoc comments, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and an empty file formats with the defaults. A build that finds the properties of a second
Java formatter fails, naming both.

It reads no file.

`-Djenesis.format.googlejavaformat=false` switches it off.
