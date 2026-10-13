# ANTLR

Generates a lexer and parser from ANTLR `.g4` grammars into the sources of a module.

```properties
# jenesis.plugins.properties
antlr+binary/generated=build.jenesis.plugin.antlr
```

```properties
# build.jenesis/plugin-antlr.properties
@grammars/antlr=../grammar
package=example.grammar
```

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the `.g4` grammars below an `antlr/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
