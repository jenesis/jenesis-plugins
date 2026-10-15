# revapi

Analyses the API of a module against a released baseline with revapi.

```properties
# build.jenesis/revapi.properties
baseline=org.example/library/1.0.0
configuration=revapi.json
```

The build discovers the plugin by itself in every module but a test module where a configuration folder holds a
`revapi.properties`, and resolves it there as `build.jenesis.plugin.revapi`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `baseline` | the release to compare with, as `<groupId>/<artifactId>[/<version>]` |
| `severity` | the least severity reported: `equivalent`, `non-breaking`, `potentially-breaking` (the default) or `breaking` |
| `configuration` | a `revapi.json` beside `revapi.properties`, revapi's own configuration as a JSON array |

Any other value is refused.

It reads the `revapi.json` that `configuration` names, if it names one, which the build binds.

`-Djenesis.artifact.revapi` sets how findings are treated, as for every checker of the build.
