/**
 * Jenesis KSP plugin
 *
 * Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module
 * names, resolving KSP from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.ksp {

    requires build.jenesis;

    exports build.jenesis.plugin.ksp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ksp.KspFactory;
}
