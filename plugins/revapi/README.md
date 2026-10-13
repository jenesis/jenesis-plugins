# revapi

Analyses the API of a module against a released baseline with revapi.

```properties
# jenesis.plugins.properties
revapi+artifact=build.jenesis.plugin.revapi
```

```properties
# build.jenesis/plugin-revapi.properties
baseline=org.example/library/1.0.0
@analysis/revapi.json=../revapi.json
```

| Value | Meaning |
|---|---|
| `baseline` | the release to compare with, as `<groupId>/<artifactId>[/<version>]` |
| `severity` | the least severity reported: `equivalent`, `non-breaking`, `potentially-breaking` (the default) or `breaking` |

Any other value is refused.

It reads a `revapi.json`, revapi's own configuration as a JSON array, if one is bound; the project binds it with an `@<input>[/<target>]=<path>` line.

`-Djenesis.artifact.revapi` sets how findings are treated, as for every checker of the build.
