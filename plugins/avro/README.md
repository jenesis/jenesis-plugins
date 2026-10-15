# Avro

Generates Java sources from Avro schemas (`.avsc`) and protocols (`.avpr`).

```properties
# build.jenesis/avro.properties
@schemas/avro=../avro
```

The build discovers the plugin by itself in every module where a configuration folder holds an `avro.properties`,
and resolves it there as `build.jenesis.plugin.avro`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the schemas and protocols below an `avro/` folder of what it is handed; the file binds them with an
`@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.avro=false` switches it off.
