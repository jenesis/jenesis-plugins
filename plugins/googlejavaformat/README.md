# google-java-format

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with google-java-format.

```properties
# jenesis.plugins.properties
googlejavaformat+format=build.jenesis.plugin.googlejavaformat
```

```properties
# build.jenesis/plugin-googlejavaformat.properties
style=aosp
imports=sort
```

| Value | Meaning |
|---|---|
| `style` | `google` (the default) or `aosp`, which indents by four |
| `reflow` | reflows long string literals, `true` by default |
| `javadoc` | formats Javadoc comments, `true` by default |
| `imports` | comma-separated `sort` and `remove` (unused ones), both by default, or `none` |

Any other value is refused, and an empty file formats with the defaults.

It reads no file.
