/**
 * Jenesis Checkstyle plugin
 *
 * Checks the Java sources of a module with Checkstyle against its checkstyle.xml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 */
module build.jenesis.plugin.checkstyle {

    requires build.jenesis;

    exports build.jenesis.plugin.checkstyle;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.checkstyle.CheckstyleFactory;
}
