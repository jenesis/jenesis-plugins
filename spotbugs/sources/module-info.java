/**
 * Jenesis SpotBugs plugin
 *
 * Analyses the compiled classes of a module with SpotBugs, excluding what its spotbugs-exclude.xml names,
 * resolving SpotBugs from the repositories of the build that loads the plugin and running it in a JVM of its
 * own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/65037e467548677cd8969732ab34d91f763cd7b10175ba14b6ebf7b36be3dee7
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/65037e467548677cd8969732ab34d91f763cd7b10175ba14b6ebf7b36be3dee7
 */
module build.jenesis.plugin.spotbugs {

    requires build.jenesis;

    exports build.jenesis.plugin.spotbugs;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotbugs.SpotBugsFactory;
}
