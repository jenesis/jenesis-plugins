/**
 * Jenesis japicmp plugin
 *
 * Compares the API of a module with the release it names as its baseline using japicmp, reporting or failing on the
 * changes its plugin-japicmp.properties selects, resolving the tool from the repositories of the build that loads the
 * plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 */
module build.jenesis.plugin.japicmp {

    requires build.jenesis;

    exports build.jenesis.plugin.japicmp;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.japicmp.JApiCmpFactory;
}
