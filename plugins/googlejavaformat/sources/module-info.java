/**
 * Jenesis google-java-format plugin
 *
 * Formats or verifies the Java sources of a module with google-java-format in the style and with the options a
 * project names, resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 */
module build.jenesis.plugin.googlejavaformat {

    requires build.jenesis;

    exports build.jenesis.plugin.googlejavaformat;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.googlejavaformat.GoogleJavaFormatFactory;
}
