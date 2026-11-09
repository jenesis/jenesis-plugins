/**
 * Jenesis forbiddenapis plugin
 *
 * Checks the compiled classes of a module with forbiddenapis against bundled signatures and the signatures files it is
 * handed, resolving forbiddenapis from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias de.thetaphi.forbiddenapis de.thetaphi/forbiddenapis
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin de.thetaphi/forbiddenapis 3.11 SHA-256/2d9243b165c566a5ca140be8dfd11ce7bf4ce00e07ccdc31d2c0719cd061c9c6
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 * @jenesis.pin maven/de.thetaphi/forbiddenapis/pom 3.11 SHA-256/ff6e17eef5447cd8dbbe14bc502d962cc02175a045fa4a9485a937bd514ed9b7
 */
module build.jenesis.plugin.forbiddenapis {

    requires build.jenesis;
    requires static de.thetaphi.forbiddenapis;

    exports build.jenesis.plugin.forbiddenapis;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;
}
