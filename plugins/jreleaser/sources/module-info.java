/**
 * Jenesis JReleaser plugin
 *
 * Releases what the build staged with JReleaser, loaded by a class loader of its own and run in the build's JVM rather
 * than as a command, in the project's root and with the version, the metadata and the staged folders the build
 * resolved, resolving JReleaser from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 */
module build.jenesis.plugin.jreleaser {

    requires build.jenesis;

    exports build.jenesis.plugin.jreleaser;

    uses java.util.spi.ToolProvider;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.jreleaser.JReleaserFactory;
}
