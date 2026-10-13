# OpenAPI Generator

Generates Java sources from an OpenAPI specification.

```properties
# jenesis.plugins.properties
openapi+binary/generated=build.jenesis.plugin.openapi
```

```properties
# build.jenesis/plugin-openapi.properties
@specification/openapi/openapi.yml=../openapi.yml
package=example.api
```

| Value | Meaning |
|---|---|
| `generator` | the generator to run, `java` by default |
| `package` | the package of the generated sources |
| `sources` | the folder of the generated project the sources are collected from, `src/main/java` by default |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the specification named `openapi.*` in an `openapi/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
