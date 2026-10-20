/**
 * Jenesis Maven plugin plugin
 *
 * Writes the plugin descriptor of a Maven plugin into the classes of a module, with the descriptor generator of Maven's
 * plugin tools over the module's classes, sources and dependencies, resolving the tools from the repositories of the
 * build that loads the plugin and running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.mavenplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.mavenplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.mavenplugin.MavenPluginFactory;
}
