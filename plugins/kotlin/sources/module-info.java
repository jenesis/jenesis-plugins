/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, incrementally in the Kotlin daemon where
 * jenesis.kotlin.incremental asks for it, and documents them with Dokka, configured by the dokka values of its
 * plugin-kotlin.properties, resolving both tools from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/b1fe38f14435dec3e11df3f8f31d8050a9bc93ad16f654da8973353eff6d4f06
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
