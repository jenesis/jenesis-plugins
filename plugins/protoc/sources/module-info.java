/**
 * Jenesis protoc plugin
 *
 * Generates Java sources from the Protocol Buffers definitions of a module with protoc and the protoc plugins it
 * names, resolving the tool from the repositories of the build that loads the plugin and running it in a JVM of its
 * own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/90ddecb09656798a0c541ef6f11764b4f5b23092c6f9f36f9235310640b5fb4f
 */
module build.jenesis.plugin.protoc {

    requires build.jenesis;

    exports build.jenesis.plugin.protoc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.protoc.ProtocFactory;
}
