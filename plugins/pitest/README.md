# PIT

Runs PIT mutation testing over the code a test module tests, reporting which mutants its tests kill.

```properties
# build.jenesis/pitest.properties
targetClasses=com.example.*
targetTests=com.example.test.*
outputFormats=XML,HTML
```

The build discovers the plugin by itself in every test module where a configuration folder holds a
`pitest.properties`, and resolves it there as `build.jenesis.plugin.pitest`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `targetClasses` | comma-separated globs of the classes to mutate; `*` by default |
| `targetTests` | comma-separated globs of the tests to run against the mutants; `*` by default |
| `outputFormats` | comma-separated report formats: `XML` (the default), `HTML`, `CSV` |
| `mutators` | comma-separated mutators or mutator groups of PIT, such as `STRONGER` |
| `failWhenNoMutations` | `true` to fail when PIT finds nothing to mutate; `false` by default |

Any other value is refused.

It reads no file. PIT runs the tests with the JUnit Platform launcher of the version the test module pins for
`org.junit.platform/junit-platform-engine` or `junit-platform-commons`, or else of the version its tests resolved.

`-Djenesis.mutate.pitest=false` switches it off.
