/**
 * Jenesis scalafmt plugin
 *
 * Checks and formats the Scala sources of a module with scalafmt, as its .scalafmt.conf says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve scalafmt from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin build.jenesis/build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.scalafmt {

    requires build.jenesis;

    exports build.jenesis.plugin.scalafmt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.scalafmt.ScalafmtFactory,
            build.jenesis.plugin.scalafmt.ScalafmtFormatFactory;
}
