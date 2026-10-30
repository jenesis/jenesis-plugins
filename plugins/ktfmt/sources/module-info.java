/**
 * Jenesis ktfmt plugin
 *
 * Formats or verifies the Kotlin sources of a module with ktfmt in the style a project names, resolving the
 * formatter from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.alias com.facebook.ktfmt com.facebook/ktfmt
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin com.facebook/ktfmt 0.64 SHA-256/7e4dbb5392b99fba5b5ccbe24a2eb4a6d88e419b97887520380c1f2550779b62
 * @jenesis.pin com.google.errorprone/error_prone_annotations 2.41.0 SHA-256/a56e782b5b50811ac204073a355a21d915a2107fce13ec711331ad036f660fcc
 * @jenesis.pin com.google.googlejavaformat/google-java-format 1.23.0 SHA-256/f9c5f181faee5c7b380feb96c1a94bec0c55859baeb2d14e9a47d92d40bef021
 * @jenesis.pin com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
 * @jenesis.pin com.google.guava/guava 33.5.0-jre SHA-256/1e301f0c52ac248b0b14fdc3d12283c77252d4d6f48521d572e7d8c4c2cc4ac7
 * @jenesis.pin com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
 * @jenesis.pin com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/com.facebook/ktfmt/pom 0.64 SHA-256/6797627da300dd341ffea2aab489e47d1b8aa4afbc20b0bba225f63bfded7d17
 * @jenesis.pin maven/com.google.errorprone/error_prone_annotations/pom 2.41.0 SHA-256/a151df1e2e0b48618d8b06a180748a29b3abb39b1b2396f6a1c879a727488c6e
 * @jenesis.pin maven/com.google.errorprone/error_prone_parent/pom 2.41.0 SHA-256/c538388d760a5c1c98dcf06f6ed3cfe5f11a651827db5cbd2ed8288c795cad42
 * @jenesis.pin maven/com.google.googlejavaformat/google-java-format-parent/pom 1.23.0 SHA-256/56091d29a0599cd00a9579ebcd3d1d9d185f5f76f4437df30f40b85fe2c852a4
 * @jenesis.pin maven/com.google.googlejavaformat/google-java-format/pom 1.23.0 SHA-256/df28c5bcd9ae2ec973177cbb5aa57b732276c98c5ed67f91ef93a37e61b5f6a0
 * @jenesis.pin maven/com.google.guava/failureaccess/pom 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.5.0-jre SHA-256/68719e687c6e4c9ff3e0fecbef7bd20896f0f4f7b314743ed33c72f962568215
 * @jenesis.pin maven/com.google.guava/guava/pom 33.5.0-jre SHA-256/0478fa78a908b3c31fe6d77be9978aaa621e2a181db84007e3d3b37424a2ac61
 * @jenesis.pin maven/com.google.guava/listenablefuture/pom 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
 * @jenesis.pin maven/com.google.j2objc/j2objc-annotations/pom 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
 * @jenesis.pin maven/net.java.dev.jna/jna/pom 4.2.2 SHA-256/abd66d98c9b3ddd82f9d79cafa4d661f41bbc595d2c81da7af3fc019109af7b7
 * @jenesis.pin maven/org.ec4j.core/ec4j-core-parent/pom 1.2.0 SHA-256/7b599ea029e118faf94e9846558330867cd2f0a0612f06b83aa0c22b1861b584
 * @jenesis.pin maven/org.ec4j.core/ec4j-core/pom 1.2.0 SHA-256/d9cbe9e89e889d6ee2b03e4df50e3b1cc21688a0d3de04fea24e656bd8f6434b
 * @jenesis.pin maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom 2.3.20 SHA-256/0c35aeee372f07514ebed37b9407b8e1181d1427755139e5cee7719ca05fe7e8
 * @jenesis.pin maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom 2.3.20 SHA-256/61540e5b3a32c0b11962449665924c6cd7554ea9aee0855bd8e4f5b7f610a0ab
 * @jenesis.pin maven/org.jetbrains.kotlin/kotlin-reflect/pom 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
 * @jenesis.pin maven/org.jetbrains.kotlin/kotlin-script-runtime/pom 2.3.20 SHA-256/66099676703a081fd9ac43139665883e5c145020b640759de0539d8916ff247f
 * @jenesis.pin maven/org.jetbrains.kotlin/kotlin-stdlib/pom 2.3.20 SHA-256/c83c02d9a7e2e954730491c33c2f1dc03c27662e27b566ec2b44d2d018639b98
 * @jenesis.pin maven/org.jetbrains.kotlinx/kotlinx-coroutines-bom/pom 1.8.0 SHA-256/1239e9dbe1397cd5971342956b2511bc3ace7b641842e4372a088dcfa8b9ad55
 * @jenesis.pin maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/pom 1.8.0 SHA-256/a5633abd53467ceb91622d81f2e982080877145e0b76e1b75788715434885d0b
 * @jenesis.pin maven/org.jetbrains/annotations/pom 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
 * @jenesis.pin maven/org.jspecify/jspecify/pom 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
 * @jenesis.pin maven/org.sonatype.oss/oss-parent/pom 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
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
