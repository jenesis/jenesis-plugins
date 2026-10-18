/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, in one JVM the build keeps warm under
 * jenesis.process.mode=daemon and incrementally where jenesis.kotlin.incremental asks for it, and documents them
 * with Dokka, configured by the dokka values of its plugin-kotlin.properties, resolving both tools from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.1 SHA-256/2c74e5fd279e77d855cdf13719e443e0b3f0864e097486e03d065d39be0ce37b
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
