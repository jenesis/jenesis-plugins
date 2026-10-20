/**
 * Jenesis detekt plugin
 *
 * Checks the Kotlin sources of a module with detekt against its detekt.yml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 */
module build.jenesis.plugin.detekt {

    requires build.jenesis;

    exports build.jenesis.plugin.detekt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.detekt.DetektFactory;
}
