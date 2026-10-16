/**
 * Jenesis Scala plugin
 *
 * Compiles the Scala sources of a module with the Scala 3 compiler before javac, jointly with its Java sources, and
 * documents them with Scaladoc, resolving the compiler, Scaladoc and, for an incremental compile, Zinc from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/dc240f752cad6a82219a6e1e745b51a1b4e5794a568d0197fef4c5282668ad8d
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/dc240f752cad6a82219a6e1e745b51a1b4e5794a568d0197fef4c5282668ad8d
 */
module build.jenesis.plugin.scala {

    requires build.jenesis;

    exports build.jenesis.plugin.scala;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.scala.ScalaFactory;
}
