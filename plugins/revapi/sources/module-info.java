/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * plugin-revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
