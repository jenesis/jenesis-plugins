/**
 * Jenesis Gradle plugin plugin
 *
 * Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID from
 * the Maven repository the project is published to.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.gradleplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.gradleplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.gradleplugin.GradlePluginFactory;
}
