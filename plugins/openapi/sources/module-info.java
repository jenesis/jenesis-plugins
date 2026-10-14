/**
 * Jenesis OpenAPI plugin
 *
 * Generates Java sources from the OpenAPI specification of a module with OpenAPI Generator, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 */
module build.jenesis.plugin.openapi {

    requires build.jenesis;

    exports build.jenesis.plugin.openapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openapi.OpenApiFactory;
}
