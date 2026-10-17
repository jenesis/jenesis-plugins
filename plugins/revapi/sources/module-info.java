/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * plugin-revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
