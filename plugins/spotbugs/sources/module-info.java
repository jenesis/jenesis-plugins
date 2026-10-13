/**
 * Jenesis SpotBugs plugin
 *
 * Analyses the compiled classes of a module with SpotBugs, excluding what its spotbugs-exclude.xml names, resolving
 * SpotBugs from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 */
module build.jenesis.plugin.spotbugs {

    requires build.jenesis;

    exports build.jenesis.plugin.spotbugs;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotbugs.SpotBugsFactory;
}
