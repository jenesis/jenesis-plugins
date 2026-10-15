# OpenAPI Generator

Generates Java sources from an OpenAPI specification.

```properties
# build.jenesis/openapi.properties
package=example.api
@specification/openapi/openapi.yml=../openapi.yml
```

The build discovers the plugin by itself in every module where a configuration folder holds an `openapi.properties`,
and resolves it there as `build.jenesis.plugin.openapi`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `generator` | the generator to run, `java` by default |
| `package` | the package of the generated sources |
| `sources` | the folder of the generated project the sources are collected from, `src/main/java` by default |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the specification named `openapi.*` in an `openapi/` folder of what it is handed; the file binds it with an
`@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.openapi=false` switches it off.
