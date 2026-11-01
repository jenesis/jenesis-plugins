/**
 * Jenesis forbiddenapis plugin
 *
 * Checks the compiled classes of a module with forbiddenapis against bundled signatures and the signatures files it is
 * handed, resolving forbiddenapis from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias de.thetaphi.forbiddenapis de.thetaphi/forbiddenapis
 * @jenesis.pin build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin build.jenesis/build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin de.thetaphi/forbiddenapis 3.11 SHA-256/2d9243b165c566a5ca140be8dfd11ce7bf4ce00e07ccdc31d2c0719cd061c9c6
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 * @jenesis.pin maven/de.thetaphi/forbiddenapis/pom 3.11 SHA-256/ff6e17eef5447cd8dbbe14bc502d962cc02175a045fa4a9485a937bd514ed9b7
 */
module build.jenesis.plugin.forbiddenapis {

    requires build.jenesis;
    requires static de.thetaphi.forbiddenapis;

    exports build.jenesis.plugin.forbiddenapis;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;
}
