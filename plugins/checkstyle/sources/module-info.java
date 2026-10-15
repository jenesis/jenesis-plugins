/**
 * Jenesis Checkstyle plugin
 *
 * Checks the Java sources of a module with Checkstyle against its checkstyle.xml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bc3a7e8556953186504a48bde1b664debea589f65021116e71ef965e2ae24990
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bc3a7e8556953186504a48bde1b664debea589f65021116e71ef965e2ae24990
 */
module build.jenesis.plugin.checkstyle {

    requires build.jenesis;

    exports build.jenesis.plugin.checkstyle;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.checkstyle.CheckstyleFactory;
}
