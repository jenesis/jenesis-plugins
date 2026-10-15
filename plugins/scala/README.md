# Scala

Compiles the Scala sources of a module with the Scala 3 compiler before javac, jointly with its Java sources, and
documents them with Scaladoc.

The build discovers the plugin by itself in every module that holds a `.scala` source, and resolves it there as
`build.jenesis.plugin.scala`; no file switches it on. It takes no values.

| Setting | Meaning |
|---|---|
| `-Djenesis.scala.incremental=true` | compiles with Zinc, in a JVM forked per compile, recompiling only what a change reaches |
| `-Djenesis.print.scalac`, `-Djenesis.print.scaladoc` | streams the tool's output |

The compiler reads the `.scala` sources and, for their symbols, the `.java` sources beside them other than
`module-info.java`; javac then compiles the Java sources against the classes it wrote. The `--release` of a
`process-javac.properties` is handed on as `-release`, and the lines of `process-scalac.properties` and
`process-scaladoc.properties` configure the JVM each tool runs in. Scaladoc renders the module's documentation under
`-Djenesis.project.documentation=true`, beside javadoc in a folder `scala/` where the module also holds Java.

It reads nothing a project binds. The compiler resolves as `org.scala-lang:scala3-compiler_3` in the tool group
of the plugin, `scala:tool/…`, with its `scala3-sbt-bridge` there too when compiling incrementally, Zinc as
`org.scala-sbt:zinc_2.13` in `scala:zinc/…`, and Scaladoc as `org.scala-lang:scaladoc_3` in `scala:scaladoc/…`.
