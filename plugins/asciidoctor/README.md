# Asciidoctor

Converts the AsciiDoc documents of a project to HTML with AsciidoctorJ and stages them with the files that belong to
no module.

```properties
# build.jenesis/asciidoctor.properties
attribute.toc=left
```

The build discovers the plugin by itself where the project's configuration folder holds an `asciidoctor.properties`,
and resolves it as `build.jenesis.plugin.asciidoctor`. It runs once for the whole project, after every module is
built, and binds the project's `docs/` folder unless a line `@docs=<folder>` binds another. The lines of the file are
its values:

| Value | Meaning |
|---|---|
| `backend` | `html5` (default) or `docbook5` |
| `output` | the folder below the staged project files the documents are written to, `docs` by default |
| `attribute.<name>` | an AsciiDoc attribute every document reads, such as `attribute.product-version=1.2.3` |

Any other value is refused.

Every `.adoc` file is converted keeping its folder, so `docs/guide/start.adoc` becomes
`target/stage/project/output/docs/guide/start.html`; a file or folder whose name starts with `_` is a partial that is
only included. Every other file of the folder, an image or a stylesheet, is copied as it stands. The output names
neither the time nor the version it was converted with, and a document that includes what does not exist fails the
build.

AsciidoctorJ resolves in the tool group of the plugin, `asciidoctor:tool/…`, and `pin` writes its closure, JRuby
included, into `jenesis.plugins.pin.properties`. `-Djenesis.project.asciidoctor=false` switches it off.
