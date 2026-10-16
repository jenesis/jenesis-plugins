/**
 * Jenesis JReleaser plugin
 *
 * Releases what the build staged with JReleaser, loaded by a class loader of its own and run in the build's JVM rather
 * than as a command, in the project's root and with the version, the metadata and the staged folders the build
 * resolved, resolving JReleaser from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/6c7ca48b61d71ca04a929bfc937f49ca5ba899c083b0a011fc29115d44b522c4
 */
module build.jenesis.plugin.jreleaser {

    requires build.jenesis;

    exports build.jenesis.plugin.jreleaser;

    uses java.util.spi.ToolProvider;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.jreleaser.JReleaserFactory;
}
