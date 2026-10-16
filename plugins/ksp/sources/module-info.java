/**
 * Jenesis KSP plugin
 *
 * Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module
 * names, resolving KSP from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 */
module build.jenesis.plugin.ksp {

    requires build.jenesis;

    exports build.jenesis.plugin.ksp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ksp.KspFactory;
}
