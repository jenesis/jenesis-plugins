/**
 * Jenesis Maven plugin plugin
 *
 * Writes the plugin descriptor of a Maven plugin into the classes of a module, with the descriptor generator of Maven's
 * plugin tools over the module's classes, sources and dependencies, resolving the tools from the repositories of the
 * build that loads the plugin and running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 */
module build.jenesis.plugin.mavenplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.mavenplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.mavenplugin.MavenPluginFactory;
}
