/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, incrementally in a JVM the build keeps warm
 * where jenesis.kotlin.incremental asks for it, and documents them with Dokka, configured by the dokka values of its
 * plugin-kotlin.properties, resolving both tools from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
