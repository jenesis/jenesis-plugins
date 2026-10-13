/**
 * Jenesis Eclipse formatter plugin
 *
 * Formats or verifies the Java sources of a module with the Eclipse formatter and the profile a project binds,
 * resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 */
module build.jenesis.plugin.eclipse {

    requires build.jenesis;

    exports build.jenesis.plugin.eclipse;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;
}
