package build.jenesis.plugin.pmd.test;

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
import build.jenesis.plugin.pmd.PmdModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PmdModuleRunTest {

    private static final String PINS = """
            pmd/maven/com.github.nawforce/scala-json-rpc-upickle-json-serializer_2.13 1.1.0 SHA-256/4ce9d100d26080a7b8813b6c3c6bc568c7c33b500b2b42120862c3f9fda926ff
            pmd/maven/com.github.nawforce/scala-json-rpc-upickle-json-serializer_2.13/pom/1.1.0 1.1.0 SHA-256/6a62745311328c02c5a3f944eee56a91be94e03f8ba2118b0be2bde9a098850f
            pmd/maven/com.github.nawforce/scala-json-rpc_2.13 1.1.0 SHA-256/0fcac451d102a687a1be2d8cb3ad4854a0beb9a26398f01db03d95f9851fa3e1
            pmd/maven/com.github.nawforce/scala-json-rpc_2.13/pom/1.1.0 1.1.0 SHA-256/b6240588845e67a28382788dc5287fd7a122908a758e975bb82b7d909b155be3
            pmd/maven/com.github.oowekyala.ooxml/nice-xml-messages 3.1 SHA-256/0b4ceb5b8362d43c9c6c49b1bd57f1c5da54d3c5c7c305fa39c0a04462bb4799
            pmd/maven/com.github.oowekyala.ooxml/nice-xml-messages/pom/3.1 3.1 SHA-256/ad0aa36596cc4c0247fea54d5acc1e47b2b8d81e744c3c89e5dacb531d62a670
            pmd/maven/com.github.pathikrit/better-files_2.13 3.9.2 SHA-256/ba44cdde78320ea3800a8e99b5b3d0f3fc3bab0a9e9962c653b016d06e396022
            pmd/maven/com.github.pathikrit/better-files_2.13/pom/3.9.2 3.9.2 SHA-256/ad724a524dbb618d35f5bea7271faff9b2d4ca765f1d1b1f8da1d832807e549a
            pmd/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            pmd/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            pmd/maven/com.google.code.gson/gson 2.11.0 SHA-256/57928d6e5a6edeb2abd3770a8f95ba44dce45f3b23b7a9dc2b309c581552a78b
            pmd/maven/com.google.code.gson/gson-parent/pom/2.11.0 2.11.0 SHA-256/8acb1f3b72a6f026916ac0735bad9aab0293d527edb7b365327def13a9367b7a
            pmd/maven/com.google.code.gson/gson/pom/2.11.0 2.11.0 SHA-256/c0e547bea998888e6e25c5886a90e762272bc88b5275343dd2c05ded6ca2e360
            pmd/maven/com.google.errorprone/error_prone_annotations 2.27.0 SHA-256/24c923372c58e35d0b9f16a028929bb9aedc77521867c274f2bd0735df5ba1f5
            pmd/maven/com.google.errorprone/error_prone_annotations/pom/2.27.0 2.27.0 SHA-256/4ca5a35d61235e16549ac346d1e34551cdf0fe27e84aa57e03cbeb255ea4e5da
            pmd/maven/com.google.errorprone/error_prone_parent/pom/2.27.0 2.27.0 SHA-256/fa81829d049559df6927f9c9a6fd6bbd09f8b50e6d4736ae72c8300b6c3d7654
            pmd/maven/com.google.flogger/flogger 0.8 SHA-256/bebe7cd82be6c8d5208d6e960cd4344ea10672132ef06f5d4c71a48ab442b963
            pmd/maven/com.google.flogger/flogger-system-backend 0.8 SHA-256/eb4428e483c5332381778d78c6a19da63b4fef3fa7e40f62dadabea0d7600cb4
            pmd/maven/com.google.flogger/flogger-system-backend/pom/0.8 0.8 SHA-256/0bcbea7e9d983200b7453dedde3d9325e87a6f8bc7cd9bc89196099f889b0c94
            pmd/maven/com.google.flogger/flogger/pom/0.8 0.8 SHA-256/357e6d00bed5ea949671fee609c1ea168f0bff56da3aa68afe3015e7f02f5502
            pmd/maven/com.google.guava/failureaccess 1.0.2 SHA-256/8a8f81cf9b359e3f6dfa691a1e776985c061ef2f223c9b2c80753e1b458e8064
            pmd/maven/com.google.guava/failureaccess/pom/1.0.2 1.0.2 SHA-256/19ebc6f4bdb4edbb3d07b6ee994f846b54ef295582a9b5634719ffa9f31d03b2
            pmd/maven/com.google.guava/guava 33.0.0-jre SHA-256/f4d85c3e4d411694337cb873abea09b242b664bb013320be6105327c45991537
            pmd/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            pmd/maven/com.google.guava/guava-parent/pom/33.0.0-jre 33.0.0-jre SHA-256/040cc88c680b413d70ba9fc8371b36093021b10996aa3598621de767d418229a
            pmd/maven/com.google.guava/guava/pom/33.0.0-jre 33.0.0-jre SHA-256/fd70b14c441986c22e6d22ced2576787756be4918b145a8abd223e3a80b6e32d
            pmd/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            pmd/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            pmd/maven/com.google.j2objc/j2objc-annotations 2.8 SHA-256/f02a95fa1a5e95edb3ed859fd0fb7df709d121a35290eff8b74dce2ab7f4d6ed
            pmd/maven/com.google.j2objc/j2objc-annotations/pom/2.8 2.8 SHA-256/37f87798b18385113c918bfa9e1276fe50735ef8fa849b5800c519d54dbf11f8
            pmd/maven/com.google.protobuf/protobuf-bom/pom/3.19.6 3.19.6 SHA-256/f2da9bb752083b4c890d5ebd66314c4726f533aefe76f6c2427e9d71f2882311
            pmd/maven/com.google.protobuf/protobuf-java 3.19.6 SHA-256/6a9a2dff91dcf71f85be71ae971f6164b5a631dcd34bff08f0618535ca44ad02
            pmd/maven/com.google.protobuf/protobuf-java/pom/3.19.6 3.19.6 SHA-256/d1094eeb0db516a0500bb0bec590136e6068a83d0ff782b7c1c771a9e89d1da4
            pmd/maven/com.google.protobuf/protobuf-parent/pom/3.19.6 3.19.6 SHA-256/accde482306edbba9a877adfbb10680fea4c136b6e797e53ba9b8469ea25aa5c
            pmd/maven/com.google.summit/summit-ast 2.3.0 SHA-256/f8c489d1fcbfb45fdf0291f9acb1e3cec194209234a55048511ee4f54a607a49
            pmd/maven/com.google.summit/summit-ast/pom/2.3.0 2.3.0 SHA-256/473a22f51187539095e3455d0cae24982ac197bc5e7552ea595138b1a35e8204
            pmd/maven/com.lihaoyi/geny_2.13 0.6.2 SHA-256/26017fa73ec7fa3cd2d44a4f5d3462c58cd6ddebf7d42d40123768edf7dee43c
            pmd/maven/com.lihaoyi/geny_2.13/pom/0.6.2 0.6.2 SHA-256/5c730523e49f88f1365121a82ccaca1a457a04e94a48481a36ff263fcb1db184
            pmd/maven/com.lihaoyi/mainargs_2.13 0.5.4 SHA-256/f22a85b990fc68747b8caa51394a308c2778e9b230d69a3cdd79bb8bd7d1b562
            pmd/maven/com.lihaoyi/mainargs_2.13/pom/0.5.4 0.5.4 SHA-256/e5cddda3821ce2d99833701cce7b235357fb0102765c5f21ba694fa4201d39d9
            pmd/maven/com.lihaoyi/sourcecode_2.13 0.3.1 SHA-256/0e663be9a6c81e9515bdd07c04831397e38ea648a211e85ad005300fe840a03d
            pmd/maven/com.lihaoyi/sourcecode_2.13/pom/0.3.1 0.3.1 SHA-256/d1f068ae52eae497ebb876c5d42ea80662b2fe6bc31f0526631b29b3d9e7c887
            pmd/maven/com.lihaoyi/ujson_2.13 1.2.0 SHA-256/68474183a6ff657f97a91488e294cbe977b2a439fec216d5167700c72471f358
            pmd/maven/com.lihaoyi/ujson_2.13/pom/1.2.0 1.2.0 SHA-256/886edfcfa0d31589f17d51ff7fc1851523f34824b8f3223d40491d771e3323d7
            pmd/maven/com.lihaoyi/upack_2.13 1.2.0 SHA-256/1ed89d975c11ca0c87398f57e29ace72ff00835ba87b504a83c4190d17416c26
            pmd/maven/com.lihaoyi/upack_2.13/pom/1.2.0 1.2.0 SHA-256/fe6b161705aa661137f546f52b19070a9963e3eacb517e58e4cf4da5448dcbe4
            pmd/maven/com.lihaoyi/upickle-core_2.13 1.2.0 SHA-256/503d9d2687053a401f974e902ed095e7534f11f9b06448e03543f72c02f4e6bd
            pmd/maven/com.lihaoyi/upickle-core_2.13/pom/1.2.0 1.2.0 SHA-256/d0f5a865f27adc7c96e3ac8a49390c004e33371780f851fa5df164581d25f8c1
            pmd/maven/com.lihaoyi/upickle-implicits_2.13 1.2.0 SHA-256/31d16e260f6eae6c4172f430f2c0711f669cd9dff576aadd1370b8bc5472f8d4
            pmd/maven/com.lihaoyi/upickle-implicits_2.13/pom/1.2.0 1.2.0 SHA-256/f7789756bc0474a7c731931977b9b6ddba4becd18f83214aaf689f0df57fe2cd
            pmd/maven/com.lihaoyi/upickle_2.13 1.2.0 SHA-256/eba8ec18d8284cfcb10395842c254280b46f97ea2aca7f48b2b3db20205bae6f
            pmd/maven/com.lihaoyi/upickle_2.13/pom/1.2.0 1.2.0 SHA-256/1e8a2efa96cc03a85c07ce65b148ee8dc9bf53bab763e00c4d68931b290f640f
            pmd/maven/com.thesamet.scalapb/lenses_2.13 0.11.15 SHA-256/cf85c64803b72d2663b352301d292220ecdb86755897eb57054cd734bc3a35e3
            pmd/maven/com.thesamet.scalapb/lenses_2.13/pom/0.11.15 0.11.15 SHA-256/ccffd4c0524dffc81d832233da8915b71206c4433b4d16e253d88ebca97fd994
            pmd/maven/com.thesamet.scalapb/scalapb-runtime_2.13 0.11.15 SHA-256/7a5e7b4c6945d76bb05eb05905fe99b1fc6b58db3cc632d81ea7fb3879efbb77
            pmd/maven/com.thesamet.scalapb/scalapb-runtime_2.13/pom/0.11.15 0.11.15 SHA-256/f0d3d11025327498557d7f6b7b53c90e1088811a12526252e1b51e4f13cd4a0e
            pmd/maven/commons-codec/commons-codec 1.15 SHA-256/b3e9f6d63a790109bf0d056611fbed1cf69055826defeb9894a71369d246ed63
            pmd/maven/commons-codec/commons-codec/pom/1.15 1.15 SHA-256/c86ee198a35a3715487860f419cbf642e7e4d9e8714777947dbe6a4e3a20ab58
            pmd/maven/info.picocli/picocli 4.7.5 SHA-256/e83a906fb99b57091d1d68ac11f7c3d2518bd7a81a9c71b259e2c00d1564c8e8
            pmd/maven/info.picocli/picocli/pom/4.7.5 4.7.5 SHA-256/7e4e8b0f4b7ca607520b20d2f1e58df4f934ca636cb1eba8289cff2f2956bfd8
            pmd/maven/io.github.apex-dev-tools/apex-ls_2.13 5.2.0 SHA-256/091fa02fd19a159a757129e3278c1b1fee9c587a9e70aa97c28830aa502cfbde
            pmd/maven/io.github.apex-dev-tools/apex-ls_2.13/pom/5.2.0 5.2.0 SHA-256/71dfb5211898946f0b0cb2749c4b8552ed7c94c457dd61987fdc0e4b0125c4ff
            pmd/maven/io.github.apex-dev-tools/apex-parser 4.3.0 SHA-256/329270981e99e42ffee60369d6363614bb015fb611da4cdd9657978d217bc26e
            pmd/maven/io.github.apex-dev-tools/apex-parser/pom/4.3.0 4.3.0 SHA-256/aacefddedef0fd5eaaf6d1c61769ea6cd64e718797bbd8cd29b19d22ce2d5575
            pmd/maven/io.github.apex-dev-tools/apex-types_2.13 1.3.0 SHA-256/9d5bbd48a29abfc704d026382f7c9bb08c8bf0b114efcd065a31fb222f84a90f
            pmd/maven/io.github.apex-dev-tools/apex-types_2.13/pom/1.3.0 1.3.0 SHA-256/8744129b01721a1843273e231c752d31c9586249847e1c435846be07609b714e
            pmd/maven/io.github.apex-dev-tools/outline-parser_2.13 1.3.0 SHA-256/e86854d0aed6b509134fd356ad7d28d7c181f0216e053384b80b662741176313
            pmd/maven/io.github.apex-dev-tools/outline-parser_2.13/pom/1.3.0 1.3.0 SHA-256/d75e48f95e3b017f407483f1c6e415f116d623741a86a62921cdec4eb4c97cc7
            pmd/maven/io.github.apex-dev-tools/sobject-types 60.0.1 SHA-256/970ae1d4937e1395d85d6cb134cda5b0bab36a62175beca28cd4386fe8b7570c
            pmd/maven/io.github.apex-dev-tools/sobject-types/pom/60.0.1 60.0.1 SHA-256/9dcfc6451d4c56f1fe9394f74d6b3c4af792ec5f402df49f390888b4d860737b
            pmd/maven/io.github.apex-dev-tools/standard-types 60.0.1 SHA-256/488401133a155b9b9eb9c380a9d7b29a04ad63fd60f84378c398c76d3e8a33fa
            pmd/maven/io.github.apex-dev-tools/standard-types/pom/60.0.1 60.0.1 SHA-256/b989cc9c149b273003a5c2dbe80d4cd94f7424231dc835b20097225075c575c2
            pmd/maven/io.github.apex-dev-tools/vf-parser 1.1.0 SHA-256/9515efe1c7f81c205d01f508c82f432a72735d132fc6bb5a1ebd954d9f881457
            pmd/maven/io.github.apex-dev-tools/vf-parser/pom/1.1.0 1.1.0 SHA-256/6762a02a4fe0993a4f4e5243f8b238ffd02fa94b4d68418a2d9f8f7c9c9b5d9a
            pmd/maven/io.methvin/directory-watcher 0.18.0 SHA-256/18f67869b0d31d39512623226220abeedd6bde486d5599e6256eab7975110754
            pmd/maven/io.methvin/directory-watcher-better-files_2.13 0.18.0 SHA-256/839d3d970babacb606bb2f2e8461c2d7c40f6e36920753c41bf9561ba638d128
            pmd/maven/io.methvin/directory-watcher-better-files_2.13/pom/0.18.0 0.18.0 SHA-256/86c54f0fce6bc2c1931902829891515e02b7e5695ea6dece2f85e37f47b8dfd2
            pmd/maven/io.methvin/directory-watcher/pom/0.18.0 0.18.0 SHA-256/175fd31a1f0e18b312fdb00b8c2db364dced89bc83413653cf69795a813cf97c
            pmd/maven/javax.annotation/jsr250-api 1.0 SHA-256/a1a922d0d9b6d183ed3800dfac01d1e1eb159f0e8c6f94736931c1def54a941f
            pmd/maven/javax.annotation/jsr250-api/pom/1.0 1.0 SHA-256/548b0ef6f04356ef2283af5140d9404f38fd3891a509d468537abf2f9462944d
            pmd/maven/me.tongfei/progressbar 0.9.5 SHA-256/a1a086fa66f85c49bb3ca701a78cebb33647f367d4a5be8588c784d56272cc6e
            pmd/maven/me.tongfei/progressbar/pom/0.9.5 0.9.5 SHA-256/92cccb74509097ee705cb55f8f8d60c4405cefbc0e5880ef2c5c8f74cedca82c
            pmd/maven/net.java.dev.jna/jna 5.12.1 SHA-256/91a814ac4f40d60dee91d842e1a8ad874c62197984403d0e3c30d39e55cf53b3
            pmd/maven/net.java.dev.jna/jna/pom/5.12.1 5.12.1 SHA-256/65ff25849bad859554b50317792f5689adb4529ae4031e9a5246319cb2b85356
            pmd/maven/net.sf.saxon/Saxon-HE 12.5 SHA-256/98c3a91e6e5aaf9b3e2b37601e04b214a6e67098493cdd8232fcb705fddcb674
            pmd/maven/net.sf.saxon/Saxon-HE/pom/12.5 12.5 SHA-256/e0f3a8551db88b657e7856635b714eb630456405eeb2b161790eb28f18769ad7
            pmd/maven/net.sourceforge.pmd/pmd-ant 7.7.0 SHA-256/cd05dafecbcecca5bcb7946fa3c52d2c59b4ba1e273e6e6f45b522694b9381bd
            pmd/maven/net.sourceforge.pmd/pmd-ant/pom/7.7.0 7.7.0 SHA-256/166e492eb28b3ec3a9b4834f88e89f3eed38f10df48f57dbd7587a22d45cf06f
            pmd/maven/net.sourceforge.pmd/pmd-apex 7.7.0 SHA-256/57fe6e13b89dfb3bea34411139e9e6f188a549f15543060747dbeceafe72354a
            pmd/maven/net.sourceforge.pmd/pmd-apex/pom/7.7.0 7.7.0 SHA-256/a08a407d813fa623b248d475e6dddac4a08060562cf906753fabf30e2ede3453
            pmd/maven/net.sourceforge.pmd/pmd-cli 7.7.0 SHA-256/ba61729debeb6bbb7f8168230f8fdaf9c3315286a88e4fc0e19022823197a0e8
            pmd/maven/net.sourceforge.pmd/pmd-cli/pom/7.7.0 7.7.0 SHA-256/285fa0a8c3525559472dd8c67c49d14cb49216437c8272a8fb91df694552d6b0
            pmd/maven/net.sourceforge.pmd/pmd-coco 7.7.0 SHA-256/0106d7fb98d5f1cbdfe8162575baced8777b1c2e6f038d45c680ac095fd011cb
            pmd/maven/net.sourceforge.pmd/pmd-coco/pom/7.7.0 7.7.0 SHA-256/880cdc7ea0a9ada5e9d8d9678c870b3de4939415a219232e7b552fa3b09ea539
            pmd/maven/net.sourceforge.pmd/pmd-core 7.7.0 SHA-256/154a77eb6f32b00270c65f9796318252b0f25e5f394b1935b456fb8dc0de3563
            pmd/maven/net.sourceforge.pmd/pmd-core/pom/7.7.0 7.7.0 SHA-256/2a8d5d6d9372d4c476194f5fc7fca2c7e0febae31e303c1feb76e5da0393899e
            pmd/maven/net.sourceforge.pmd/pmd-cpp 7.7.0 SHA-256/7b61fcaf5f8ee9d66805880d528918e9dfba8c16225054bda9a0bc79fc5a1f3e
            pmd/maven/net.sourceforge.pmd/pmd-cpp/pom/7.7.0 7.7.0 SHA-256/513be791db76e3cd51e3d788b9e5dce1cc82485db5ec933bfd7c943e6c8b2da6
            pmd/maven/net.sourceforge.pmd/pmd-cs 7.7.0 SHA-256/9e39796ae011ad3ac3c981e9889d315ec5ded085ba0fa50ce4b85177f1a2a7f0
            pmd/maven/net.sourceforge.pmd/pmd-cs/pom/7.7.0 7.7.0 SHA-256/79ed13021a41d67118e8aeb05f25acdc0506063dec0802084e20c19b48921a14
            pmd/maven/net.sourceforge.pmd/pmd-dart 7.7.0 SHA-256/09ae3531d07c55bbaea37477f83a47636b27cdeb3cea1aba50e58d9c75cec601
            pmd/maven/net.sourceforge.pmd/pmd-dart/pom/7.7.0 7.7.0 SHA-256/f504813cfbeccc9846243f5d609eeaae97d038cfae83192ddee7138e66b48f83
            pmd/maven/net.sourceforge.pmd/pmd-designer 7.2.0 SHA-256/d7bdecaf6ea499ed0bf2767d638657b36f286f20ac2cc3bc498c25a8a71f1c85
            pmd/maven/net.sourceforge.pmd/pmd-designer/pom/7.2.0 7.2.0 SHA-256/653e944fbc92f16cb620586d09e256f7351f2bf9577b2c9bf6b8b72c66a8758b
            pmd/maven/net.sourceforge.pmd/pmd-dist 7.7.0 SHA-256/ab31c56321ceec536a8e0591a59645b33f486f3b2cdde7afa914c1261d3b2b23
            pmd/maven/net.sourceforge.pmd/pmd-dist/pom/7.7.0 7.7.0 SHA-256/d94dc2cfcaa8bf0c0f3561d4e23b77fc098a9ac0a43ab1963e00cb55381c2d21
            pmd/maven/net.sourceforge.pmd/pmd-fortran 7.7.0 SHA-256/cebbd0a9b825844639feabe29e9b3fbf4fc10688d86ad2700c63905730220bf4
            pmd/maven/net.sourceforge.pmd/pmd-fortran/pom/7.7.0 7.7.0 SHA-256/01dc8cd4f5196e2e728d61821591745d379755337d5072f144eb6c1a52366148
            pmd/maven/net.sourceforge.pmd/pmd-gherkin 7.7.0 SHA-256/18629e5a3ef54b579dc7fab1767e0e2784910dd9f28cc461a32c58bce69c0fea
            pmd/maven/net.sourceforge.pmd/pmd-gherkin/pom/7.7.0 7.7.0 SHA-256/87189795951db6c7768a7da23711799bd834a1c9d6da0dc146848856f483e0c0
            pmd/maven/net.sourceforge.pmd/pmd-go 7.7.0 SHA-256/933262390a5293dc2ec30df78106c33f6a68b24f2f5aa67991f8fd2df9f52e8a
            pmd/maven/net.sourceforge.pmd/pmd-go/pom/7.7.0 7.7.0 SHA-256/fc8ce20b701e693b80551cbe750f2785240c20c52a2320c22c8b0a75fea480df
            pmd/maven/net.sourceforge.pmd/pmd-groovy 7.7.0 SHA-256/82efcb0689c6876d6e2a5c3a42c27335e6901482f093ef2516c8e6b48eefb6e8
            pmd/maven/net.sourceforge.pmd/pmd-groovy/pom/7.7.0 7.7.0 SHA-256/d379a35a2b6e8d4628e814812be3cf6414bc451be568102ee719f1186bbee6b8
            pmd/maven/net.sourceforge.pmd/pmd-html 7.7.0 SHA-256/c4b51bba99f206a308bb39a2f12331413296317b35c91dcfcb6c605cefdc510a
            pmd/maven/net.sourceforge.pmd/pmd-html/pom/7.7.0 7.7.0 SHA-256/cf4292cdb30793308a1e7e111b3336f8e3ac3c9151d0935729b5d1a2af055aa8
            pmd/maven/net.sourceforge.pmd/pmd-java 7.7.0 SHA-256/95160f77a5ec3da2a3b5f4fe8968d0bfed8155c4ab583e530c49b7c9944eb968
            pmd/maven/net.sourceforge.pmd/pmd-java/pom/7.7.0 7.7.0 SHA-256/de56844156f1c00588e260de6704c37e67efed6fdab678e2ecf7393a11313de2
            pmd/maven/net.sourceforge.pmd/pmd-javascript 7.7.0 SHA-256/c93ba2e7943967e6ce541e6c07eca9db819dd242c99cf2ad8ce2a757af001a06
            pmd/maven/net.sourceforge.pmd/pmd-javascript/pom/7.7.0 7.7.0 SHA-256/f1b18bb9af8788eee03dde3ff3b7687ef25969b6f5603f16493492be04e31d57
            pmd/maven/net.sourceforge.pmd/pmd-jsp 7.7.0 SHA-256/e5d06fb8b46874a2ca14a21244d36f848edb4a85b3a1ed9010dd9367ae74aa96
            pmd/maven/net.sourceforge.pmd/pmd-jsp/pom/7.7.0 7.7.0 SHA-256/e0e202d350eee60a4cf4ac276b69d3e76fe46c012bf5c74238436a207672db63
            pmd/maven/net.sourceforge.pmd/pmd-julia 7.7.0 SHA-256/ee349032557dd1c8aafbac8d90b9a1b6d4b924bbb8520bebe5c92cfe4101d62f
            pmd/maven/net.sourceforge.pmd/pmd-julia/pom/7.7.0 7.7.0 SHA-256/add080883f0de0f30d81f785967d4d72e7da06cd69f674d29e24ff1406a3a271
            pmd/maven/net.sourceforge.pmd/pmd-kotlin 7.7.0 SHA-256/b2564f643d1f1349f3b052a8fd062fb26a8d49f3a0a6c7750da084eb60d1fd01
            pmd/maven/net.sourceforge.pmd/pmd-kotlin/pom/7.7.0 7.7.0 SHA-256/dfa6ad494c3d1989134bf65babe2ffd55dea0588878e61b49a22f32f158a48f1
            pmd/maven/net.sourceforge.pmd/pmd-languages-deps/pom 7.7.0 SHA-256/c2baf0a9a1fcc9f82b95c95692d8a75d9c147500cfee0c2d96e965b982d66bde
            pmd/maven/net.sourceforge.pmd/pmd-languages-deps/pom/7.7.0 7.7.0 SHA-256/c2baf0a9a1fcc9f82b95c95692d8a75d9c147500cfee0c2d96e965b982d66bde
            pmd/maven/net.sourceforge.pmd/pmd-lua 7.7.0 SHA-256/318f66b1f5e20caad10ce416bca3c2739d5a68c107fa1a507bd5bba4f0bb886b
            pmd/maven/net.sourceforge.pmd/pmd-lua/pom/7.7.0 7.7.0 SHA-256/5b0bb7f2a145f06f62118e9607afc7142129d7c1c879b4791b50219c1479742b
            pmd/maven/net.sourceforge.pmd/pmd-matlab 7.7.0 SHA-256/6c201adb0c1149483094e752265ec96fa0a57342307e68efe263f5f157cb3860
            pmd/maven/net.sourceforge.pmd/pmd-matlab/pom/7.7.0 7.7.0 SHA-256/9b0b0638c2f2d562fde59619083bb476b7e5bc44eaebbf286bef1a856f0f7ea7
            pmd/maven/net.sourceforge.pmd/pmd-modelica 7.7.0 SHA-256/6ee11e8bccad56aa0788363fdb77a2615206c4f327ee591221a8ad549e506b1b
            pmd/maven/net.sourceforge.pmd/pmd-modelica/pom/7.7.0 7.7.0 SHA-256/cbf918e3f4a49a05aefff5b2fa67a8d10bc6a31516e7e8d60c92ea2862d380b7
            pmd/maven/net.sourceforge.pmd/pmd-objectivec 7.7.0 SHA-256/48d86fc578ee92cccf76cac0c89364e2a4cd03d4ff1f34c74967597004bef6da
            pmd/maven/net.sourceforge.pmd/pmd-objectivec/pom/7.7.0 7.7.0 SHA-256/db6e0cc14f67bedbf750b54ab10009394b4e591ad5ab8358a7e337382ca24c43
            pmd/maven/net.sourceforge.pmd/pmd-perl 7.7.0 SHA-256/67dac2b726f4fe5f309fd2ab31e82e86417f5ad315af2cf99816ebdef7d47260
            pmd/maven/net.sourceforge.pmd/pmd-perl/pom/7.7.0 7.7.0 SHA-256/b6d1b359945cc8add3b05312fefeb25ee06604e9464e80d870bc4b1eb0bdde83
            pmd/maven/net.sourceforge.pmd/pmd-php 7.7.0 SHA-256/679ad2736ffb0067aa17a8c48ff30272606639e22982777a9cb9d59b56affe1a
            pmd/maven/net.sourceforge.pmd/pmd-php/pom/7.7.0 7.7.0 SHA-256/f499ddd387632d2cac15a0faffbc54f14d296760a02c75afb30a9df7a3ec5a5f
            pmd/maven/net.sourceforge.pmd/pmd-plsql 7.7.0 SHA-256/1176e9c9d32df1cac70baa1a677fc164466037e98cf9fb175e671d0d84f40130
            pmd/maven/net.sourceforge.pmd/pmd-plsql/pom/7.7.0 7.7.0 SHA-256/f7a8561d0d5934aa3ca363c677c265e87c17ad9ee762376fda5891641607d3da
            pmd/maven/net.sourceforge.pmd/pmd-python 7.7.0 SHA-256/266ad9374196ab188ed6e4488a0eab7f4d4c2c6ba5f4d5fc758ce8bdb2ab05aa
            pmd/maven/net.sourceforge.pmd/pmd-python/pom/7.7.0 7.7.0 SHA-256/dabae4a1430b9ab9219a1f7acb703bd3e3dfb804fd836ae09e6631423ba7c19d
            pmd/maven/net.sourceforge.pmd/pmd-ruby 7.7.0 SHA-256/9be3cdf1cb6365dbf43e5282ae25e0b523ebbefadd94a68b6106d4a7318f880b
            pmd/maven/net.sourceforge.pmd/pmd-ruby/pom/7.7.0 7.7.0 SHA-256/974b5b73579cdb51db0bd8b771776c0af2437c7375f5dfec46e6899dca39a553
            pmd/maven/net.sourceforge.pmd/pmd-scala-common/pom/7.7.0 7.7.0 SHA-256/6400ead69f654b4bd8760d0214fdd4310b29269f38bf6a8981df2a390777b26f
            pmd/maven/net.sourceforge.pmd/pmd-scala_2.13 7.7.0 SHA-256/0d14fd5efc914fbf1be59b643cd2c668662d24087090b05821f65400affb63ef
            pmd/maven/net.sourceforge.pmd/pmd-scala_2.13/pom/7.7.0 7.7.0 SHA-256/cb953e9cda354d2c5e10400fe908c3a93a9ad943178a1875842b9b37a13a19be
            pmd/maven/net.sourceforge.pmd/pmd-swift 7.7.0 SHA-256/662d3fbd5a3cefc1bd2e272475914a17da7f4e6663d8d3a5da012014d28d35fb
            pmd/maven/net.sourceforge.pmd/pmd-swift/pom/7.7.0 7.7.0 SHA-256/03a610c435bc9142c0476092dd44f7fe213ee11ef7d024464fbbfea8f750675f
            pmd/maven/net.sourceforge.pmd/pmd-tsql 7.7.0 SHA-256/c8515721ae6263006cdd7e87bf7033b0fbc51a48794cb8f17f3d1a702426115e
            pmd/maven/net.sourceforge.pmd/pmd-tsql/pom/7.7.0 7.7.0 SHA-256/558e4dc6c2f530c4597275ba0d0c73b4fea2975b21e0a3c06e5d5ec9b6a69e5f
            pmd/maven/net.sourceforge.pmd/pmd-velocity 7.7.0 SHA-256/69e3f8b9d1dbff1c3c9e4db2236fd84cf94fc3b3fd514cb3b151061990c61862
            pmd/maven/net.sourceforge.pmd/pmd-velocity/pom/7.7.0 7.7.0 SHA-256/14872fb7826f60b75465a1f141c4d215024d3d561382213a188fa85b75a0a1ba
            pmd/maven/net.sourceforge.pmd/pmd-visualforce 7.7.0 SHA-256/fed550e03961ad29486c01e8ce0d1f08ed7272007da986aef3df148b4630180a
            pmd/maven/net.sourceforge.pmd/pmd-visualforce/pom/7.7.0 7.7.0 SHA-256/097a8f55f0dff5d3a828e4f9eadadedb21c938975142f09ce0c7f9030830584b
            pmd/maven/net.sourceforge.pmd/pmd-xml 7.7.0 SHA-256/5635cdccad59e08a8018aa68005ec253934b8a769cab5dbced16587d11b7dbef
            pmd/maven/net.sourceforge.pmd/pmd-xml/pom/7.7.0 7.7.0 SHA-256/9f6e5a74988cd26658ba476b9d2e9673590ed613d0f98c051455915d45ff6bb5
            pmd/maven/net.sourceforge.pmd/pmd/pom/7.7.0 7.7.0 SHA-256/46a634132a775c599f8ece94d3318fe5b53f01780533dac0a7e6673f034a3452
            pmd/maven/org.antlr/antlr4-master/pom/4.9.3 4.9.3 SHA-256/565cc054d3badf50ca0a0717dae311268a2d6709fdddc90ac4a43c62d01935b9
            pmd/maven/org.antlr/antlr4-runtime 4.9.3 SHA-256/131a6594969bc4f321d652ea2a33bc0e378ca312685ef87791b2c60b29d01ea5
            pmd/maven/org.antlr/antlr4-runtime/pom/4.9.3 4.9.3 SHA-256/4f7e44e4ea0629fa3a759b1116feb9fb28814e95f791e1eeedd20c12889d7aa4
            pmd/maven/org.apache.commons/commons-lang3 3.14.0 SHA-256/7b96bf3ee68949abb5bc465559ac270e0551596fa34523fddf890ec418dde13c
            pmd/maven/org.apache.commons/commons-lang3/pom/3.14.0 3.14.0 SHA-256/110438863bad37c28f906bf87016e38c7a8c758ba321e09d11dc5a2363a8e79e
            pmd/maven/org.apache.commons/commons-parent/pom/52 52 SHA-256/75dbe8f34e98e4c3ff42daae4a2f9eb4cbcd3b5f1047d54460ace906dbb4502e
            pmd/maven/org.apache.commons/commons-parent/pom/64 64 SHA-256/6f19638994e8357b4ed734696f992057efaafa1235673998133299798e2ccddb
            pmd/maven/org.apache.groovy/groovy 4.0.19 SHA-256/96c650051f44cb6cdaff9d389d06eb881966d1c13d0067f69b39022cda926112
            pmd/maven/org.apache.groovy/groovy-bom/pom/4.0.19 4.0.19 SHA-256/0a7d668a721b43e7c9484bc51c5d5d0ac770ac114cd348f1b7d96fb323be3c35
            pmd/maven/org.apache.groovy/groovy/pom/4.0.19 4.0.19 SHA-256/d0b7bdee554c3ca1f6a1629fdb5a92447fb60473e374071348b39a0523b7e075
            pmd/maven/org.apache.httpcomponents.client5/httpclient5 5.1.3 SHA-256/28c759254f4e35319e078bb6ffea75676608dc12cb243b24fb3c8732522977fe
            pmd/maven/org.apache.httpcomponents.client5/httpclient5-parent/pom/5.1.3 5.1.3 SHA-256/a27b1413aece92a3aa4774915f7589e0c6179d728d2ac6a295d758ee4fa24cc5
            pmd/maven/org.apache.httpcomponents.client5/httpclient5/pom/5.1.3 5.1.3 SHA-256/1988ab3d1bdae0f51f22c83dc97b88fa07561adb6244679d8b8f7146cdd13aaf
            pmd/maven/org.apache.httpcomponents.core5/httpcore5 5.1.3 SHA-256/f2bf2f2c7772169c9e30699719667ad30f9b46c4e9d7841907deb2d12d9923fe
            pmd/maven/org.apache.httpcomponents.core5/httpcore5-h2 5.1.3 SHA-256/d0e78ba15aa8ebe77982b660ac4b09a95d6e035dbdbea762577dc1c8e2935807
            pmd/maven/org.apache.httpcomponents.core5/httpcore5-h2/pom/5.1.3 5.1.3 SHA-256/2bc0317a148edc9aefea3ec1535394efced3d137d62f7ff5656d0b03f94c0786
            pmd/maven/org.apache.httpcomponents.core5/httpcore5-parent/pom/5.1.3 5.1.3 SHA-256/a67538865ae0f3744b21e91ca47d461054737c552d1ff29da714c860c992d5bb
            pmd/maven/org.apache.httpcomponents.core5/httpcore5/pom/5.1.3 5.1.3 SHA-256/7fc2b8045809f3f27ac9d4d9e99b9d34621b6371cf93c7313ecd84a5af0e9bae
            pmd/maven/org.apache.httpcomponents/httpcomponents-parent/pom/12 12 SHA-256/4209f09593212987c29d6801917309dd5e6f71b53b2b1d0e0f0efb984ad11fa1
            pmd/maven/org.apache/apache/pom/23 23 SHA-256/bc10624e0623f36577fac5639ca2936d3240ed152fb6d8d533ab4d270543491c
            pmd/maven/org.apache/apache/pom/30 30 SHA-256/63dd4a393a9c0dfcb314efe83871a41d243bc8d200cbc7f2d197f30da78239d8
            pmd/maven/org.checkerframework/checker-compat-qual 2.5.3 SHA-256/d76b9afea61c7c082908023f0cbc1427fab9abd2df915c8b8a3e7a509bccbc6d
            pmd/maven/org.checkerframework/checker-compat-qual/pom/2.5.3 2.5.3 SHA-256/f7fcdac99eb33d169f6d52a35541c22ffd14f458abbcf56f9c49ee1486598c9c
            pmd/maven/org.checkerframework/checker-qual 3.48.1 SHA-256/21e8dfe8103e125d96a329653ca81e87ac430326dbdbf299cea3dc1ae3f039a2
            pmd/maven/org.checkerframework/checker-qual/pom/3.48.1 3.48.1 SHA-256/e2fb25082a82add284f60c1bff25d9301cd7e9b26138812cded1845eeb4227c9
            pmd/maven/org.danilopianini/gson-extras 1.3.0 SHA-256/a8bec65d0eb9d5fcda6410eea9993104f8c0852f8024972d8662b4dac8258c1d
            pmd/maven/org.danilopianini/gson-extras/pom/1.3.0 1.3.0 SHA-256/0537c06d826dbe4b8fdfa366be9320ff03246a6974a1ee367cd563beff237a5e
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib 1.9.24 SHA-256/858b902696da9cf585ab9d98ffc1c2712269828354dfe9107e3711b084a36468
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.9.24 SHA-256/b6699b850ba0789f2e904279cd8bdc7bea9130ffd157cdba001fc7425d8a47b7
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7/pom/1.9.24 1.9.24 SHA-256/4586a937d5bcbc3ab3042c0409a1c729614bcba3c7a7296f252d626ee37387d4
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.9.24 SHA-256/5b5bbfb3e1184b5e13317c3d42237fa24add443b2e7781961eea334db136adb1
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8/pom/1.9.24 1.9.24 SHA-256/06e06def49f96aaf6e5c3ec42836ae59d6e2da6254700914281675679dc9f2a5
            pmd/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/1.9.24 1.9.24 SHA-256/b9cead4c222dee90c74f6f013d316a9e50f811a0293313c3351aecb2f7cced5f
            pmd/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            pmd/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            pmd/maven/org.jline/jline 3.21.0 SHA-256/1e7d63a2bd1c26354ca1987e55469ea4327c4a3845c10d7a7790ca9729c49c02
            pmd/maven/org.jline/jline-parent/pom/3.21.0 3.21.0 SHA-256/98d561914ff908ec21d8b46bef89fb7af824d416dfd32b1ba71fa5d4a4f4c7a0
            pmd/maven/org.jline/jline/pom/3.21.0 3.21.0 SHA-256/0f11a07911f2e0687e120794a67536328b61e866a953cc1ea75b796c0b926c73
            pmd/maven/org.jsoup/jsoup 1.17.2 SHA-256/f60b33b38e9d7ac93eaaa68a6c70f706bb99036494b2e2add2bfee11d09ac6f5
            pmd/maven/org.jsoup/jsoup/pom/1.17.2 1.17.2 SHA-256/7a349d217790c3730be308ced1ea9ee32c4e74f72058e83c2b60e5a28954dd0d
            pmd/maven/org.junit/junit-bom/pom/5.10.0 5.10.0 SHA-256/e006dd8894f9fc7b75fc32bb12fe5ed8be65667d5b454f99e2e0b8c5bb8d30b3
            pmd/maven/org.junit/junit-bom/pom/5.11.2 5.11.2 SHA-256/f48e88538aac145eb3ae0345a9ebd055b28f329a35dce8d1e9281325ca9b0ea2
            pmd/maven/org.mozilla/rhino 1.7.15 SHA-256/2427fdcbc149ca0a25ccfbb7c71b01f39ad42708773a47816cd2342861766b63
            pmd/maven/org.mozilla/rhino/pom/1.7.15 1.7.15 SHA-256/fc5cc95ec945d7509f1b36321393d55ecd1e075e62fe993c848bf31e6139030f
            pmd/maven/org.ow2.asm/asm 9.7 SHA-256/adf46d5e34940bdf148ecdd26a9ee8eea94496a72034ff7141066b3eea5c4e9d
            pmd/maven/org.ow2.asm/asm/pom/9.7 9.7 SHA-256/de00115f1d84f3a0b2ee3a4b6f6192d066f86d185d67b9d1522f2c80feac5f00
            pmd/maven/org.ow2/ow2/pom/1.5.1 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
            pmd/maven/org.pcollections/pcollections 4.0.2 SHA-256/2bbeef5797a241300c4f7513cd546239629ed7deda4fc0c31df90bb95f5f13ef
            pmd/maven/org.pcollections/pcollections/pom/4.0.2 4.0.2 SHA-256/2478a453fb5384a2476dd2f8974072eb1d3fc5c5bab289dded55e7e0aed70c9f
            pmd/maven/org.scala-js/scalajs-stubs_2.13 1.0.0 SHA-256/60a58e75030081111da2e96e70140e6e370f2d1db7a353fe065b62eb757f82e3
            pmd/maven/org.scala-js/scalajs-stubs_2.13/pom/1.0.0 1.0.0 SHA-256/17fc7077237b83951f0e1ad1ab8d04b529fa17e2cf49f8fee5a4b04c0292af00
            pmd/maven/org.scala-lang.modules/scala-collection-compat_2.13 2.8.1 SHA-256/9b8cc6028dab5813fe751950382499d655fe8777e2c4b07368eaa9d1116e049c
            pmd/maven/org.scala-lang.modules/scala-collection-compat_2.13/pom/2.8.1 2.8.1 SHA-256/300d2f79ae8a5c82274ff90702355f0c8245d4423ad54e879dcb2b0341fcc40d
            pmd/maven/org.scala-lang.modules/scala-parallel-collections_2.13 1.0.0 SHA-256/fc08be49e91db44d7fe5c1ff95a322ad4500805a525cc2c4b1b91693f041c8e4
            pmd/maven/org.scala-lang.modules/scala-parallel-collections_2.13/pom/1.0.0 1.0.0 SHA-256/dc8cdbf1da9e639c0e4df76a368503d85c7ae85a5f1c3a29f6c4f9d7803e4759
            pmd/maven/org.scala-lang.modules/scala-xml_2.13 1.3.0 SHA-256/6d96d45a7fc6fc7ab69bdbac841b48cf67ab109f048c8db375ae4effae524f39
            pmd/maven/org.scala-lang.modules/scala-xml_2.13/pom/1.3.0 1.3.0 SHA-256/c690c17a60b00bba2b2acbf016793deda0a10fc3a84cb9b7d51b0c943d292387
            pmd/maven/org.scala-lang/scala-library 2.13.13 SHA-256/b58b223f9b59f88d04c9dc9ec26f4b463e93e6f296de5663dbd710a7dfce2743
            pmd/maven/org.scala-lang/scala-library/pom/2.13.13 2.13.13 SHA-256/fdc1a8e8a231d73a6f2258e2a48cf0f7fd7113366ee81c026b184dbc3021efe2
            pmd/maven/org.scala-lang/scala-reflect 2.13.10 SHA-256/62bd7a7198193c5373a992c122990279e413af3307162472a5d3cbb8ecadb35e
            pmd/maven/org.scala-lang/scala-reflect/pom/2.13.10 2.13.10 SHA-256/79d5c44035af6622b04e311ff41eacb04d0471b787b0d4c20b239b1d70d5e724
            pmd/maven/org.scalameta/common_2.13 4.9.1 SHA-256/223c3dc10561ffe4522bb29b2d5e93d2c3d12bbc81d896b6400a31a292e40ab3
            pmd/maven/org.scalameta/common_2.13/pom/4.9.1 4.9.1 SHA-256/0e67bd5460ee9068ac94bad76701540035c58a2e737151d948cca8290b7d60aa
            pmd/maven/org.scalameta/parsers_2.13 4.9.1 SHA-256/9240ab9c384572e1aa9cecef91b5a1276498a63b1d0794db7da72615aa0b44de
            pmd/maven/org.scalameta/parsers_2.13/pom/4.9.1 4.9.1 SHA-256/29570c5a245ee5e7f277a0e3d5829c875f8469ebddcafb67b6bfcf64c89e8762
            pmd/maven/org.scalameta/trees_2.13 4.9.1 SHA-256/d3a86bd56296d39fdf915c5f07629f60261852bd8127e11bef800e3b0c9d3e9e
            pmd/maven/org.scalameta/trees_2.13/pom/4.9.1 4.9.1 SHA-256/9bf77651003aafb3490af98604cb7a93e09157b426b3cc26cbddb2c4c8a3a231
            pmd/maven/org.slf4j/jul-to-slf4j 1.7.36 SHA-256/9e641fb142c5f0b0623d6222c09ea87523a41bf6bed48ac79940724010b989de
            pmd/maven/org.slf4j/jul-to-slf4j/pom/1.7.36 1.7.36 SHA-256/99ff5340c4642a30829d3d98253f123e7a3ef573483164477c88d89d7e6586b7
            pmd/maven/org.slf4j/slf4j-api 1.7.36 SHA-256/d3ef575e3e4979678dc01bf1dcce51021493b4d11fb7f1be8ad982877c16a1c0
            pmd/maven/org.slf4j/slf4j-api/pom/1.7.36 1.7.36 SHA-256/fb046a9c229437928bb11c2d27c8b5d773eb8a25e60cbd253d985210dedc2684
            pmd/maven/org.slf4j/slf4j-parent/pom/1.7.36 1.7.36 SHA-256/bb388d37fbcdd3cde64c3cede21838693218dc451f04040c5df360a78ed7e812
            pmd/maven/org.slf4j/slf4j-simple 1.7.36 SHA-256/2f39bed943d624dfa8f4102d0571283a10870b6aa36f197a8a506f147010c10f
            pmd/maven/org.slf4j/slf4j-simple/pom/1.7.36 1.7.36 SHA-256/c56b80a0a6bea2a0463e70d0892ae338a9e507e486767a520453409812058d1b
            pmd/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            pmd/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            pmd/maven/org.xmlresolver/xmlresolver 5.2.2 SHA-256/efc92bd7ed32b3e57095e0b3e872051ccfbbdcc980831ef33e89e38161a85222
            pmd/maven/org.xmlresolver/xmlresolver/jar/data 5.2.2 SHA-256/173904bdbd783ba0fac92c5bcc05da5d09f0ce7eed24346666ea0a239461f9b4
            pmd/maven/org.xmlresolver/xmlresolver/pom/5.2.2 5.2.2 SHA-256/e15a2922543b3a55c31774f10e83db74e93b2fa90b3d6315dd3a1c42a91f0074
            """;
    private static final String VERSION = "7.7.0";
    private static final String CONFIG = """
            <?xml version="1.0"?>
            <ruleset name="test"
                     xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
                <rule ref="category/java/bestpractices.xml/SystemPrintln"/>
            </ruleset>
            """;

    @TempDir
    private Path root, project;

    @Test
    public void downloads_the_pinned_pmd_and_writes_a_report() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("pmd.xml"), CONFIG);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.java"), """
                package sample;
                public class Sample {
                    public void run() {
                        System.out.println("hello");
                    }
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pmd",
                new PmdModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path resolved = root.resolve("pmd").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned PMD version resolves")
                    .anyMatch(name -> name.contains("pmd-core") && name.contains(VERSION));
        }
        Path report = root.resolve("pmd").resolve("check").resolve("output").resolve("reports").resolve("pmd").resolve("pmd-report.xml");
        assertThat(report).isNotEmptyFile();
        assertThat(report).content().contains("SystemPrintln");
    }

    @Test
    public void runs_no_rule_below_the_minimum_priority() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("pmd.xml"), """
                <?xml version="1.0"?>
                <ruleset name="test"
                         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
                    <rule ref="category/java/bestpractices.xml/SystemPrintln">
                        <priority>3</priority>
                    </rule>
                </ruleset>
                """);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.java"), """
                package sample;
                public class Sample {
                    public void run() {
                        System.out.println("hello");
                    }
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pmd",
                PmdModule.ofEnvironment(new Environment(Map.of("pmd.priority", "2", "strict.pmd", "true")),
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path report = root.resolve("pmd").resolve("check").resolve("output").resolve("reports").resolve("pmd").resolve("pmd-report.xml");
        assertThat(report).content()
                .as("a rule of priority 3 does not run when the minimum priority is 2, as maven-pmd-plugin's minimumPriority")
                .doesNotContain("SystemPrintln");
    }

    @Test
    public void fails_a_report_only_run_when_pmd_cannot_load_its_rule_set() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("pmd.xml"), """
                <?xml version="1.0"?>
                <ruleset name="test"
                         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
                    <rule ref="category/java/bestpractices.xml/NoSuchRule"/>
                </ruleset>
                """);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.java"), """
                package sample;
                public class Sample {
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pmd",
                new PmdModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");

        assertThatThrownBy(executor::execute)
                .rootCause()
                .as("report-only covers findings, not a PMD that never ran")
                .hasMessageContaining("Unexpected exit code");
    }

    private BuildExecutor newExecutor() throws IOException {
        return BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
    }
}
