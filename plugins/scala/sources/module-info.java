/**
 * Jenesis Scala plugin
 *
 * Compiles the Scala sources of a module with the Scala 3 compiler before javac, jointly with its Java sources, and
 * documents them with Scaladoc, resolving the compiler, Scaladoc and, for an incremental compile, Zinc from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/a3d9a8a361410748f3f02968ac31e8f432da7055bee68eaa030d87544f2891a3
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/a3d9a8a361410748f3f02968ac31e8f432da7055bee68eaa030d87544f2891a3
 */
module build.jenesis.plugin.scala {

    requires build.jenesis;

    exports build.jenesis.plugin.scala;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.scala.ScalaFactory;
}
