/**
 * Jenesis Eclipse formatter plugin
 *
 * Formats or verifies the Java sources of a module with the Eclipse formatter and the profile a project binds,
 * resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 */
module build.jenesis.plugin.eclipse {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.eclipse;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;
}
