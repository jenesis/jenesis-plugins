/**
 * Jenesis scalafmt plugin
 *
 * Checks and formats the Scala sources of a module with scalafmt, as its .scalafmt.conf says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve scalafmt from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 */
module build.jenesis.plugin.scalafmt {

    requires build.jenesis;

    exports build.jenesis.plugin.scalafmt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.scalafmt.ScalafmtFactory,
            build.jenesis.plugin.scalafmt.ScalafmtFormatFactory;
}
