/**
 * Jenesis revapi plugin
 *
 * Analyses the API of a module against the release it names as its baseline with revapi, at the severity its
 * plugin-revapi.properties selects, resolving revapi from the repositories of the build that loads the plugin and running it
 * in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 */
module build.jenesis.plugin.revapi {

    requires build.jenesis;

    exports build.jenesis.plugin.revapi;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.revapi.RevapiFactory;
}
