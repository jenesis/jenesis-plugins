/**
 * Jenesis Gradle plugin plugin
 *
 * Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID from
 * the Maven repository the project is published to.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.gradleplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.gradleplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.gradleplugin.GradlePluginFactory;
}
