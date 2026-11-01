# Prince of Space

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the
[Prince of Space](https://github.com/agustafson/prince-of-space) formatter.

```properties
# jenesis.plugins.properties
princeofspace+format=build.jenesis.plugin.princeofspace
```

```properties
# build.jenesis/plugin-princeofspace.properties
wrap-style=wide
line-length=100
```

A project names the plugin in its `jenesis.plugins.properties`, and the build runs it in every module where a
configuration folder holds a `plugin-princeofspace.properties`. The lines of that file are its values:

| Value | Meaning |
|---|---|
| `wrap-style` | `balanced` (the default), `wide` or `narrow` |
| `indent-style` | `spaces` (the default) or `tabs` |
| `indent-size` | indent units per block level, `4` by default; with `tabs` each unit is a tab |
| `line-length` | the line length wrapping aims for, `120` by default |
| `closing-paren-on-new-line` | puts the closing `)` of a wrapped argument list on its own line, `true` by default |
| `trailing-commas` | adds trailing commas to multi-line enums and array literals, `false` by default |
| `java-version` | the Java language level the sources are parsed at, `17` by default |

Any other value is refused, and an empty file formats with the defaults. The formatter,
`io.github.agustafson.princeofspace:prince-of-space-core`, resolves in `plugin.princeofspace:tool/…`.

It reads no file.

`-Djenesis.plugin.princeofspace=false` switches it off.
