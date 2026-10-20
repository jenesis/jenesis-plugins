/**
 * Jenesis Maven plugin plugin
 *
 * Writes the plugin descriptor of a Maven plugin into the classes of a module, with the descriptor generator of Maven's
 * plugin tools over the module's classes, sources and dependencies, resolving the tools from the repositories of the
 * build that loads the plugin and running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 */
module build.jenesis.plugin.mavenplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.mavenplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.mavenplugin.MavenPluginFactory;
}
