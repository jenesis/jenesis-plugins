/**
 * Jenesis SpotBugs plugin
 *
 * Analyses the compiled classes of a module with SpotBugs, excluding what its spotbugs-exclude.xml names, resolving
 * SpotBugs from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/002a0daeb38916b5b8b87509cab91ad9f6f2a28d6501ffa446053bfc21f032cf
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/002a0daeb38916b5b8b87509cab91ad9f6f2a28d6501ffa446053bfc21f032cf
 */
module build.jenesis.plugin.spotbugs {

    requires build.jenesis;

    exports build.jenesis.plugin.spotbugs;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotbugs.SpotBugsFactory;
}
