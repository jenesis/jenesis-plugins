/**
 * Jenesis OpenAPI plugin
 *
 * Generates Java sources from the OpenAPI specification of a module with OpenAPI Generator, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 */
module build.jenesis.plugin.openapi {

    requires build.jenesis;

    exports build.jenesis.plugin.openapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openapi.OpenApiFactory;
}
