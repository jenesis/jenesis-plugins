# async-profiler

Profiles a module's tests with async-profiler, attached as an agent to the JVM the tests run in, and writes what it
sampled to the reports of the test step.

```properties
# build.jenesis/asyncprofiler.properties
event=cpu
interval=1ms
threads
```

The build discovers the plugin by itself in every test module where a configuration folder holds an
`asyncprofiler.properties`, and resolves it there as `build.jenesis.plugin.asyncprofiler`. A profile folder of the
configuration folder, `build.jenesis/<profile>/asyncprofiler.properties`, profiles only the builds run with
`-Djenesis.make.profiles=<profile>`. The lines of the file are its values:

| Value | Meaning |
|---|---|
| `<option>=<value>` | an option of async-profiler, such as `event=cpu`, `interval=1ms` or `alloc=512k` |
| `<option>` | an option of async-profiler without a value, such as `threads` or `simple` |

A value holds no comma. The build names the file the profile is written to, so `file` is refused: it writes
`reports/asyncprofiler/tests.html`, a flame graph, or `tests.jfr`, `tests.collapsed`, `tests.txt` or `tests.otlp`
where an option selects that format (`jfr`, `collapsed`, `flat`, `traces`, `summary`, `otlp`).

It reads nothing a project binds. The agent the tests run with, `tools.profiler:async-profiler`, resolves in
`asyncprofiler:agent/…`, and the tests' JVM is granted native access for it. async-profiler ships its library for
Linux on x64 and arm64 and for macOS; elsewhere the plugin fails the build.

`-Djenesis.observe.asyncprofiler=false` switches it off.
