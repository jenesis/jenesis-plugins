/**
 * Jenesis KSP plugin
 *
 * Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module
 * names, resolving KSP from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 */
module build.jenesis.plugin.ksp {

    requires build.jenesis;

    exports build.jenesis.plugin.ksp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ksp.KspFactory;
}
