# wsimport

Generates a web service client from WSDL descriptions.

```properties
# jenesis.plugins.properties
wsimport+binary/generated=build.jenesis.plugin.wsimport
```

```properties
# build.jenesis/plugin-wsimport.properties
@descriptions/wsimport=../wsimport
package=example.greeter
```

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `location` | where the generated client reads its description |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the descriptions below a `wsimport/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
