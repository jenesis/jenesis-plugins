/**
 * Jenesis protoc plugin
 *
 * Generates Java sources from the Protocol Buffers definitions of a module with protoc and the protoc plugins it
 * names, resolving the tool from the repositories of the build that loads the plugin and running it in a JVM of its
 * own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/dc240f752cad6a82219a6e1e745b51a1b4e5794a568d0197fef4c5282668ad8d
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/dc240f752cad6a82219a6e1e745b51a1b4e5794a568d0197fef4c5282668ad8d
 */
module build.jenesis.plugin.protoc {

    requires build.jenesis;

    exports build.jenesis.plugin.protoc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.protoc.ProtocFactory;
}
