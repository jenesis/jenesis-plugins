# XJC

Generates Java sources from XML schemas.

```properties
# jenesis.plugins.properties
xjc+binary/generated=build.jenesis.plugin.xjc
```

```properties
# build.jenesis/plugin-xjc.properties
@schemas/xjc=../xjc
package=example.order
```

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the schemas (`.xsd`), bindings (`.xjb`) and catalog below an `xjc/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
