# Spotless

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the files of a module that each set of files selects,
with the [Spotless](https://github.com/diffplug/spotless) steps the set names.

```properties
# build.jenesis/spotless.properties
json.target=*.json
json.steps=jacksonJson,endWithNewline
markdown.target=*.md
markdown.steps=flexmark,trimTrailingWhitespace,endWithNewline
java.target=*.java
java.steps=removeUnusedImports,googleJavaFormat
java.googleJavaFormat.version=1.28.0
java.googleJavaFormat.style=AOSP
```

The build discovers the plugin by itself in every module where a configuration folder holds a `spotless.properties`,
and resolves it there as `build.jenesis.plugin.spotless`. A set reads the module's sources and resources alike. The
lines of the file name sets of files, each a name of lower case letters, digits and hyphens:

| Value | Meaning |
|---|---|
| `<set>.target` | comma-separated globs a file of the module's sources or resources matches, against its path from any folder down, as `*.json` or `config/*.yaml` |
| `<set>.steps` | comma-separated steps applied in order to every file the set matches |
| `<set>.<step>.<option>` | an option of one of the set's steps |

A file several sets match runs through each set in turn. The steps and their options:

| Step | Options | Tool |
|---|---|---|
| `trimTrailingWhitespace` | | |
| `endWithNewline` | | |
| `leadingTabsToSpaces` | `size`, `4` by default | |
| `leadingSpacesToTabs` | `size`, `4` by default | |
| `replaceRegex` | `regex` (required), `replacement`, empty by default | |
| `importOrder` | `order`, comma-separated import groups | |
| `formatAnnotations` | | |
| `removeUnusedImports` | `version` | `com.google.googlejavaformat:google-java-format` |
| `googleJavaFormat` | `version`, `style` (`GOOGLE` or `AOSP`), `reflowLongStrings`, `reorderImports`, `formatJavadoc` | `com.google.googlejavaformat:google-java-format` |
| `palantirJavaFormat` | `version`, `style` (`PALANTIR`, `AOSP` or `GOOGLE`), `formatJavadoc` | `com.palantir.javaformat:palantir-java-format` |
| `jacksonJson` | `version` | `com.fasterxml.jackson.core:jackson-databind` |
| `jacksonYaml` | `version` | `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` |
| `flexmark` | `version` | `com.vladsch.flexmark:flexmark-all` |

An option left out takes Spotless's default, and a flag named with no value is `true`. Any other key, step or
option is refused, naming those it takes, as is a step asked for at two versions. A set running `googleJavaFormat` or
`palantirJavaFormat` makes Spotless a Java formatter, and a build that finds the properties of a second one fails,
naming both. Spotless, `com.diffplug.spotless:spotless-lib`, resolves in `spotless:tool/…`, and the tool of each step in
a group named after the step, `spotless:googlejavaformat/…`, so no two tools share a version; a tool floats on its newest
release unless `version` names one, and is pinned like any other. A step that needs no tool resolves nothing. Steps
that need Node.js or a program of the platform are not offered.

It reads no file but those its sets select.

`-Djenesis.format.spotless=false` switches it off.
