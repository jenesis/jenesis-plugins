/**
 * Jenesis CodeNarc plugin
 *
 * Checks the Groovy sources of a module with CodeNarc against its rule set, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 */
module build.jenesis.plugin.codenarc {

    requires build.jenesis;

    exports build.jenesis.plugin.codenarc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.codenarc.CodeNarcFactory;
}
