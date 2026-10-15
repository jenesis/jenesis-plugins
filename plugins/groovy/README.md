# Groovy

Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
them with Groovydoc.

The build discovers the plugin by itself in every module that holds a `.groovy` source, and resolves it there as
`build.jenesis.plugin.groovy`; no file switches it on, and a build compiles no Groovy without it. It takes no values.

It reads nothing a project binds: groovyc compiles the module's `.groovy` sources, and Groovydoc documents them, and
the module's Java sources beside them wherever the module holds Java, at the root of the module's documentation, or
in a `groovy/` folder of it beside the Javadoc of the module's Java sources. Groovy and Groovydoc resolve in the tool
group of the plugin, `groovy:tool/…`, and a `process-groovyc.properties` or `process-groovydoc.properties` configures
the JVM each runs in.
