/**
 * Jenesis PIT plugin
 *
 * Runs PIT mutation testing over the code a test module tests, resolving PIT and its JUnit Platform plugin from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/0a293bd0f8fe09fac0f19d7f13950b4db3799c5c9a3a6ffa78999b22431d265a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/0a293bd0f8fe09fac0f19d7f13950b4db3799c5c9a3a6ffa78999b22431d265a
 */
module build.jenesis.plugin.pitest {

    requires build.jenesis;

    exports build.jenesis.plugin.pitest;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.pitest.PiTestFactory;
}
