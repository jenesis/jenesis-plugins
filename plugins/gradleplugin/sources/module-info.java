/**
 * Jenesis Gradle plugin plugin
 *
 * Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID from
 * the Maven repository the project is published to.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 */
module build.jenesis.plugin.gradleplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.gradleplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.gradleplugin.GradlePluginFactory;
}
