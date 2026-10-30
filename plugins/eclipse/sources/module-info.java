/**
 * Jenesis Eclipse formatter plugin
 *
 * Formats or verifies the Java sources of a module with the Eclipse formatter and the profile a project binds,
 * resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 */
module build.jenesis.plugin.eclipse {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.eclipse;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;
}
