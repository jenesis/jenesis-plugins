/**
 * Jenesis detekt plugin
 *
 * Checks the Kotlin sources of a module with detekt against its detekt.yml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 */
module build.jenesis.plugin.detekt {

    requires build.jenesis;

    exports build.jenesis.plugin.detekt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.detekt.DetektFactory;
}
