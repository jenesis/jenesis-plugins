/**
 * Jenesis SpotBugs plugin
 *
 * Analyses the compiled classes of a module with SpotBugs, excluding what its spotbugs-exclude.xml names, resolving
 * SpotBugs from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin build.jenesis/build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.spotbugs {

    requires build.jenesis;

    exports build.jenesis.plugin.spotbugs;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotbugs.SpotBugsFactory;
}
