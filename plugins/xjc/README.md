# XJC

Generates Java sources from XML schemas.

```properties
# build.jenesis/xjc.properties
package=example.order
@schemas/xjc=../xjc
```

The build discovers the plugin by itself in every module where a configuration folder holds an `xjc.properties`,
and resolves it there as `build.jenesis.plugin.xjc`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the schemas (`.xsd`), bindings (`.xjb`) and catalog below an `xjc/` folder of what it is handed; the file
binds them with an `@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.xjc=false` switches it off.
