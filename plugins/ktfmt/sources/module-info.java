/**
 * Jenesis ktfmt plugin
 *
 * Formats or verifies the Kotlin sources of a module with ktfmt in the style a project names, resolving the
 * formatter from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.alias com.facebook.ktfmt com.facebook/ktfmt
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin com.facebook/ktfmt 0.64 SHA-256/7e4dbb5392b99fba5b5ccbe24a2eb4a6d88e419b97887520380c1f2550779b62
 * @jenesis.pin com.google.errorprone/error_prone_annotations 2.41.0 SHA-256/a56e782b5b50811ac204073a355a21d915a2107fce13ec711331ad036f660fcc
 * @jenesis.pin com.google.googlejavaformat/google-java-format 1.23.0 SHA-256/f9c5f181faee5c7b380feb96c1a94bec0c55859baeb2d14e9a47d92d40bef021
 * @jenesis.pin com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
 * @jenesis.pin com.google.guava/guava 33.5.0-jre SHA-256/1e301f0c52ac248b0b14fdc3d12283c77252d4d6f48521d572e7d8c4c2cc4ac7
 * @jenesis.pin com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
 * @jenesis.pin com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
 * @jenesis.pin net.java.dev.jna/jna 4.2.2 SHA-256/1f38af54e06c6e6f6dbf39ba2c052b952dea5dddb4871127b34639ddeb11bdbe
 * @jenesis.pin org.ec4j.core/ec4j-core 1.2.0 SHA-256/7517c08e91ba0925f9470b21e538bc0feac48398fc650eb1bae8af7c92492117
 * @jenesis.pin org.jetbrains.kotlin/kotlin-compiler-embeddable 2.3.20 SHA-256/976f989d0b5f5d80e8e8a8ad4b73da0bfc27fdd965b9fa38362b2be79ecc1337
 * @jenesis.pin org.jetbrains.kotlin/kotlin-daemon-embeddable 2.3.20 SHA-256/8870bab840b8087c96c4ddc06088b4aedf5131c408af3674306304f1f96af3f4
 * @jenesis.pin org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
 * @jenesis.pin org.jetbrains.kotlin/kotlin-script-runtime 2.3.20 SHA-256/6fcdb7da6e65cf8cc43e5aabab94bdcc48825e7933686f8a1bf694eb88f8e00e
 * @jenesis.pin org.jetbrains.kotlin/kotlin-stdlib 2.3.20 SHA-256/0ae12504a5040ebaf37703908483420d1a5624dd1d93f357665f8c77c848a01e
 * @jenesis.pin org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm 1.8.0 SHA-256/9860906a1937490bf5f3b06d2f0e10ef451e65b95b269f22daf68a3d1f5065c5
 * @jenesis.pin org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
 * @jenesis.pin org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
 */
module build.jenesis.plugin.ktfmt {

    requires build.jenesis;
    requires static com.facebook.ktfmt;

    exports build.jenesis.plugin.ktfmt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.ktfmt.KtfmtFormatFactory;
}
