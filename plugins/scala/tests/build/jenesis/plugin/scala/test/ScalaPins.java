package build.jenesis.plugin.scala.test;

import module java.base;
import build.jenesis.BuildStep;
import build.jenesis.SequencedProperties;

final class ScalaPins {

    private static final String COMPILER = """
            scalac/maven/io.get-coursier/interface 1.0.29-M4 SHA-256/a9173f8348005f7a64ae4748854c473e8acdc3433afad0cd37d82167aec272fc
            scalac/maven/org.jline/jline-native 4.3.1 SHA-256/ff94ab4bccc3c1549444082ce07a2760cbf1ad4dae04e43508597bd802bcb231
            scalac/maven/org.jline/jline-reader 4.3.1 SHA-256/02aa899906086ce9d9c992bb05aed776045e5915a180e717dcb661fb87ab84d3
            scalac/maven/org.jline/jline-terminal 4.3.1 SHA-256/272927363471fdbf2f38710befbfe866acc1ed3472062a8ced02b16686f27bcc
            scalac/maven/org.jline/jline-terminal-jni 4.3.1 SHA-256/1c4873798f36412827087013e424199bced29da7c8421f3850cfe7f31f274c1c
            scalac/maven/org.ow2.asm/asm 9.10.1 SHA-256/ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb
            scalac/maven/org.ow2.asm/asm-analysis 9.10.1 SHA-256/dede75a21306b65974ecd8f87114ff6970f09fb794157a4ca09ab25c888c2bfc
            scalac/maven/org.ow2.asm/asm-commons 9.10.1 SHA-256/6d0abefb7cbf972ea16edb37ec14835372505063a45f976ab7ea889ed9497895
            scalac/maven/org.ow2.asm/asm-tree 9.10.1 SHA-256/3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b
            scalac/maven/org.ow2.asm/asm-util 9.10.1 SHA-256/1bb99d091fba2597dc6d51193e9bbcf0d8447e7ed96bd8f0198b18152f09655c
            scalac/maven/org.scala-lang/scala-library 3.10.0 SHA-256/b47befb29e02bbc08a10bbd387b35cf80ff9bf04fda8fdf3dda6c8ad6895f325
            scalac/maven/org.scala-lang/scala3-compiler_3 3.10.0 SHA-256/51e241938950723faebe420eb4d3bd4da3a1b686f6be574ad0ff4040a6b4bc1f
            scalac/maven/org.scala-lang/scala3-directives-parser_3 3.10.0 SHA-256/2d28f3366f452296134228b0a165ca33cf9a962ae97566085ba59c8031cc2b2b
            scalac/maven/org.scala-lang/scala3-interfaces 3.10.0 SHA-256/2293237c57462efcc200e1cf9ec7b5764c17458b3886689cc16c5ec9a5552ddf
            scalac/maven/org.scala-lang/scala3-library_3 3.10.0 SHA-256/4daaede24ddfeea082bbed096ba0c492d99e8428632e9fd3c38f82cf7aad4b63
            scalac/maven/org.scala-lang/scala3-repl_3 3.10.0 SHA-256/648c00039a5b4234ba5e618fe9df615c14cd87a679d3943d915f3ccd2a451f0f
            scalac/maven/org.scala-lang/scala3-sbt-bridge 3.10.0 SHA-256/c74f713b223693b361a5204c2d319b926565cf394370462fe7777e20c9022439
            scalac/maven/org.scala-lang/tasty-core_3 3.10.0 SHA-256/cd8088db4457e84610cfdf0381e8c2f92f409fb565ad5d792a3bdc7d37526058
            scalac/maven/org.scala-sbt/compiler-interface 1.12.0 SHA-256/92f487a505eafdd0a033e4ff4f38a81509a655c76257c64e6808dd873cc145c3
            scalac/maven/org.scala-sbt/util-interface 1.11.5 SHA-256/1fc950a9e1a35385e79316e23c120d4fd184f6a7af0274e00a703b0dc76376af
            scalac/maven/org.slf4j/slf4j-api 1.7.36 SHA-256/d3ef575e3e4979678dc01bf1dcce51021493b4d11fb7f1be8ad982877c16a1c0
            """, ZINC = """
            zinc/maven/com.eed3si9n/shaded-jawn-parser_2.13 1.3.2 SHA-256/f3273384cb4b94d600187e72037b815ecdaa5b44528bb06ec8dc4704d4892884
            zinc/maven/com.eed3si9n/shaded-scalajson_2.13 1.0.0-M4 SHA-256/7b6b6d85727bd8abab940b559de8e32aa5081add29f7531c855bb0761ae8de67
            zinc/maven/com.eed3si9n/sjson-new-core_2.13 0.9.0 SHA-256/67b5c99308d44fabd14402120c5b5ecbf7969b857cd4ce5229e07fdf851b2c7d
            zinc/maven/com.eed3si9n/sjson-new-scalajson_2.13 0.10.1 SHA-256/cdb3326ad10f4bae2752b1d6777859e74caa6b2f87b70a694fa23d03485b8e5d
            zinc/maven/com.lmax/disruptor 3.4.2 SHA-256/f412ecbb235c2460b45e63584109723dea8d94b819c78c9bfc38f50cba8546c0
            zinc/maven/com.swoval/file-tree-views 2.1.12 SHA-256/fd7373889b7a92cf3e97db36c920ba272aec158a9387b3259fca9f2dfaeda914
            zinc/maven/io.github.java-diff-utils/java-diff-utils 4.16 SHA-256/620403030d676a4a27f780a3acec7438dee1b1651a1c804fa6bb11bb07399a6f
            zinc/maven/net.openhft/zero-allocation-hashing 0.16 SHA-256/3bc39c640cc8314575de4ebcb1a0bca540516d3c60d49f8de7d638b09868553d
            zinc/maven/org.apache.logging.log4j/log4j-api 2.17.1 SHA-256/b0d8a4c8ab4fb8b1888d0095822703b0e6d4793c419550203da9e69196161de4
            zinc/maven/org.apache.logging.log4j/log4j-core 2.17.1 SHA-256/c967f223487980b9364e94a7c7f9a8a01fd3ee7c19bdbf0b0f9f8cb8511f3d41
            zinc/maven/org.fusesource.jansi/jansi 2.4.1 SHA-256/2e5e775a9dc58ffa6bbd6aa6f099d62f8b62dcdeb4c3c3bbbe5cf2301bc2dcc1
            zinc/maven/org.jline/jline-native 3.27.1 SHA-256/636646da40413e74963b3f4b40c9c5dcfb42f3d0ecf1b93d6ddf5c895effe422
            zinc/maven/org.jline/jline-terminal 3.27.1 SHA-256/42b7b756506bca98b505938426a35c99082f7819c333ed25bd4114bfb0e3b5f2
            zinc/maven/org.jline/jline-terminal-jni 3.27.1 SHA-256/e4bc64f1e68e9fb25d80517338e76abca1e0145b82e389b5c2b2c7d71d150f82
            zinc/maven/org.jline/jline/jar/jdk8 3.29.0 SHA-256/ed2680487642df95379f220c09c0f77e25095713387e2bc00c2d6581bb79c804
            zinc/maven/org.scala-lang.modules/scala-parallel-collections_2.13 0.2.0 SHA-256/d15f22f1308b98e9ac52a3d1ac8d582d548d6d852b1116cbdf5a50f431246ed1
            zinc/maven/org.scala-lang.modules/scala-parser-combinators_2.13 1.1.2 SHA-256/5c285b72e6dc0a98e99ae0a1ceeb4027dab9adfa441844046bd3f19e0efdcb54
            zinc/maven/org.scala-lang.modules/scala-xml_2.13 2.3.0 SHA-256/4b4d6698c74bff84a105102bbf58390980dc7bb8c40bdea4bc727040b3f966bd
            zinc/maven/org.scala-lang/scala-compiler 2.13.18 SHA-256/2f15891fcae7aad30a3892194fb2abb6224cf7ce5d2bd90fba7f1c48682fca21
            zinc/maven/org.scala-lang/scala-library 2.13.18 SHA-256/4e85d96ff7bc7dc627985523c3541b9917aaa08e956391380c42db21a2c4e5a0
            zinc/maven/org.scala-lang/scala-reflect 2.13.18 SHA-256/6935ff1982b2ac93d695f15aa66921be2f602921277afe002f018fd8c7d6e29b
            zinc/maven/org.scala-sbt.jline/jline 2.14.7-sbt-9a88bc413e2b34a4580c001c654d1a7f4f65bf18 SHA-256/a0dbaa781954fa5ed015706d12d0044e018a5705da6791ec98a51fb365d66cb0
            zinc/maven/org.scala-sbt/collections_2.13 1.12.4 SHA-256/80e1800426dc9de5bd3dbd52e83237cd64b1278639f18fca4a4f1a74346dfb80
            zinc/maven/org.scala-sbt/compiler-bridge_2.13 1.12.1 SHA-256/4c608ed219494576266f421ff6863a9d4ffc3ed167093226f6fea5063cc88434
            zinc/maven/org.scala-sbt/compiler-interface 1.12.1 SHA-256/8571d6dae6c3f7f4e782d0bda16ca832e06f749f60cd0e1c66d28dba621c27a9
            zinc/maven/org.scala-sbt/core-macros_2.13 1.12.4 SHA-256/37d044a643bdeb0340a6fa0d657c247cec905d43485eb648f9585a7ed96a16af
            zinc/maven/org.scala-sbt/io_2.13 1.10.5 SHA-256/fb16f4fb3fa0043d303faf0cb6d1a6b4f539a77b34949398527d732a4c05df57
            zinc/maven/org.scala-sbt/launcher-interface 1.4.4 SHA-256/aeb5bc134ecd37ca5532fd2630be65058b88fd7dd1c517cb4b52d316b52be2ce
            zinc/maven/org.scala-sbt/sbinary_2.13 0.5.1 SHA-256/29ffddb55471910f2c5f82a954683ce1fab3b094900b97adb1f777c44b1afccc
            zinc/maven/org.scala-sbt/util-control_2.13 1.12.4 SHA-256/6d8a9f952618bd7166219da26dd22deee2e4f7dcf8bab305b8e0fdcb02400fab
            zinc/maven/org.scala-sbt/util-interface 1.12.4 SHA-256/cd6916d1e622c3e662bf58d739babae022a582ec48107f5fb84b25cc56b0c3dc
            zinc/maven/org.scala-sbt/util-logging_2.13 1.12.4 SHA-256/455ef5fc777ba780512a02c874178fb8f029de064c04f1e8de75c56b63a2a27f
            zinc/maven/org.scala-sbt/util-position_2.13 1.12.4 SHA-256/2a999002e13f9d4e147d580669429bfed495b6b2b4c27a2254ce2ded579d3ba9
            zinc/maven/org.scala-sbt/util-relation_2.13 1.12.4 SHA-256/7c187a51584f50739d888d92add7592936888043546f47a916953e1e595f8d68
            zinc/maven/org.scala-sbt/zinc-apiinfo_2.13 1.12.1 SHA-256/854b4241d7c4811c44a514743d85bb6410ae70dc70a95569ea7c300a09942e07
            zinc/maven/org.scala-sbt/zinc-classfile_2.13 1.12.1 SHA-256/ca720b449b4d15c576f470a28911fe6c5768b5ec78a20a26d37b167f31253d81
            zinc/maven/org.scala-sbt/zinc-classpath_2.13 1.12.1 SHA-256/167ad63a8f06ac0c84edfc1b6f6be7a242989d725f5ea26f3ae53eb28a2ebde2
            zinc/maven/org.scala-sbt/zinc-compile-core_2.13 1.12.1 SHA-256/8d4c1fd49c1ade3725923960f00552f1717302bd5309b15a4b02957b277c8922
            zinc/maven/org.scala-sbt/zinc-core_2.13 1.12.1 SHA-256/66f2c043d06f4b9b1f39653e614a82b59a6cd7e6778a06078b63054ce6db77b1
            zinc/maven/org.scala-sbt/zinc-persist-core-assembly 1.12.1 SHA-256/1f029e2ab30d11544b8e553bf57d1e20a6e93d69298c2ef229438ab58ad4fddb
            zinc/maven/org.scala-sbt/zinc-persist_2.13 1.12.1 SHA-256/b8864e40d7499ece209c7fd66aef35ebab87f45ec2d7f211b3bdb0974948d381
            zinc/maven/org.scala-sbt/zinc_2.13 1.12.1 SHA-256/eb0ef70fe9ad0944bd0bfabf8ca69921047c4adce354b19b2ff3f0c3d86f1a1b
            """, SCALADOC = """
            scaladoc/maven/com.fasterxml.jackson.core/jackson-annotations 2.21 SHA-256/53ca085f4a150f703f49e1aabd935bd03b43e1ea3d55d135438292af22cef56b
            scaladoc/maven/com.fasterxml.jackson.core/jackson-core 2.13.2 SHA-256/9bfa278ad05179fb68087851caf607652702ca25424bec8358a3716e751405c8
            scaladoc/maven/com.fasterxml.jackson.core/jackson-databind 2.13.4.2 SHA-256/ba1a368137f9b92f48dd07f299ff7a782a69487b853ef5578215426fcde8f08b
            scaladoc/maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310 2.13.2 SHA-256/cf5034791afbbec5139d539c1a94194f172d52a516ad9c25aad5f235a937c747
            scaladoc/maven/com.vladsch.flexmark/flexmark 0.64.8 SHA-256/3133878d108f0e1964d7d427f379a7433e1b71a45f9f3c1fab7ed0ac0301d3b0
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-anchorlink 0.64.8 SHA-256/36d6849df8548a71b0f87eb97d3690864b960a8e4979fedf865418c8903453a6
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-autolink 0.64.8 SHA-256/4850d6f895ec3f2932635829d74ff9a2fb00918965bb91c000ab497d603e11b8
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-emoji 0.64.8 SHA-256/012d2db51463afd84702627e493fedce42eb7deb1bae309ddcefd181ab283cbe
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-gfm-strikethrough 0.64.8 SHA-256/fc6cad026fbd036b3ffd0448264360a5f1b572756ef3427ff022715b7fc5f1c3
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-gfm-tasklist 0.64.8 SHA-256/c86754ba8f0ffe997fa448da2a6ab2cfdbea9b381884cfed6f74bf513b77f5a9
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-ins 0.64.8 SHA-256/5b61b83c31cb0bd3273cbf12aed3887b63b8bdc6dfa17b03ecf479cf1ce176bd
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-superscript 0.64.8 SHA-256/457933324d1ca9f283bc15a1842777c6df0385f318fcceade4de41621395e402
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-tables 0.64.8 SHA-256/cdf82d26e112e96ade7e370138aeb16920b7e2d411d20c6c15cb1acfacd1ef1f
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-wikilink 0.64.8 SHA-256/f32ef779eefb4c3b80d5bebf3c4b7ad7206fc46e083a5901cf51db7b4535ff79
            scaladoc/maven/com.vladsch.flexmark/flexmark-ext-yaml-front-matter 0.64.8 SHA-256/6c60b85db4cc851301c1a52d712dc0909c5d2da9bd17ebae4d97a3a03afe22ee
            scaladoc/maven/com.vladsch.flexmark/flexmark-jira-converter 0.64.8 SHA-256/ccc1c3a9659a104b75a53a2321e9c1d4cedb05acd867d4366d8b8e1647bd37dd
            scaladoc/maven/com.vladsch.flexmark/flexmark-util 0.64.8 SHA-256/1e24c891dac9532bb722be7122c7026fb47868c3de36c02400eed8e9ad535727
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-ast 0.64.8 SHA-256/a5342f644c6a5f37d502f225c3a0cb699c263621c82e720d9c08eba5a9f3b8d3
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-builder 0.64.8 SHA-256/afb9c88d9f652451e75f1ab069ad7ddd791d712eb211c51d8d2a83494240875d
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-collection 0.64.8 SHA-256/031ff01408d0f54c6235db18d451dc6e6b15b859b0ec1e40ff3429a82375082a
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-data 0.64.8 SHA-256/83ceb5989ab0205054bf1b42446f55cb6da14c10aa7c2bda05d6a2504670f353
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-dependency 0.64.8 SHA-256/e41adb5e8144699b61b78d50eb004eeb7de84f90c4c63626609e6cee5ea28b42
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-format 0.64.8 SHA-256/6c9ac87686474edf0117b3f838d9dcb3b965fa72be4998d2177b598013679f73
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-html 0.64.8 SHA-256/5b19e6506aff7913c74ad725af7c2b8a9f23958e6324bf0710c5a645a4663017
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-misc 0.64.8 SHA-256/74472ca81a9080e51ea1a4f11c270898e3d6c242c92275fab47eb993806c8f2b
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-options 0.64.8 SHA-256/2489f41e9caf1d9f8061833b311b25403925cbb631fff2b8c069192804d6fc88
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-sequence 0.64.8 SHA-256/6e22976fe4e9b8de94fd529991ecb2d79cc482aad94985367470a93be1d82631
            scaladoc/maven/com.vladsch.flexmark/flexmark-util-visitor 0.64.8 SHA-256/d9b3002a833a078c45e139568b306dab0ac117ff9046c6a1f9e180c0154563fc
            scaladoc/maven/nl.big-o/liqp 0.9.2.3 SHA-256/1f7dc450a0e116dcf9be7f7285f531bd59c6f9a88a85377bb0f716678e2d838a
            scaladoc/maven/org.jetbrains/annotations 24.0.1 SHA-256/61666dbce7e42e6c85b43c04fcfb8293a21dcb55b3c80e869270ce42c01a6b35
            scaladoc/maven/org.jsoup/jsoup 1.22.2 SHA-256/596785996d3c6df16f544c232d10a9a1d88783b2a4740cf5251cb616f2be704d
            scaladoc/maven/org.nibor.autolink/autolink 0.6.0 SHA-256/a80be030f6386f18111cad9161c0b6983157352a1b59a59e6002172f0d321c04
            scaladoc/maven/org.ow2.asm/asm 9.10.1 SHA-256/ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb
            scaladoc/maven/org.ow2.asm/asm-analysis 9.10.1 SHA-256/dede75a21306b65974ecd8f87114ff6970f09fb794157a4ca09ab25c888c2bfc
            scaladoc/maven/org.ow2.asm/asm-commons 9.10.1 SHA-256/6d0abefb7cbf972ea16edb37ec14835372505063a45f976ab7ea889ed9497895
            scaladoc/maven/org.ow2.asm/asm-tree 9.10.1 SHA-256/3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b
            scaladoc/maven/org.ow2.asm/asm-util 9.10.1 SHA-256/1bb99d091fba2597dc6d51193e9bbcf0d8447e7ed96bd8f0198b18152f09655c
            scaladoc/maven/org.scala-lang/scala-library 3.10.0 SHA-256/b47befb29e02bbc08a10bbd387b35cf80ff9bf04fda8fdf3dda6c8ad6895f325
            scaladoc/maven/org.scala-lang/scala3-compiler_3 3.10.0 SHA-256/51e241938950723faebe420eb4d3bd4da3a1b686f6be574ad0ff4040a6b4bc1f
            scaladoc/maven/org.scala-lang/scala3-interfaces 3.10.0 SHA-256/2293237c57462efcc200e1cf9ec7b5764c17458b3886689cc16c5ec9a5552ddf
            scaladoc/maven/org.scala-lang/scala3-library_3 3.10.0 SHA-256/4daaede24ddfeea082bbed096ba0c492d99e8428632e9fd3c38f82cf7aad4b63
            scaladoc/maven/org.scala-lang/scala3-tasty-inspector_3 3.10.0 SHA-256/96a9e772a6d3f5c7e76f8fba9f6745cf6d68478c8c9346109b287de8cf957a12
            scaladoc/maven/org.scala-lang/scaladoc_3 3.10.0 SHA-256/2f1a34c894696858c0ccc97a4ea0d6400efb7a6ba6171281c50813c956a851af
            scaladoc/maven/org.scala-lang/tasty-core_3 3.10.0 SHA-256/cd8088db4457e84610cfdf0381e8c2f92f409fb565ad5d792a3bdc7d37526058
            scaladoc/maven/org.scala-sbt/compiler-interface 1.12.0 SHA-256/92f487a505eafdd0a033e4ff4f38a81509a655c76257c64e6808dd873cc145c3
            scaladoc/maven/org.scala-sbt/util-interface 1.11.5 SHA-256/1fc950a9e1a35385e79316e23c120d4fd184f6a7af0274e00a703b0dc76376af
            scaladoc/maven/org.snakeyaml/snakeyaml-engine 3.0.1 SHA-256/0b638c8806112215e02e58e95cbd9e9a69d4bd540991f47553d053919994cc94
            scaladoc/maven/tools.jackson.core/jackson-core 3.2.1 SHA-256/2688aaa60b207403287e16a71fb5644bf2f9cfc464cc601ae95c6c86a5af1f94
            scaladoc/maven/tools.jackson.core/jackson-databind 3.2.1 SHA-256/8d005c5626b1839e2eea6bd33a437287e7a24d54e1bb3746b8f861193262b519
            scaladoc/maven/tools.jackson.dataformat/jackson-dataformat-yaml 3.2.1 SHA-256/b6a748ca9aab640f647079b6bfa53f16a5b65b815a19581c4afe0601e754854e
            scaladoc/maven/ua.co.k/strftime4j 1.0.6 SHA-256/d37cabad2f97a746fa811f04c01c9e4b5f6636fff05e294f4e5324c051dd7495
            """;

    private ScalaPins() {
    }

    static void write(Path folder, String compiler, String zinc, String scaladoc) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(COMPILER.replaceAll("(?m)^scalac/", compiler + "/")
                + ZINC.replaceAll("(?m)^zinc/", zinc + "/")
                + SCALADOC.replaceAll("(?m)^scaladoc/", scaladoc + "/")));
        versions.store(folder.resolve(BuildStep.VERSIONS));
    }
}
