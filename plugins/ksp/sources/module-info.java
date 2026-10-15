/**
 * Jenesis KSP plugin
 *
 * Runs Kotlin Symbol Processing over the Kotlin and Java sources of a module with the symbol processors the module
 * names, resolving KSP from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 */
module build.jenesis.plugin.ksp {

    requires build.jenesis;

    exports build.jenesis.plugin.ksp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ksp.KspFactory;
}
