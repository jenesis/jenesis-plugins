/**
 * Jenesis Gradle plugin plugin
 *
 * Stages the marker of every Gradle plugin a staged jar declares, so that a build resolves the plugin by its ID from
 * the Maven repository the project is published to.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 */
module build.jenesis.plugin.gradleplugin {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.gradleplugin;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.gradleplugin.GradlePluginFactory;
}
