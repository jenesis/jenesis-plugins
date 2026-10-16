/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, incrementally in a JVM the build keeps warm
 * where jenesis.kotlin.incremental asks for it, and documents them with Dokka, configured by the dokka values of its
 * plugin-kotlin.properties, resolving both tools from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
