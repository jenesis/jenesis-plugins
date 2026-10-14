# JReleaser

Releases what the build staged with JReleaser, run in the build's own JVM rather than as a command.

```properties
# jenesis.plugins.properties
jreleaser+release=build.jenesis.plugin.jreleaser
```

```properties
# build.jenesis/release/plugin-jreleaser.properties, active with -Djenesis.make.profiles=release
@jreleaser=jreleaser.yml
dry=false
```

| Value | Meaning |
|---|---|
| `command` | the JReleaser command to run, `full-release` by default |
| `dry` | `true` (the default) runs it with `--dry-run`, `false` releases |
| `JRELEASER_*` | a setting JReleaser reads, such as `JRELEASER_GITHUB_TOKEN=@github.token` |

Any other value is refused.

It reads the `jreleaser.yml`, `.yaml`, `.toml` or `.json` the project binds with an `@<input>[/<target>]=<path>` line.
A releaser runs only where a configuration folder holds its `plugin-jreleaser.properties`, so placing the file in a
profile's folder keeps every build but a release from running it.

JReleaser runs in the project's root with the version the build resolved, `-Djenesis.project.version` included. Every
value of the project's resolved metadata and the folder of every staged tree are template properties of the
configuration, named in camel case after a `jenesis` prefix: `{{ jenesisStageMaven }}` is the staged Maven tree and
`{{ jenesisScmUrl }}` the `scm.url` of the metadata. They apply wherever JReleaser expands a template, which a deployer's
`stagingRepositories` is not: name the staged Maven tree there relative to the project's root, as
`target/stage/maven/output`. A `JRELEASER_*` value, the version among them, is handed to JReleaser
as its settings file, which then stands in for `~/.jreleaser/config.properties`; JReleaser still reads its `JRELEASER_*`
environment variables. Its output lands in the step's own folder, never in the project.
JReleaser resolves in the group named after the plugin, `jreleaser/…`, and `pin` pins it in
`jenesis.plugins.pin.properties`.
