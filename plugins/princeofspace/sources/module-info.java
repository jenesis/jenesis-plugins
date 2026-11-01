/**
 * Jenesis Prince of Space plugin
 *
 * Formats or verifies the Java sources of a module with the Prince of Space formatter and the options a project
 * names, resolving the formatter from the repositories of the build that loads the plugin and running it in a JVM
 * of its own.
 *
 * @jenesis.release 25
 * @jenesis.alias io.princeofspace io.github.agustafson.princeofspace/prince-of-space-core
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin com.github.javaparser/javaparser-core 3.28.2 SHA-256/b5499a3b1c40b16c0671fabe478c9aafeab38160c6fde74a6c13f42d86716ecd
 * @jenesis.pin io.github.agustafson.princeofspace/prince-of-space-core 2.3.0 SHA-256/cd74d0ff8e8fa1553dcbefa9fe07709a417a7b2b371d2b13083778c41132066e
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/com.github.javaparser/javaparser-core/pom 3.28.2 SHA-256/87d2b333dfec96896d5a49e80a95b92ef572f610e848276b45eaa061e05b4132
 * @jenesis.pin maven/com.github.javaparser/javaparser-parent/pom 3.28.2 SHA-256/0358ce75bca1a54c141e16b7f1621e973fa177ddf64fe02b0f38df23ad400543
 * @jenesis.pin maven/io.github.agustafson.princeofspace/prince-of-space-core/pom 2.3.0 SHA-256/22bb5e09b8bdde300bc59f4e85904d9977593f452065c526ca0bcbc20758f409
 * @jenesis.pin maven/org.jspecify/jspecify/pom 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
 * @jenesis.pin maven/org.junit/junit-bom/pom 5.14.4 SHA-256/5706e8f29a0a07f56efbbea4a0670793414194bb8d24d8143ba1e787a2f32856
 * @jenesis.pin maven/org.slf4j/slf4j-api/pom 2.0.20 SHA-256/2e625c3f553cddd49f8554cea9926cbf70673b3d6833464f0c5a02f4d9130594
 * @jenesis.pin maven/org.slf4j/slf4j-bom/pom 2.0.20 SHA-256/351a3d34f0886942268efcf291708c48dfadce7f2643837efdb4a6954d4e6c62
 * @jenesis.pin maven/org.slf4j/slf4j-parent/pom 2.0.20 SHA-256/d931cf99cfd3554d322809df03c43ef897082cd480d6ff91b44d8f067ca324c8
 * @jenesis.pin org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
 * @jenesis.pin org.slf4j/slf4j-api 2.0.20 SHA-256/7e1446499b359675d8aabe6af2de86b85e72ae8a2a932c0b40d0e5ba57097439
 */
module build.jenesis.plugin.princeofspace {

    requires build.jenesis;
    requires static io.princeofspace;

    exports build.jenesis.plugin.princeofspace;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.princeofspace.PrinceOfSpaceFactory;
}
