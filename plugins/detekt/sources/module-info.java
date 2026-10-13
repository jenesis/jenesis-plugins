/**
 * Jenesis detekt plugin
 *
 * Checks the Kotlin sources of a module with detekt against its detekt.yml, resolving the tool from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/9ad33d240f655e7e5ad42ef9af2ff9f4fdef339d54149cac6bac0402bcc9dab9
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/9ad33d240f655e7e5ad42ef9af2ff9f4fdef339d54149cac6bac0402bcc9dab9
 */
module build.jenesis.plugin.detekt {

    requires build.jenesis;

    exports build.jenesis.plugin.detekt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.detekt.DetektFactory;
}
