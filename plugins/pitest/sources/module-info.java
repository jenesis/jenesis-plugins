/**
 * Jenesis PIT plugin
 *
 * Runs PIT mutation testing over the code a test module tests, resolving PIT and its JUnit Platform plugin from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 */
module build.jenesis.plugin.pitest {

    requires build.jenesis;

    exports build.jenesis.plugin.pitest;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.pitest.PiTestFactory;
}
