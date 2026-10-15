# wsimport

Generates a web service client from WSDL descriptions.

```properties
# build.jenesis/wsimport.properties
package=example.greeter
@descriptions/wsimport=../wsimport
```

The build discovers the plugin by itself in every module where a configuration folder holds a `wsimport.properties`,
and resolves it there as `build.jenesis.plugin.wsimport`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `package` | the package of the generated sources |
| `location` | where the generated client reads its description |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the descriptions below a `wsimport/` folder of what it is handed; the file binds them with an
`@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.wsimport=false` switches it off.
