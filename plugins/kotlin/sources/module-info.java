/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, in one JVM the build keeps warm under
 * jenesis.process.mode=daemon and incrementally where jenesis.kotlin.incremental asks for it, and documents them
 * with Dokka, configured by the values of a dokka.properties, resolving both tools from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.17.0 SHA-256/195b07a6b70f2d5088dc832758f0f84ad8c120928e9f3993ec8437c72f4fae01
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
