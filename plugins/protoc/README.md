# protoc

Generates Java sources from Protocol Buffers definitions (`.proto`).

```properties
# jenesis.plugins.properties
protoc+binary/generated=build.jenesis.plugin.protoc
```

```properties
# build.jenesis/plugin-protoc.properties
@definitions/protoc=../protoc
```

| Value | Meaning |
|---|---|
| `classifier` | the platform of the protoc executable, detected by default |
| `plugins` | comma-separated protoc plugins as `<name>=<groupId>/<artifactId>`, each resolved in `protoc:<name>`; `tool` is protoc's own |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the definitions below a `protoc/` folder of what it is handed; the project binds it with an `@<input>[/<target>]=<path>` line.
