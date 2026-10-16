/**
 * Jenesis JReleaser plugin
 *
 * Releases what the build staged with JReleaser, loaded by a class loader of its own and run in the build's JVM rather
 * than as a command, in the project's root and with the version, the metadata and the staged folders the build
 * resolved, resolving JReleaser from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 */
module build.jenesis.plugin.jreleaser {

    requires build.jenesis;

    exports build.jenesis.plugin.jreleaser;

    uses java.util.spi.ToolProvider;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.jreleaser.JReleaserFactory;
}
