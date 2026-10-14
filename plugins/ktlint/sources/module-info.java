/**
 * Jenesis ktlint plugin
 *
 * Checks and formats the Kotlin sources of a module with ktlint, as its .editorconfig says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve ktlint from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 */
module build.jenesis.plugin.ktlint {

    requires build.jenesis;

    exports build.jenesis.plugin.ktlint;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ktlint.KtlintFactory,
            build.jenesis.plugin.ktlint.KtlintFormatFactory;
}
