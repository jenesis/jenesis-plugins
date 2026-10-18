/**
 * Jenesis forbiddenapis plugin
 *
 * Checks the compiled classes of a module with forbiddenapis against bundled signatures and the signatures files it is
 * handed, resolving forbiddenapis from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias de.thetaphi.forbiddenapis de.thetaphi/forbiddenapis
 * @jenesis.pin build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 * @jenesis.pin de.thetaphi/forbiddenapis 3.11 SHA-256/2d9243b165c566a5ca140be8dfd11ce7bf4ce00e07ccdc31d2c0719cd061c9c6
 */
module build.jenesis.plugin.forbiddenapis {

    requires build.jenesis;
    requires static de.thetaphi.forbiddenapis;

    exports build.jenesis.plugin.forbiddenapis;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;
}
