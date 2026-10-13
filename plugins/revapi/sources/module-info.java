/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/8aa734d9f8633331c72caa652f166ba571afa066bf11cb89f1205a4e07c7c913
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/8aa734d9f8633331c72caa652f166ba571afa066bf11cb89f1205a4e07c7c913
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
