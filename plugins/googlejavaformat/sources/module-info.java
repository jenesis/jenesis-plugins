/**
 * Jenesis google-java-format plugin
 *
 * Formats or verifies the Java sources of a module with google-java-format in the style and with the options a
 * project names, resolving the formatter from the repositories of the build that loads the plugin and
 * running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin com.google.errorprone/error_prone_annotations 2.47.0 SHA-256/5364bc6f22e72e98195e406a58d3ba1c09ffa11dea0729592cb870dc2de4056d
 * @jenesis.pin com.google.googlejavaformat 1.37.0 SHA-256/49874ad1d6b1006d2f8660482135609eb124888e0046c3dbfefc706a0387c700
 * @jenesis.pin com.google.googlejavaformat/google-java-format 1.37.0 SHA-256/49874ad1d6b1006d2f8660482135609eb124888e0046c3dbfefc706a0387c700
 * @jenesis.pin com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
 * @jenesis.pin com.google.guava/guava 33.6.0-jre SHA-256/dc573e1fca4fd5454f4a5fd3d7da2df03002876a4175bafc14a95980dd7713b3
 * @jenesis.pin com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
 * @jenesis.pin com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/com.google.errorprone/error_prone_annotations/pom 2.47.0 SHA-256/d80c889a4a6f711f6945fbee79e05ec247b178a567e9d5abf58eb26ebf0a0752
 * @jenesis.pin maven/com.google.errorprone/error_prone_parent/pom 2.47.0 SHA-256/2368a990c7a63095e1d0d44459d5a4092f0eb31f8562bd12cdf0e1c877b6a685
 * @jenesis.pin maven/com.google.googlejavaformat/google-java-format-parent/pom 1.37.0 SHA-256/7b103362462c4e16e6567715180e87f8a02255ab105d5bb6bac64a244d705a23
 * @jenesis.pin maven/com.google.googlejavaformat/google-java-format/pom 1.37.0 SHA-256/ae9c58622fd7fca223ac3ebec77bfb9c1f950ea481c20f786405e13252367c95
 * @jenesis.pin maven/com.google.guava/failureaccess/pom 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.6.0-jre SHA-256/374bd31f61b1cf612bee9ab2e4d70bbdf77dd85a49b431f809d4fbdc901f2dd4
 * @jenesis.pin maven/com.google.guava/guava/pom 33.6.0-jre SHA-256/b45b78944280054311c53da9270809fb40dcabc408ce66ce55730099b4f57d32
 * @jenesis.pin maven/com.google.guava/listenablefuture/pom 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
 * @jenesis.pin maven/com.google.j2objc/j2objc-annotations/pom 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
 * @jenesis.pin maven/org.commonmark/commonmark-ext-gfm-tables/pom 0.29.0 SHA-256/20a67830ba96d37826eb7f3354a9f390caa27e1f66af67029ff69ef7c2f33bd1
 * @jenesis.pin maven/org.commonmark/commonmark-parent/pom 0.29.0 SHA-256/118b96f673388d28bc6c33519bb9b19770be59f58935f230bca9eb0b082f6e19
 * @jenesis.pin maven/org.commonmark/commonmark/pom 0.29.0 SHA-256/dbd3979a45e5c6b4dcb57c0e71961c3decdb7e089f5b8f5b93754c2c8e178973
 * @jenesis.pin maven/org.jspecify/jspecify/pom 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
 * @jenesis.pin maven/org.sonatype.oss/oss-parent/pom 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
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
