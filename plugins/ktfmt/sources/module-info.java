/**
 * Jenesis ktfmt plugin
 *
 * Formats or verifies the Kotlin sources of a module with ktfmt in the style a project names, resolving the
 * formatter from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/0a293bd0f8fe09fac0f19d7f13950b4db3799c5c9a3a6ffa78999b22431d265a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/0a293bd0f8fe09fac0f19d7f13950b4db3799c5c9a3a6ffa78999b22431d265a
 */
module build.jenesis.plugin.ktfmt {

    requires build.jenesis;

    exports build.jenesis.plugin.ktfmt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ktfmt.KtfmtFormatFactory;
}
