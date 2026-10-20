/**
 * Jenesis Palantir Java Format plugin
 *
 * Formats or verifies the Java sources of a module with Palantir Java Format and the options a project names,
 * resolving the formatter from the repositories of the build that loads the plugin and running it in a JVM of
 * its own.
 *
 * @jenesis.release 25
 * @jenesis.alias com.palantir.javaformat com.palantir.javaformat/palantir-java-format
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
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
 * @jenesis.pin org.functionaljava/functionaljava 5.0 SHA-256/377ad140e7d26ba04fadf219b09d7e1c74bc0232fa4010b20c1c79db11f9670e
 * @jenesis.pin org.jspecify/jspecify 1.0.1 SHA-256/070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66
 */
module build.jenesis.plugin.palantir {

    requires build.jenesis;
    requires static com.palantir.javaformat;

    exports build.jenesis.plugin.palantir;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.palantir.PalantirJavaFormatFactory;
}
