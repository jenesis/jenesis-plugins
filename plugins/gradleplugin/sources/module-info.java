/**
 * Jenesis Gradle plugin plugin
 *
 * Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID from
 * the Maven repository the project is published to.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 */
module build.jenesis.plugin.gradleplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.gradleplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.gradleplugin.GradlePluginFactory;
}
