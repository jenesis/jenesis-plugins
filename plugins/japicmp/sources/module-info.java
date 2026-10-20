/**
 * Jenesis japicmp plugin
 *
 * Compares the API of a module with the release it names as its baseline using japicmp, reporting or failing on the
 * changes its japicmp.properties selects, resolving the tool from the repositories of the build that loads the
 * plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.japicmp {

    requires build.jenesis;

    exports build.jenesis.plugin.japicmp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.japicmp.JApiCmpFactory;
}
