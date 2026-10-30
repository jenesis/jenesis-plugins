/**
 * Jenesis Palantir Java Format plugin
 *
 * Formats or verifies the Java sources of a module with Palantir Java Format and the options a project names,
 * resolving the formatter from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias com.palantir.javaformat com.palantir.javaformat/palantir-java-format
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin com.fasterxml.jackson.core/jackson-annotations 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
 * @jenesis.pin com.fasterxml.jackson.core/jackson-core 2.22.3 SHA-256/8a501126a385b25841915d839508f8a66e2a0dbc8a6709d055ef3b3e852b094c
 * @jenesis.pin com.fasterxml.jackson.core/jackson-databind 2.22.3 SHA-256/556db5439e206114346043f68d200497dc96a0bca62a360a81784092ebd0e0a9
 * @jenesis.pin com.fasterxml.jackson.datatype/jackson-datatype-guava 2.22.3 SHA-256/30d44f810bd73264f2b8e46c799f5a5dc933acf827493d73bfba5525c647d6c0
 * @jenesis.pin com.fasterxml.jackson.datatype/jackson-datatype-jdk8 2.22.3 SHA-256/f223061a5aaecdc9506458143807c73bcd1a744d6b3bbb8da4703a0abc408f7d
 * @jenesis.pin com.fasterxml.jackson.module/jackson-module-parameter-names 2.22.3 SHA-256/3f085667cd7b2dd9794a837b6e4a5c57779132bfc37b49b5c457870eee9d60ba
 * @jenesis.pin com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
 * @jenesis.pin com.google.errorprone/error_prone_annotations 2.50.0 SHA-256/4667724877f1d37a689202da191e23efa7657c62eef93ccdac406eccfe5cdd0a
 * @jenesis.pin com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
 * @jenesis.pin com.google.guava/guava 33.7.2-jre SHA-256/b530942257fb935f8b2cfaa5f8eb5bd59c555fd8e8d01b8ce98912e077ea606c
 * @jenesis.pin com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
 * @jenesis.pin com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
 * @jenesis.pin com.palantir.javaformat/palantir-java-format 2.102.0 SHA-256/8074d7e21832edcad422f8dbf6002d296551963bec410daad97317210a55b5c1
 * @jenesis.pin com.palantir.javaformat/palantir-java-format-spi 2.102.0 SHA-256/ac4b8e44d90e09ee4ceafb6ad3083097871a161cc88feab6bd87378f4e4859bc
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/com.fasterxml.jackson.core/jackson-annotations/pom 2.22 SHA-256/38c3ba52a0eade3e3eacc5b13499d56e1038ccbad8ec4bb79426dc21432b46d0
 * @jenesis.pin maven/com.fasterxml.jackson.core/jackson-core/pom 2.22.3 SHA-256/bc74c18b24f3ebbfd42741f2b29b37eca9b25e512e6fa7add77f6973cee50bf1
 * @jenesis.pin maven/com.fasterxml.jackson.core/jackson-databind/pom 2.22.3 SHA-256/27a5c6657b955d088daf586f2be9769224e33618ab4f44682a1b36ed98f7b904
 * @jenesis.pin maven/com.fasterxml.jackson.datatype/jackson-datatype-guava/pom 2.22.3 SHA-256/222824308869262ce0c3dcc063e958a90733fc791cf229e7264e5c9809d5c88b
 * @jenesis.pin maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/pom 2.22.3 SHA-256/e4d6921c69fecf71fde0a631c16e115e27d814b4bf501920a2311b6ede4896ee
 * @jenesis.pin maven/com.fasterxml.jackson.datatype/jackson-datatypes-collections/pom 2.22.3 SHA-256/65ed3877e344771fb90b085389bd6c3015821281bd4fa9669a008b7336493d4e
 * @jenesis.pin maven/com.fasterxml.jackson.module/jackson-module-parameter-names/pom 2.22.3 SHA-256/8e43e72ff40b3a5b47d3bc8b33bc5c0532f61eb43da97448b1c0baee8f0cb74f
 * @jenesis.pin maven/com.fasterxml.jackson.module/jackson-modules-java8/pom 2.22.3 SHA-256/19aa2e1fa4a541bd20815f110f80e9232bc3dee87d2492bb04e56cdfc4f28bb6
 * @jenesis.pin maven/com.fasterxml.jackson/jackson-base/pom 2.22.3 SHA-256/1991ff35885b389eb29c23ae318c8eca66562915205bdeea53c722a5bd1b3bcb
 * @jenesis.pin maven/com.fasterxml.jackson/jackson-bom/pom 2.22.3 SHA-256/8f499c6a8c017bb987a1a73b91f705c22fe24ec2777e7055c7a2a913f14b5440
 * @jenesis.pin maven/com.fasterxml.jackson/jackson-parent/pom 2.22 SHA-256/7d45a7efb69df207bed7cc54b87a0bb012f10952c095570a273b49485da0d129
 * @jenesis.pin maven/com.fasterxml/oss-parent/pom 79 SHA-256/9206b7dd67fb138036576c3dfca947a2a8f3e2c4d2d9c1f2d1dea519b3a4dae1
 * @jenesis.pin maven/com.google.code.findbugs/jsr305/pom 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
 * @jenesis.pin maven/com.google.errorprone/error_prone_annotations/pom 2.50.0 SHA-256/71a0680d277324bab46bec4b00f6f111c1b794a931d0d8cda79bcb9246982f92
 * @jenesis.pin maven/com.google.errorprone/error_prone_parent/pom 2.50.0 SHA-256/abf45a019db125b50ff55134ec2af94ae3519761038adf0a60b9bd7b4b8b6254
 * @jenesis.pin maven/com.google.guava/failureaccess/pom 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
 * @jenesis.pin maven/com.google.guava/guava-parent/pom 33.7.2-jre SHA-256/a81570d53c8710355e0216a4405afa93834b17c4cdd0ed9aa8beede3a132bbbe
 * @jenesis.pin maven/com.google.guava/guava/pom 33.7.2-jre SHA-256/5d1501a97230f90df54ea33164831a61c6714a8b2a4c6a04e90bb729581bbabc
 * @jenesis.pin maven/com.google.guava/listenablefuture/pom 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
 * @jenesis.pin maven/com.google.j2objc/j2objc-annotations/pom 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
 * @jenesis.pin maven/com.palantir.javaformat/palantir-java-format-spi/pom 2.102.0 SHA-256/482a1ebf2d932a7adaca2984512c7b7991650424a9a51f0ec637e56cc5409bd1
 * @jenesis.pin maven/com.palantir.javaformat/palantir-java-format/pom 2.102.0 SHA-256/fe8d312e5310946775e6ba35053b6358f166336accd944b02ee82a0064d2c3f8
 * @jenesis.pin maven/org.functionaljava/functionaljava/pom 5.0 SHA-256/47d380d98553daf742739b510bfdc4948a741877edcb2a5396fc29199689ce09
 * @jenesis.pin maven/org.jspecify/jspecify/pom 1.0.1 SHA-256/55b38bf1a7b1d8ea518cf2f4f3ae9aaa236e61bbf0b6bd31ffb49753a059dd5e
 * @jenesis.pin maven/org.junit/junit-bom/pom 5.14.3 SHA-256/08a02856e487c9357f9b29e38745f8ae805848111e72d15aad0352338f1632e1
 * @jenesis.pin maven/org.sonatype.oss/oss-parent/pom 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
 * @jenesis.pin maven/org.sonatype.oss/oss-parent/pom 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
 * @jenesis.pin org.functionaljava/functionaljava 5.0 SHA-256/377ad140e7d26ba04fadf219b09d7e1c74bc0232fa4010b20c1c79db11f9670e
 * @jenesis.pin org.jspecify/jspecify 1.0.1 SHA-256/070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66
 */
module build.jenesis.plugin.palantir {

    requires build.jenesis;
    requires static com.palantir.javaformat;

    exports build.jenesis.plugin.palantir;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.palantir.PalantirJavaFormatFactory;
}
