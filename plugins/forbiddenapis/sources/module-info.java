/**
 * Jenesis forbiddenapis plugin
 *
 * Checks the compiled classes of a module with forbiddenapis against bundled signatures and the signatures files it is
 * handed, resolving forbiddenapis from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias de.thetaphi.forbiddenapis de.thetaphi/forbiddenapis
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 * @jenesis.pin de.thetaphi/forbiddenapis 3.11 SHA-256/2d9243b165c566a5ca140be8dfd11ce7bf4ce00e07ccdc31d2c0719cd061c9c6
 */
module build.jenesis.plugin.forbiddenapis {

    requires build.jenesis;
    requires static de.thetaphi.forbiddenapis;

    exports build.jenesis.plugin.forbiddenapis;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;
}
