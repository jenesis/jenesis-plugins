/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, in one JVM the build keeps warm under
 * jenesis.process.mode=daemon and incrementally where jenesis.kotlin.incremental asks for it, and documents them
 * with Dokka, configured by the dokka values of its plugin-kotlin.properties, resolving both tools from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
