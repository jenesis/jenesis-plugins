/**
 * Jenesis CodeNarc plugin
 *
 * Checks the Groovy sources of a module with CodeNarc against its rule set, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/1040b44171cc040da654969e21e63e5a7c5487d7bc5adeb5b53f155c5110be6d
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/1040b44171cc040da654969e21e63e5a7c5487d7bc5adeb5b53f155c5110be6d
 */
module build.jenesis.plugin.codenarc {

    requires build.jenesis;

    exports build.jenesis.plugin.codenarc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.codenarc.CodeNarcFactory;
}
