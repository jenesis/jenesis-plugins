# Kotlin

Compiles the Kotlin sources of a module with kotlinc ahead of javac and documents them with Dokka.

```properties
# build.jenesis/dokka.properties
module-name=Example
documented-visibilities=public,protected
external-link.coroutines=https://kotlinlang.org/api/kotlinx.coroutines
```

The build discovers the plugin by itself in every module that holds a `.kt` source, and resolves it there as
`build.jenesis.plugin.kotlin`; a build compiles no Kotlin without it. Dokka takes its values from the lines of a
`dokka.properties` in a configuration folder, if one is there:

| Value | Meaning |
|---|---|
| `module-name` | the name Dokka documents the module by, `documentation` by default |
| `format` | `html` (the default) or `javadoc` |
| `documented-visibilities` | the visibilities to document, a comma-separated list of `public`, `protected`, `internal`, `private` and `package` |
| `suppress-obvious-functions` | `true` (the default) leaves out the functions every class has |
| `suppress-inherited-members` | `true` leaves out the members a type inherits, `false` by default |
| `skip-empty-packages` | `true` (the default) leaves out a package with nothing documented |
| `skip-deprecated` | `true` leaves out what is deprecated, `false` by default |
| `report-undocumented` | `true` warns of every undocumented declaration, `false` by default |
| `fail-on-warning` | `true` fails the build on a warning of Dokka, `false` by default |
| `suppressed-packages` | regular expressions of the packages to leave out, comma-separated |
| `jdk-version` | the feature release of the JDK whose documentation Dokka links to, such as `17` |
| `source-link` | the URL of the source folder each declaration links to |
| `source-link-suffix` | what precedes the line number in such a link, `#L` by default |
| `footer-message` | the footer of every page |
| `external-link.<name>` | the URL of documentation to link to, its `package-list` read beside it |
| `external-link.<name>.package-list` | the URL of that `package-list` where it lies elsewhere |

Any other value is refused.

It reads nothing a project binds, so Dokka's includes, custom assets and custom styles have no value. The compiler
is handed the module's dependencies alone, `kotlin-stdlib` among them where the module declares it, compiles for the
release javac compiles for, compiles a source below `META-INF/versions/<N>/` for release `<N>`, and lets the tests of
a module see its `internal` declarations. The lines of `process-kotlinc.properties` reach kotlinc, a `-J` line its
JVM, and the build places the resources of the module. Under `-Djenesis.process.mode=daemon`, every compilation
runs side by side with the others in one JVM the build keeps warm, which a `-J` line configures.
`-Djenesis.kotlin.incremental=true` compiles incrementally through the Kotlin Build Tools API of Kotlin 2.3 or newer. The compiler resolves in the tool group of the plugin, `kotlin:tool/…`, and Dokka,
which runs on a Kotlin of its own, in `kotlin:dokka/…`. A compiler plugin is named as
`@jenesis.plugin kotlin:tool <coordinate>`.
