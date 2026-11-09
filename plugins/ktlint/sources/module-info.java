/**
 * Jenesis ktlint plugin
 *
 * Checks and formats the Kotlin sources of a module with ktlint, as its .editorconfig says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve ktlint from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 */
module build.jenesis.plugin.ktlint {

    requires build.jenesis;

    exports build.jenesis.plugin.ktlint;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ktlint.KtlintFactory,
            build.jenesis.plugin.ktlint.KtlintFormatFactory;
}
