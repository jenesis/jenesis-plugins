/**
 * Jenesis CodeNarc plugin
 *
 * Checks the Groovy sources of a module with CodeNarc against its rule set, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 */
module build.jenesis.plugin.codenarc {

    requires build.jenesis;

    exports build.jenesis.plugin.codenarc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.codenarc.CodeNarcFactory;
}
