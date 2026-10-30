package build.jenesis.plugin.openrewrite.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Pinning;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.openrewrite.OpenRewriteModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OpenRewriteModuleRunTest {

    private static final String PINS = """
            openrewrite/maven/com.fasterxml.jackson.core/jackson-annotations 2.21 SHA-256/53ca085f4a150f703f49e1aabd935bd03b43e1ea3d55d135438292af22cef56b
            openrewrite/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.21 2.21 SHA-256/71cac5392151e2a528cc9a09179f0a334179f05c52f8e596cf58ddf12bb27f2b
            openrewrite/maven/com.fasterxml.jackson.core/jackson-core 2.21.6 SHA-256/36111c3a4372cd5c2be6f4ec44a050382487920f3fc38ca3015c9c1678bd7c56
            openrewrite/maven/com.fasterxml.jackson.core/jackson-core/pom/2.21.6 2.21.6 SHA-256/e23418096b02abd1cc829100f4195b07b88dbdf5e17d51440e900ae335073052
            openrewrite/maven/com.fasterxml.jackson.core/jackson-databind 2.21.6 SHA-256/62134a98de12d0a421cd2f5b9854c8e63f10f524b0341e66cfd31a4326f6a329
            openrewrite/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.21.6 2.21.6 SHA-256/18205190a800163548cecfd817b3f330bb6b9522fa826bdc9b1db9fade997d32
            openrewrite/maven/com.fasterxml.jackson.dataformat/jackson-dataformat-smile 2.21.6 SHA-256/735ea360294ced091cb623cb9370604bfba89187d24b0934267475d2921f2309
            openrewrite/maven/com.fasterxml.jackson.dataformat/jackson-dataformat-smile/pom/2.21.6 2.21.6 SHA-256/aa6851edbdf4837a80e211fdf2fd8b9dc7ffc3eb1c97ff51e43e5e9083c18666
            openrewrite/maven/com.fasterxml.jackson.dataformat/jackson-dataformats-binary/pom/2.21.6 2.21.6 SHA-256/eb24102450be2c4781ae132e9ea8d61ea70347887aa60f73559ba8a959e16fab
            openrewrite/maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310 2.21.6 SHA-256/1d82027afe574c63e24df72eb109f98de02dff3bb5adf81f0174b27088490067
            openrewrite/maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310/pom/2.21.6 2.21.6 SHA-256/4fe1e9d01a748463fa8e9247ac29c124fd60e4d748abec8bcfe54e7ee7da2792
            openrewrite/maven/com.fasterxml.jackson.module/jackson-module-parameter-names 2.21.6 SHA-256/24f5c6f75fad85d7e2e3f600ef4832a839def8a2bf1e6f2fc5a0577cef54e01d
            openrewrite/maven/com.fasterxml.jackson.module/jackson-module-parameter-names/pom/2.21.6 2.21.6 SHA-256/9ff8d526768fad3b82494a6012e6a0721c9bd646eacd58574210b5026ec4d8fc
            openrewrite/maven/com.fasterxml.jackson.module/jackson-modules-java8/pom/2.21.6 2.21.6 SHA-256/21488268202856ca661cd0ba5bf0adb0e3810a386c0565a3f9457a25e9c69972
            openrewrite/maven/com.fasterxml.jackson/jackson-base/pom/2.21.6 2.21.6 SHA-256/d0a9cbfaeeeb1335555647a316a1c39d5d045f4c072eeb320626b3df2666ebc5
            openrewrite/maven/com.fasterxml.jackson/jackson-bom/pom/2.21.5 2.21.5 SHA-256/57fe22dec659de0655357fad0582ef7c8b2fe5a8906a6f1ff1bcbbd1e4554846
            openrewrite/maven/com.fasterxml.jackson/jackson-bom/pom/2.21.6 2.21.6 SHA-256/19cc2ca65ea8f623e035e1023ab9de8627fe5f578fafdaff491060fb4cf0b5ab
            openrewrite/maven/com.fasterxml.jackson/jackson-parent/pom/2.21 2.21 SHA-256/3851df627faeb46887b956104630b74a8bbb5f7de22e974cf15982e20e0373de
            openrewrite/maven/com.fasterxml/oss-parent/pom/75 75 SHA-256/fcbbf1c18c9047e6917d384f2064e21b42a58fc85fb0523a9e2e015eff7c619d
            openrewrite/maven/com.github.ben-manes.caffeine/caffeine 2.9.3 SHA-256/1e0a7bbef1dd791653143f3f05d0e489934bf5481e58a87c9e619cd46b68729b
            openrewrite/maven/com.github.ben-manes.caffeine/caffeine/pom/2.9.3 2.9.3 SHA-256/6fa4f1c10192806f8ef05b5d4be7bd9f5ce58b876f64364d4e90c3fc092323dc
            openrewrite/maven/com.google.errorprone/error_prone_annotations 2.10.0 SHA-256/a249d4d25dfb86d41e6c82fc335df580189f0c9feeabdc53233fc1e5060724a1
            openrewrite/maven/com.google.errorprone/error_prone_annotations/pom/2.10.0 2.10.0 SHA-256/d680dc9f578a77a6dcd992b2c3250eb168a7214cf27f8f0cac1f06526710c0cc
            openrewrite/maven/com.google.errorprone/error_prone_parent/pom/2.10.0 2.10.0 SHA-256/07891a87c490f900c12097bee95ec1cb6e071c8a7df810666387e0c772f089fa
            openrewrite/maven/com.googlecode.javaewah/JavaEWAH 1.1.13 SHA-256/4c0fda2b1d317750d7ea324e36c70b2bc48310c0aaae67b98df0915d696d7111
            openrewrite/maven/com.googlecode.javaewah/JavaEWAH/pom/1.1.13 1.1.13 SHA-256/9725b1fe9c6810d977750bb84415ea1082c4b488d4a839f9704bb4f5e8fc17f4
            openrewrite/maven/com.univocity/univocity-parsers 2.9.1 SHA-256/31685122d5e392e98672ed6009a95a4c1623ca1185567bd44ee94527d454e5c3
            openrewrite/maven/com.univocity/univocity-parsers/pom/2.9.1 2.9.1 SHA-256/52177d22cd08b25571135be471198be757fcc8cfbe71b70481d01a3dc216eddb
            openrewrite/maven/io.github.classgraph/classgraph 4.8.193 SHA-256/e95ab7083d025b7f6a4fb7c0e608a9ff1ddc0ea91b4cd757c9c3a0650e61b34b
            openrewrite/maven/io.github.classgraph/classgraph/pom/4.8.193 4.8.193 SHA-256/27e3f015c305b6299574a732d30cfbd2ba09b86aaeaa3619e591bf702cbe0d3a
            openrewrite/maven/io.micrometer/micrometer-core 1.9.17 SHA-256/c350cfdc76c08356f12a60e840f2dbeaeea7540712d58b54becd6c302a86147a
            openrewrite/maven/io.micrometer/micrometer-core/pom/1.9.17 1.9.17 SHA-256/03969200ba58af48f173abf5ece20bc0af08735045fbfe33025f4fd9a886ab16
            openrewrite/maven/io.moderne/jsonrpc 1.0.12 SHA-256/20225ec918f60137496b0596993b04d21ae35db464d67e02f8b93aa4543c4b66
            openrewrite/maven/io.moderne/jsonrpc/pom/1.0.12 1.0.12 SHA-256/7a3678d0a7124cc9a66f9d38017c3c3450a417186d304e6329f83c56bceaa2e3
            openrewrite/maven/io.quarkus.gizmo/gizmo 1.0.11.Final SHA-256/a54bfbb6dcce9298134595b3cd1df961bc732468825f26ee5296f2b5f88c0b99
            openrewrite/maven/io.quarkus.gizmo/gizmo/pom/1.0.11.Final 1.0.11.Final SHA-256/ffdc392086cec2db81d26e2ebd36034711e8929c28435b9476919608536b38fd
            openrewrite/maven/net.java.dev.jna/jna-platform 5.19.1 SHA-256/3b3864f5b449e9c3c24b16861524b622b086563f44e0cd8384c8efc5a6052f82
            openrewrite/maven/net.java.dev.jna/jna 5.19.1 SHA-256/4fb141dd8ef6b0585ffceea4bc49602fbc6312fa977e2c488794ea3e6aafecae
            openrewrite/maven/net.java.dev.jna/jna-platform/pom/5.19.1 5.19.1 SHA-256/3e9603f6f0e7a49c88378d2a82bcaf53e33ad67dbc90a8d5a27e09a9736fce50
            openrewrite/maven/net.java.dev.jna/jna/pom/5.19.1 5.19.1 SHA-256/911b754a03b66af0fed2bbdfdc3a86807360dd036ad3b481ef5162c685e456bf
            openrewrite/maven/org.antlr/antlr4-master/pom/4.13.2 4.13.2 SHA-256/0adda026685873f651360178bffc446ceee4833081739e3ae9d6e8f07e8d902a
            openrewrite/maven/org.antlr/antlr4-runtime 4.13.2 SHA-256/dd3e8a13a2d669bf84fb8d834de35ce4875f27157698d206241ec8488aadcaf7
            openrewrite/maven/org.antlr/antlr4-runtime/pom/4.13.2 4.13.2 SHA-256/03ce07a2796c511b0c94dc14fd86ccdd6e3828c57967ad2383de304e0d11ba79
            openrewrite/maven/org.apache.commons/commons-lang3 3.20.0 SHA-256/69e5c9fa35da7a51a5fd2099dfe56a2d8d32cf233e2f6d770e796146440263f4
            openrewrite/maven/org.apache.commons/commons-lang3/pom/3.20.0 3.20.0 SHA-256/7ca83b2709c1e7a9e03b576cd41422190379489a80866e542f8c8b955411a2aa
            openrewrite/maven/org.apache.commons/commons-parent/pom/92 92 SHA-256/94f6c027b15f6402995dd1b2c7ea35bfe1ebc88b6832b3134368b41998721953
            openrewrite/maven/org.apache.commons/commons-parent/pom/93 93 SHA-256/a88b5f6165a659bf835441837bcc5c53e99fde79b92cfbb86988ce2954cd5267
            openrewrite/maven/org.apache.commons/commons-text 1.15.0 SHA-256/58d2da30f058512a1e7f914e39241deca4dff5c27a085b4ed2faa9e7208067f6
            openrewrite/maven/org.apache.commons/commons-text/pom/1.15.0 1.15.0 SHA-256/5e5168687bcedaea2aa8556b8b32f5afa08efcbc31d11b947c8676987bc9d239
            openrewrite/maven/org.apache/apache/pom/35 35 SHA-256/ea297dcd114136e8b8e8b630230d52a76c2fc69f6c5db25d672b1857000728b8
            openrewrite/maven/org.checkerframework/checker-qual 3.19.0 SHA-256/a827c49183f3a632277d27a0a4673686cb341507447b9d570261094bd748aa68
            openrewrite/maven/org.checkerframework/checker-qual/pom/3.19.0 3.19.0 SHA-256/29ba9c5ce1a94b700bd823c4ed612f5827b590f19a49775fd7eb8f997f8aa371
            openrewrite/maven/org.hdrhistogram/HdrHistogram 2.1.12 SHA-256/9b47fbae444feaac4b7e04f0ea294569e4bc282bc69d8c2ce2ac3f23577281e2
            openrewrite/maven/org.hdrhistogram/HdrHistogram/pom/2.1.12 2.1.12 SHA-256/7fb3e790c154d1b5e23170bb8cbf7f70ef0809af17229f1dcb010d7799651080
            openrewrite/maven/org.jboss/jandex 2.4.2.Final SHA-256/3f2ce55c7d71e744581488dc5105806aa8084c08e6e916a019bab8f8698994f0
            openrewrite/maven/org.jboss/jandex/pom/2.4.2.Final 2.4.2.Final SHA-256/ea5e01cbb750f95d7e0e447d3cb93435a74281953d95d6532b3c84807c28d747
            openrewrite/maven/org.jboss/jboss-parent/pom/12 12 SHA-256/9e56f5b21898a6e711e857621ac1efb6cf3c3de165a4aa307db998826ad4ffdf
            openrewrite/maven/org.jboss/jboss-parent/pom/39 39 SHA-256/04dff075a00094b63061af407d2816d9cde66795acae376a054bdfe8ba31e664
            openrewrite/maven/org.jetbrains/annotations 26.1.0 SHA-256/ebc7aec252ed0c7d2d04c039d7f00e69f7b86b1f493c741d67b3ef31b986b054
            openrewrite/maven/org.jetbrains/annotations/pom/26.1.0 26.1.0 SHA-256/f8dc50cf3aac4bcec740a90955f2800e41251e9c403630b75b752d3b6ef5bee2
            openrewrite/maven/org.jspecify/jspecify 1.0.1 SHA-256/070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66
            openrewrite/maven/org.jspecify/jspecify/pom/1.0.1 1.0.1 SHA-256/55b38bf1a7b1d8ea518cf2f4f3ae9aaa236e61bbf0b6bd31ffb49753a059dd5e
            openrewrite/maven/org.junit/junit-bom/pom/5.13.4 5.13.4 SHA-256/d7a08a99b2502f0bb68cd4e1f984f0bf69324aaa208bd0f73366c03fc3548a42
            openrewrite/maven/org.junit/junit-bom/pom/5.14.1 5.14.1 SHA-256/01b01dfa366550b40ac5760548a7d728b6109d17c451e83864d1e5e0ce862c94
            openrewrite/maven/org.junit/junit-bom/pom/6.1.2 6.1.2 SHA-256/9b6d6a6fa82b3d76048955e0bb0783d64c7e74711c51f1b5dcfc3eccdd303be2
            openrewrite/maven/org.latencyutils/LatencyUtils 2.0.3 SHA-256/a32a9ffa06b2f4e01c5360f8f9df7bc5d9454a5d373cd8f361347fa5a57165ec
            openrewrite/maven/org.latencyutils/LatencyUtils/pom/2.0.3 2.0.3 SHA-256/8f0c0153790b84af6c09316d569bd106ee0f008b9393e80ba478f5bf65738a28
            openrewrite/maven/org.objenesis/objenesis 3.6 SHA-256/6ecc0fc776af04e2bfd2bf42d618ab5161440dcb8a0e7dff9e84c065c7cec3dd
            openrewrite/maven/org.objenesis/objenesis-parent/pom/3.6 3.6 SHA-256/6948bc13f93520ec6889f66b458d02f0e8f2a6f559f03989ec9898c6757a64bc
            openrewrite/maven/org.objenesis/objenesis/pom/3.6 3.6 SHA-256/2934b7416a8d567a9878399ab59663a73f1b071886e5cf84243959f385b95abd
            openrewrite/maven/org.openrewrite.tools/java-object-diff 1.0.2 SHA-256/86c5e72942bb12143f2c283bb2d44c9f022e52d44361751ead8c11f3ebde2596
            openrewrite/maven/org.openrewrite.tools/java-object-diff/pom/1.0.2 1.0.2 SHA-256/722ae39507bbd16553b132cabbc25127c793f5876981878c3a24b70fc6281446
            openrewrite/maven/org.openrewrite.tools/jgit 1.5.0 SHA-256/50c68d603cb8d7f2585d3d98824dc509fdfd95812adb4ac93d6cec4db095b29c
            openrewrite/maven/org.openrewrite.tools/jgit/pom/1.5.0 1.5.0 SHA-256/793ccc776d74650b0e91c007d681740fa7236670657136747ae9dfb15d337a2e
            openrewrite/maven/org.openrewrite/rewrite-core 8.90.4 SHA-256/12168e8045fb62c1b3f3ad3f3ef8895db84e2791d8c811aefda410e262266503
            openrewrite/maven/org.openrewrite/rewrite-core/pom/8.90.4 8.90.4 SHA-256/d0f4c5fd9453ca42c84f49da7d0e445a53dae82f9594833bd6d0b756f6395a6f
            openrewrite/maven/org.openrewrite/rewrite-java-25 8.90.4 SHA-256/32b05a74bb19e72e4e0241c16ad49c79283e61746ce717066a7563b4947a9cf6
            openrewrite/maven/org.openrewrite/rewrite-java-25/pom/8.90.4 8.90.4 SHA-256/9cd8915e5aecf987d802d1d91b724818ec5c2e23a9afe95b2c6cbe86558cf755
            openrewrite/maven/org.openrewrite/rewrite-java-lombok 8.90.4 SHA-256/db47834696d127a836b4ac51225bbe1a5b2dbab155c9f44f1b0c5cd582a6f741
            openrewrite/maven/org.openrewrite/rewrite-java 8.90.4 SHA-256/c18f5b86a2401f64d82d036460e15937dfd87d41e3b9772757cadee89b89c5bb
            openrewrite/maven/org.openrewrite/rewrite-java-lombok/pom/8.90.4 8.90.4 SHA-256/db4511f09bff9a05df5232062d2134881b4c6ec7625332e541777d9233e5b1c0
            openrewrite/maven/org.openrewrite/rewrite-java/pom/8.90.4 8.90.4 SHA-256/af5c9b8d9b10622406d878974563dd11c3903b4071ee65db3895bbc5e98ba478
            openrewrite/maven/org.openrewrite/rewrite-properties 8.90.4 SHA-256/fd6d276c91cfd69fd34aa52dd67e5896cd86bbbd3c1a38d8206a36d1bdeb2b83
            openrewrite/maven/org.openrewrite/rewrite-properties/pom/8.90.4 8.90.4 SHA-256/6ba8cfa5b52f2601c4fa40368e9b01b56de4d275e2a2ee8a20ea0ab2b79d15ee
            openrewrite/maven/org.openrewrite/rewrite-xml 8.90.4 SHA-256/6652d97b0ecc8801bbd17158c83f445f60904d41466e3807363f2a87f77d2d2e
            openrewrite/maven/org.openrewrite/rewrite-xml/pom/8.90.4 8.90.4 SHA-256/39f323a9a4b93157b965deab7f3392e43da952144551e828531abf33d29416f5
            openrewrite/maven/org.openrewrite/rewrite-yaml 8.90.4 SHA-256/4a13a672601e40180c2ddc234edbfcf4dab139ffb0b8ce7ec25f4e7fccf9552d
            openrewrite/maven/org.openrewrite/rewrite-yaml/pom/8.90.4 8.90.4 SHA-256/ed31088a3e2763cc648e753cff1cba8b1879dccecfa23286daf4ae717161b814
            openrewrite/maven/org.ow2.asm/asm-analysis 9.10.1 SHA-256/dede75a21306b65974ecd8f87114ff6970f09fb794157a4ca09ab25c888c2bfc
            openrewrite/maven/org.ow2.asm/asm-analysis/pom/9.10.1 9.10.1 SHA-256/534fbb85f643c2330691e92652ec40dd497b4f279ef13d829d85858905d33ba3
            openrewrite/maven/org.ow2.asm/asm-tree 9.10.1 SHA-256/3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b
            openrewrite/maven/org.ow2.asm/asm-tree/pom/9.10.1 9.10.1 SHA-256/963eebdea6e2e1ff25c9068b64a238e1585c99f8acd455595ac159a7f52b6c41
            openrewrite/maven/org.ow2.asm/asm-util 9.10.1 SHA-256/1bb99d091fba2597dc6d51193e9bbcf0d8447e7ed96bd8f0198b18152f09655c
            openrewrite/maven/org.ow2.asm/asm 9.10.1 SHA-256/ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb
            openrewrite/maven/org.ow2.asm/asm-util/pom/9.10.1 9.10.1 SHA-256/fcea2bad7868ed18949a2e71c40cf26de1516d16440e63f585c3e05d285c0d2b
            openrewrite/maven/org.ow2.asm/asm/pom/9.10.1 9.10.1 SHA-256/e1b1c3832ca3ac0a9e563c405f4c760118b5556a4920daee14058886197e094d
            openrewrite/maven/org.ow2/ow2/pom/1.5.1 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
            openrewrite/maven/org.projectlombok/lombok 1.18.46 SHA-256/01f7b1a015e33e2b62d5f5f37053306357ab1415fd181fcba7794f5d198c1126
            openrewrite/maven/org.projectlombok/lombok/pom/1.18.46 1.18.46 SHA-256/294e987cf26b37ed9de1b97d32039090d27c587d0e04cea2ae47766fa7038995
            openrewrite/maven/org.slf4j/slf4j-api 2.0.17 SHA-256/7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832
            openrewrite/maven/org.slf4j/slf4j-api/pom/2.0.17 2.0.17 SHA-256/150c40287f7cecdc21b9380caac98e928c4f33c023db6b348df1c5ac977026bf
            openrewrite/maven/org.slf4j/slf4j-bom/pom/2.0.17 2.0.17 SHA-256/f78d27b642b4b886eb839fc102b5cd9fe7f30f37599ffe68185be4e1609031e9
            openrewrite/maven/org.slf4j/slf4j-nop 2.0.17 SHA-256/3716f83649ec66161a2edefd4f49df34d1dd1c51cdcf941996c6987260f0a829
            openrewrite/maven/org.slf4j/slf4j-nop/pom/2.0.17 2.0.17 SHA-256/e5e548dce8c1c979138a7e96ca354510fa0ce103f0a6df53a1f5b25e9298b9b2
            openrewrite/maven/org.slf4j/slf4j-parent/pom/2.0.17 2.0.17 SHA-256/95cd71e852dfdb29126e58b7b939d57ec2b2e6024391852e0b545dee082bbf3b
            openrewrite/maven/org.sonatype.oss/oss-parent/pom/5 5 SHA-256/1678d4120a585d8a630131aeec4c524d928398583b7eab616ee7d5a87f520d3d
            openrewrite/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            openrewrite/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            openrewrite/maven/org.yaml/snakeyaml 2.6 SHA-256/c8f7a98e7394adda02f6317249710e4d1b4c7a25aa8c7eace0c2eea52eb8bf85
            openrewrite/maven/org.yaml/snakeyaml/pom/2.6 2.6 SHA-256/2773b0e948964d21e37b0e1dcf39025e627b873e0ce6aa2a47e23c374303e0c4
            """;
    private static final String UNUSED = """
            package sample;

            import java.util.List;
            import java.util.Map;

            public class Sample {
                public List<String> names() {
                    return List.of("Ada");
                }
            }
            """;

    @TempDir
    private Path root, project;

    @Test
    public void verify_mode_fails_the_build_and_leaves_the_file_untouched() throws IOException {
        Path sample = sample(UNUSED);

        assertThatThrownBy(newExecutor(true, List.of("org.openrewrite.java.RemoveUnusedImports"))::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
        assertThat(Files.readString(sample)).as("verify mode never rewrites the source").isEqualTo(UNUSED);
    }

    @Test
    public void applies_the_recipes_in_place_and_verifies_the_result() throws IOException {
        Path sample = sample(UNUSED);

        newExecutor(false, List.of("org.openrewrite.java.RemoveUnusedImports")).execute();

        assertThat(Files.readString(sample))
                .as("the unused import is removed and the used one kept")
                .contains("import java.util.List;")
                .doesNotContain("import java.util.Map;");

        newExecutor(true, List.of("org.openrewrite.java.RemoveUnusedImports")).execute();
    }

    @Test
    public void runs_a_recipe_that_the_project_declares_in_its_rewrite_yml() throws IOException {
        Path sample = sample(UNUSED);
        Files.writeString(project.resolve(OpenRewriteModule.CONFIGURATION), """
                type: specs.openrewrite.org/v1beta/recipe
                name: sample.Cleanup
                displayName: Sample cleanup
                recipeList:
                  - org.openrewrite.java.RemoveUnusedImports
                """);

        newExecutor(false, List.of("sample.Cleanup")).execute();

        assertThat(Files.readString(sample))
                .as("the declared recipe runs the recipes it lists")
                .doesNotContain("import java.util.Map;");
    }

    @Test
    public void runs_every_recipe_its_rewrite_yml_declares_when_it_names_none() throws IOException {
        Path sample = sample(UNUSED);
        Files.writeString(project.resolve(OpenRewriteModule.CONFIGURATION), """
                type: specs.openrewrite.org/v1beta/recipe
                name: sample.Cleanup
                displayName: Sample cleanup
                recipeList:
                  - org.openrewrite.java.RemoveUnusedImports
                """);

        newExecutor(false, List.of()).execute();

        assertThat(Files.readString(sample))
                .as("a rewrite.yml alone runs the recipes it declares")
                .doesNotContain("import java.util.Map;");
    }

    @Test
    public void refuses_to_run_without_a_recipe_or_a_rewrite_yml() throws IOException {
        sample(UNUSED);

        assertThatThrownBy(newExecutor(false, List.of())::execute)
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("names no recipe and is handed no rewrite.yml");
    }

    private Path sample(String content) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, content);
        return sample;
    }

    private BuildExecutor newExecutor(boolean verify, List<String> recipes) throws IOException {
        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        executor.addSource("project", project);
        executor.addModule(
                "openrewrite",
                new OpenRewriteModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .pinning(Pinning.STRICT)
                        .recipes(recipes)
                        .verify(verify),
                "project");
        return executor;
    }
}
