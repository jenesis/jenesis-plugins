/**
 * Jenesis PIT plugin
 *
 * Runs PIT mutation testing over the code a test module tests, resolving PIT and its JUnit Platform plugin from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 */
module build.jenesis.plugin.pitest {

    requires build.jenesis;

    exports build.jenesis.plugin.pitest;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.pitest.PiTestFactory;
}
