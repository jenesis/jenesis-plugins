# JaCoCo

Measures the code coverage of a module's tests with the JaCoCo agent and reports it, failing the build where a limit
is not met.

```properties
# jenesis.plugins.properties
jacoco+observe=build.jenesis.plugin.jacoco
```

```properties
# build.jenesis/plugin-jacoco.properties
minimum.line=0.8
maximum.missed.branch=3
```

| Value | Meaning |
|---|---|
| `minimum.<counter>` | the least covered ratio, from `0` to `1` |
| `maximum.missed.<counter>` | the most items left uncovered, a whole number |

A counter is `instruction`, `branch`, `line`, `complexity`, `method` or `class`. Any other value is refused.

It reads nothing a project binds: the agent joins the JVM the tests run in, and the report reads what it recorded.
The report's tool resolves in the tool group of the plugin, `jacoco:tool/…`, and the agent the tests run with in
`jacoco:agent/…`, at the release of JaCoCo the tool group pins.
