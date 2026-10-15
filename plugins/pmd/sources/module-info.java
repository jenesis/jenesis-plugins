/**
 * Jenesis PMD plugin
 *
 * Checks the Java sources of a module with PMD against the rule set of its pmd.xml, resolving PMD from the
 * repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/3a267b99570ff390b97e5ef9e2d5d032466a5fd28bcec6161fd6a45e3d61df56
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/3a267b99570ff390b97e5ef9e2d5d032466a5fd28bcec6161fd6a45e3d61df56
 */
module build.jenesis.plugin.pmd {

    requires build.jenesis;

    exports build.jenesis.plugin.pmd;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.pmd.PmdFactory;
}
