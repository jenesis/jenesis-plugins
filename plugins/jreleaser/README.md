# JReleaser

Releases what the build staged with JReleaser, run in the build's own JVM rather than as a command.

```properties
# jenesis.plugins.properties
jreleaser+release=build.jenesis.plugin.jreleaser
```

```properties
# build.jenesis/plugin-jreleaser.properties
@jreleaser=jreleaser.yml
```

| Value | Meaning |
|---|---|
| `command` | the JReleaser command to run, `full-release` by default |
| `JRELEASER_*` | a setting JReleaser reads, such as `JRELEASER_GITHUB_TOKEN=@github.token` |

Any other value is refused. JReleaser runs with `--dry-run` unless the command line passes
`-Djenesis.release.publish=true`, which no file may set:

```shell
java -Djenesis.release.publish=true -Djenesis.project.version=1.2.3 build/jenesis/Make.java release
```

It reads the `jreleaser.yml`, `.yaml`, `.toml` or `.json` the project binds with an `@<input>[/<target>]=<path>` line.
A releaser runs where a configuration folder holds its `plugin-jreleaser.properties`, and every `release` and every
`pin` runs it, dry unless the command line publishes.

JReleaser runs in the project's root with the version the build resolved, `-Djenesis.project.version` included. Every
value of the project's resolved metadata and the folder of every staged tree are template properties of the
configuration, named in camel case after a `jenesis` prefix: `{{ jenesisStageMaven }}` is the staged Maven tree and
`{{ jenesisScmUrl }}` the `scm.url` of the metadata. They apply wherever JReleaser expands a template, which a deployer's
`stagingRepositories` is not: name the staged Maven tree there relative to the project's root, as
`target/stage/maven/output`. A `JRELEASER_*` value, the version among them, is handed to JReleaser
as its settings file, which then stands in for `~/.jreleaser/config.properties`; JReleaser still reads its `JRELEASER_*`
environment variables. Its output lands in the step's own folder, never in the project.
JReleaser resolves in the tool group of the plugin, `jreleaser:tool/…`. `pin` runs last, after a dry release, so it
resolves JReleaser and writes the line that pins it, `jreleaser\:tool/maven/org.jreleaser/jreleaser-tool-provider=<version>
SHA-256/<hex>`, into `jenesis.plugins.pin.properties` with the rest. A configuration folder `pin` does not read - a
profile's it was not given - keeps JReleaser out of that run, and `pin` then keeps the line as it stands.
