/**
 * Jenesis japicmp plugin
 *
 * Compares the API of a module with the release it names as its baseline using japicmp, reporting or failing on the
 * changes its japicmp.properties selects, resolving the tool from the repositories of the build that loads the
 * plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/8aa734d9f8633331c72caa652f166ba571afa066bf11cb89f1205a4e07c7c913
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/8aa734d9f8633331c72caa652f166ba571afa066bf11cb89f1205a4e07c7c913
 */
module build.jenesis.plugin.japicmp {

    requires build.jenesis;

    exports build.jenesis.plugin.japicmp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.japicmp.JApiCmpFactory;
}
