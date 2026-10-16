/**
 * Jenesis ktlint plugin
 *
 * Checks and formats the Kotlin sources of a module with ktlint, as its .editorconfig says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve ktlint from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 */
module build.jenesis.plugin.ktlint {

    requires build.jenesis;

    exports build.jenesis.plugin.ktlint;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ktlint.KtlintFactory,
            build.jenesis.plugin.ktlint.KtlintFormatFactory;
}
