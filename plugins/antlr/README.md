# ANTLR

Generates a lexer and parser from ANTLR `.g4` grammars into the sources of a module.

```properties
# build.jenesis/antlr.properties
package=example.grammar
@grammars/antlr=../grammar
```

The build discovers the plugin by itself in every module where a configuration folder holds an `antlr.properties`,
and resolves it there as `build.jenesis.plugin.antlr`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the `.g4` grammars below an `antlr/` folder of what it is handed; the file binds them with an
`@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.antlr=false` switches it off.
