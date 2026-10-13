# japicmp

Compares the API of a module with a released baseline using japicmp.

```properties
# jenesis.plugins.properties
japicmp+artifact=build.jenesis.plugin.japicmp
```

```properties
# build.jenesis/plugin-japicmp.properties
baseline=org.example/library/1.0.0
format=xml
```

| Value | Meaning |
|---|---|
| `baseline` | the release to compare with, as `<groupId>/<artifactId>[/<version>]` |
| `access` | the least access compared: `public`, `protected`, `package` or `private` |
| `include`, `exclude` | comma-separated japicmp patterns of what is compared |
| `format` | comma-separated report formats: `xml`, `html` |
| `ignore-missing-classes` | `true` by default |
| `only-incompatible`, `only-modified`, `semantic-versioning` | flags of japicmp's own |
| `error-on-binary-incompatibility`, `error-on-source-incompatibility`, `error-on-modifications`, `error-on-semantic-incompatibility` | flags that fail the build on such a change |

Any other value is refused.
