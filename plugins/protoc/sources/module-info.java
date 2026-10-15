/**
 * Jenesis protoc plugin
 *
 * Generates Java sources from the Protocol Buffers definitions of a module with protoc and the protoc plugins it
 * names, resolving the tool from the repositories of the build that loads the plugin and running it in a JVM of its
 * own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/3a267b99570ff390b97e5ef9e2d5d032466a5fd28bcec6161fd6a45e3d61df56
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/3a267b99570ff390b97e5ef9e2d5d032466a5fd28bcec6161fd6a45e3d61df56
 */
module build.jenesis.plugin.protoc {

    requires build.jenesis;

    exports build.jenesis.plugin.protoc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.protoc.ProtocFactory;
}
