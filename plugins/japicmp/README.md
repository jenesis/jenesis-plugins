# japicmp

Compares the API of a module with a released baseline using japicmp.

```properties
# build.jenesis/japicmp.properties
baseline=org.example/library/1.0.0
format=xml
```

The build discovers the plugin by itself in every module but a test module where a configuration folder holds a
`japicmp.properties`, and resolves it there as `build.jenesis.plugin.japicmp`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `baseline` | the release to compare with, as `<groupId>/<artifactId>[/<version>]` |
| `access` | the least access compared: `public`, `protected`, `package` or `private` |
| `include`, `exclude` | japicmp patterns of what is compared, separated by commas or semicolons; a comma between the parentheses of a method belongs to its pattern |
| `format` | comma-separated report formats: `xml`, `html` |
| `ignore-missing-classes` | `true` by default |
| `only-incompatible`, `only-modified`, `semantic-versioning` | flags of japicmp's own |
| `error-on-binary-incompatibility`, `error-on-source-incompatibility`, `error-on-modifications`, `error-on-semantic-incompatibility` | flags that fail the build on such a change |

Any other value is refused.

`-Djenesis.artifact.japicmp=false` switches it off.
