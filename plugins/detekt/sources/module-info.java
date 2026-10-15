/**
 * Jenesis detekt plugin
 *
 * Checks the Kotlin sources of a module with detekt against its detekt.yml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 */
module build.jenesis.plugin.detekt {

    requires build.jenesis;

    exports build.jenesis.plugin.detekt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.detekt.DetektFactory;
}
