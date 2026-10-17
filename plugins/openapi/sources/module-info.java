/**
 * Jenesis OpenAPI plugin
 *
 * Generates Java sources from the OpenAPI specification of a module with OpenAPI Generator, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 */
module build.jenesis.plugin.openapi {

    requires build.jenesis;

    exports build.jenesis.plugin.openapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openapi.OpenApiFactory;
}
