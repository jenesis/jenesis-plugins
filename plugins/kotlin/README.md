# Kotlin

Compiles the Kotlin sources of a module with kotlinc ahead of javac and documents them with Dokka.

```properties
# jenesis.plugins.properties
kotlin+language=build.jenesis.plugin.kotlin
```

```properties
# build.jenesis/plugin-kotlin.properties
dokka.module-name=Example
dokka.documented-visibilities=public,protected
dokka.external-link.coroutines=https://kotlinlang.org/api/kotlinx.coroutines
```

| Value | Meaning |
|---|---|
| `dokka.module-name` | the name Dokka documents the module by, `documentation` by default |
| `dokka.format` | `html` (the default) or `javadoc` |
| `dokka.documented-visibilities` | the visibilities to document, a comma-separated list of `public`, `protected`, `internal`, `private` and `package` |
| `dokka.suppress-obvious-functions` | `true` (the default) leaves out the functions every class has |
| `dokka.suppress-inherited-members` | `true` leaves out the members a type inherits, `false` by default |
| `dokka.skip-empty-packages` | `true` (the default) leaves out a package with nothing documented |
| `dokka.skip-deprecated` | `true` leaves out what is deprecated, `false` by default |
| `dokka.report-undocumented` | `true` warns of every undocumented declaration, `false` by default |
| `dokka.fail-on-warning` | `true` fails the build on a warning of Dokka, `false` by default |
| `dokka.suppressed-packages` | regular expressions of the packages to leave out, comma-separated |
| `dokka.jdk-version` | the feature release of the JDK whose documentation Dokka links to, such as `17` |
| `dokka.source-link` | the URL of the source folder each declaration links to |
| `dokka.source-link-suffix` | what precedes the line number in such a link, `#L` by default |
| `dokka.footer-message` | the footer of every page |
| `dokka.external-link.<name>` | the URL of documentation to link to, its `package-list` read beside it |
| `dokka.external-link.<name>.package-list` | the URL of that `package-list` where it lies elsewhere |

Any other value is refused.

It runs in every module that holds a `.kt` source, with or without a `plugin-kotlin.properties`, and takes the place of
the build's own Kotlin compiler and Dokka. It reads nothing a project binds, so Dokka's includes, custom assets and
custom styles have no value. The compiler is handed the module's dependencies alone, `kotlin-stdlib` among them where
the module declares it, compiles for the release javac compiles for, compiles a source below `META-INF/versions/<N>/`
for release `<N>`, and lets the tests of a module see its `internal` declarations. The lines of
`process-kotlinc.properties` reach kotlinc, a `-J` line its JVM, and the build places the resources of the module.
`-Djenesis.kotlin.incremental=true` compiles incrementally in the Kotlin daemon, through the Kotlin Build Tools API of
Kotlin 2.3 or newer. The compiler resolves in the group named after the plugin, `kotlin/…`, and Dokka, which runs on a
Kotlin of its own, in `kotlin-dokka/…`.
