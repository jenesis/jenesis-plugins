# google-java-format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with google-java-format.

```properties
# build.jenesis/javaformat.properties
formatter=google
style=aosp
imports=sort
```

The build discovers the plugin by itself in every module where a configuration folder holds a `javaformat.properties`
naming `formatter=google`, and resolves it there as `build.jenesis.plugin.googlejavaformat`. The other lines of the
file are its values:

| Value | Meaning |
|---|---|
| `style` | `google` (the default) or `aosp`, which indents by four |
| `reflow` | reflows long string literals, `true` by default |
| `javadoc` | formats Javadoc comments, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and a file with no other line formats with the defaults.

It reads no file.

`-Djenesis.format.java=false` switches it off.
