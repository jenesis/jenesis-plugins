/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, incrementally in a JVM the build keeps warm
 * where jenesis.kotlin.incremental asks for it, and documents them with Dokka, configured by the dokka values of its
 * plugin-kotlin.properties, resolving both tools from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
