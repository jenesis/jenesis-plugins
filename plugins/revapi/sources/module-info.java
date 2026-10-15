/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * plugin-revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
