/**
 * Jenesis Maven plugin plugin
 *
 * Writes the plugin descriptor of a Maven plugin into the classes of a module, with the descriptor generator of Maven's
 * plugin tools over the module's classes, sources and dependencies, resolving the tools from the repositories of the
 * build that loads the plugin and running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.mavenplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.mavenplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.mavenplugin.MavenPluginFactory;
}
