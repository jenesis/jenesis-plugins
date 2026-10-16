/**
 * Jenesis Checkstyle plugin
 *
 * Checks the Java sources of a module with Checkstyle against its checkstyle.xml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 */
module build.jenesis.plugin.checkstyle {

    requires build.jenesis;

    exports build.jenesis.plugin.checkstyle;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.checkstyle.CheckstyleFactory;
}
