/**
 * Jenesis google-java-format plugin
 *
 * Formats or verifies the Java sources of a module with google-java-format in the style and with the options a
 * project names, resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin com.google.errorprone/error_prone_annotations 2.47.0 SHA-256/5364bc6f22e72e98195e406a58d3ba1c09ffa11dea0729592cb870dc2de4056d
 * @jenesis.pin com.google.googlejavaformat 1.37.0 SHA-256/49874ad1d6b1006d2f8660482135609eb124888e0046c3dbfefc706a0387c700
 * @jenesis.pin com.google.googlejavaformat/google-java-format 1.37.0 SHA-256/49874ad1d6b1006d2f8660482135609eb124888e0046c3dbfefc706a0387c700
 * @jenesis.pin com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
 * @jenesis.pin com.google.guava/guava 33.6.0-jre SHA-256/dc573e1fca4fd5454f4a5fd3d7da2df03002876a4175bafc14a95980dd7713b3
 * @jenesis.pin com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
 * @jenesis.pin com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
 * @jenesis.pin org.commonmark/commonmark 0.29.0 SHA-256/4c9b0fa481e36d7176f38e572c134656424cf9601bc190855d0f89478df901c6
 * @jenesis.pin org.commonmark/commonmark-ext-gfm-tables 0.29.0 SHA-256/f263d284c0795b0ddd6071d4fa969f67d460644c20828e51957ad5aec0d75612
 * @jenesis.pin org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
 */
module build.jenesis.plugin.googlejavaformat {

    requires build.jenesis;
    requires static com.google.googlejavaformat;

    exports build.jenesis.plugin.googlejavaformat;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.googlejavaformat.GoogleJavaFormatFactory;
}
