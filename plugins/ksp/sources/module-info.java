/**
 * Jenesis KSP plugin
 *
 * Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module
 * names, resolving KSP from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d5f186f8f022d7f65ff1fb69eebf3020f57fd75d4ac24891a756ae27638fb483
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d5f186f8f022d7f65ff1fb69eebf3020f57fd75d4ac24891a756ae27638fb483
 */
module build.jenesis.plugin.ksp {

    requires build.jenesis;

    exports build.jenesis.plugin.ksp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ksp.KspFactory;
}
