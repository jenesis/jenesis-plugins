# JaCoCo

Measures the code coverage of a module's tests with the JaCoCo agent and reports it, failing the build where a limit
is not met.

```properties
# build.jenesis/jacoco.properties
minimum.line=0.8
maximum.missed.branch=3
```

The build discovers the plugin by itself in every test module where a configuration folder holds a
`jacoco.properties`, and resolves it there as `build.jenesis.plugin.jacoco`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `minimum.<counter>` | the least covered ratio, from `0` to `1` |
| `maximum.missed.<counter>` | the most items left uncovered, a whole number |

A counter is `instruction`, `branch`, `line`, `complexity`, `method` or `class`. Any other value is refused.

The report covers the module the tests name by `@jenesis.test`, or the artifact a Maven layout's tests belong to, and
not the tests themselves; the limits are checked against that module alone. A test module that names none has every
module of the project it requires covered. The report shows the source of every class it covers whose sources the
build hands the tests' observers.

It reads nothing a project binds: the agent joins the JVM the tests run in, and the report reads what it recorded.
The report's tool resolves in the tool group of the plugin, `jacoco:tool/…`, and the agent the tests run with in
`jacoco:agent/…`, at the release of JaCoCo the tool group pins.

`-Djenesis.observe.jacoco=false` switches it off.
