/**
 * Jenesis PIT plugin
 *
 * Runs PIT mutation testing over the code a test module tests, resolving PIT and its JUnit Platform plugin from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bc3a7e8556953186504a48bde1b664debea589f65021116e71ef965e2ae24990
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bc3a7e8556953186504a48bde1b664debea589f65021116e71ef965e2ae24990
 */
module build.jenesis.plugin.pitest {

    requires build.jenesis;

    exports build.jenesis.plugin.pitest;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.pitest.PiTestFactory;
}
