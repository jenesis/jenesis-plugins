# PIT

Runs PIT mutation testing over the code a test module tests, reporting which mutants its tests kill.

```properties
# jenesis.plugins.properties
pitest+observed=build.jenesis.plugin.pitest
```

```properties
# build.jenesis/plugin-pitest.properties
targetClasses=com.example.*
targetTests=com.example.test.*
outputFormats=XML,HTML
```

| Value | Meaning |
|---|---|
| `targetClasses` | comma-separated globs of the classes to mutate; `*` by default |
| `targetTests` | comma-separated globs of the tests to run against the mutants; `*` by default |
| `outputFormats` | comma-separated report formats: `XML` (the default), `HTML`, `CSV` |
| `mutators` | comma-separated mutators or mutator groups of PIT, such as `STRONGER` |
| `failWhenNoMutations` | `true` to fail when PIT finds nothing to mutate; `false` by default |

Any other value is refused.

It runs in each test module where its `plugin-pitest.properties` is found, and reads no file. PIT runs the tests
with the JUnit Platform launcher of the version the test module pins for `org.junit.platform/junit-platform-engine`
or `junit-platform-commons`, as `pin` writes it, so a test module that pins neither gives PIT no launcher.
