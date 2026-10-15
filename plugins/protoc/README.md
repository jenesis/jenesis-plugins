# protoc

Generates Java sources from Protocol Buffers definitions (`.proto`).

```properties
# build.jenesis/protoc.properties
@definitions/protoc=../protoc
```

The build discovers the plugin by itself in every module where a configuration folder holds a `protoc.properties`,
and resolves it there as `build.jenesis.plugin.protoc`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `classifier` | the platform of the protoc executable, detected by default |
| `plugins` | comma-separated protoc plugins as `<name>=<groupId>/<artifactId>`, each resolved in `protoc:<name>`; `tool` is protoc's own |
| `arguments` | further arguments of the tool, split at white space |

Any other value is refused.

It reads the definitions below a `protoc/` folder of what it is handed; the file binds them with an
`@<input>[/<target>]=<path>` line, its path resolved against the module's own folder.

`-Djenesis.generate.protoc=false` switches it off.
