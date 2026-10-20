/**
 * Jenesis Scala plugin
 *
 * Compiles the Scala sources of a module with the Scala 3 compiler before javac, jointly with its Java sources, and
 * documents them with Scaladoc, resolving the compiler, Scaladoc and, for an incremental compile, Zinc from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.scala {

    requires build.jenesis;

    exports build.jenesis.plugin.scala;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.scala.ScalaFactory;
}
