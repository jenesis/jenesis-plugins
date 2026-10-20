/**
 * Jenesis Maven plugin plugin
 *
 * Writes the plugin descriptor of a Maven plugin into the classes of a module, with the descriptor generator of Maven's
 * plugin tools over the module's classes, sources and dependencies, resolving the tools from the repositories of the
 * build that loads the plugin and running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 */
module build.jenesis.plugin.mavenplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.mavenplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.mavenplugin.MavenPluginFactory;
}
