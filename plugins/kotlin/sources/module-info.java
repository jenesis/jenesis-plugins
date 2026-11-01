/**
 * Jenesis Kotlin plugin
 *
 * Compiles the Kotlin sources of a module with kotlinc ahead of javac, in one JVM the build keeps warm under
 * jenesis.process.mode=daemon and incrementally where jenesis.kotlin.incremental asks for it, and documents them
 * with Dokka, configured by the values of a dokka.properties, resolving both tools from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin build.jenesis/build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.kotlin {

    requires build.jenesis;

    exports build.jenesis.plugin.kotlin;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.kotlin.KotlinFactory;
}
