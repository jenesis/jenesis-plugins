/**
 * Jenesis JReleaser plugin
 *
 * Releases what the build staged with JReleaser, loaded by a class loader of its own and run in the build's JVM rather
 * than as a command, in the project's root and with the version, the metadata and the staged folders the build
 * resolved, resolving JReleaser from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 */
module build.jenesis.plugin.jreleaser {

    requires build.jenesis;

    exports build.jenesis.plugin.jreleaser;

    uses java.util.spi.ToolProvider;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.jreleaser.JReleaserFactory;
}
