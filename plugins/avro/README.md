# Avro

Generates Java sources from Avro schemas (`.avsc`) and protocols (`.avpr`).

```properties
# jenesis.plugins.properties
avro+binary/generated=build.jenesis.plugin.avro
```

```properties
# build.jenesis/plugin-avro.properties
@schemas/avro=../avro
```

| Value | Meaning |
|---|---|
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the schemas and protocols below an `avro/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
