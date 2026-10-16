/**
 * Jenesis Scalastyle plugin
 *
 * Checks the Scala sources of a module with Scalastyle against its scalastyle-config.xml, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 */
module build.jenesis.plugin.scalastyle {

    requires build.jenesis;

    exports build.jenesis.plugin.scalastyle;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.scalastyle.ScalastyleFactory;
}
