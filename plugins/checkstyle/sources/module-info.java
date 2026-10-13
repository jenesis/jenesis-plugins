/**
 * Jenesis Checkstyle plugin
 *
 * Checks the Java sources of a module with Checkstyle against its checkstyle.xml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 */
module build.jenesis.plugin.checkstyle {

    requires build.jenesis;

    exports build.jenesis.plugin.checkstyle;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.checkstyle.CheckstyleFactory;
}
