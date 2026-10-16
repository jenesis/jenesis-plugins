/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * plugin-revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
