package build.jenesis.plugin.checkstyle.test;

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
import build.jenesis.plugin.checkstyle.CheckstyleModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CheckstyleModuleRunTest {

    private static final String PINS = """
            checkstyle/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            checkstyle/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            checkstyle/maven/com.google.errorprone/error_prone_annotations 2.28.0 SHA-256/f3fc8a3a0a4020706a373b00e7f57c2512dd26d1f83d28c7d38768f8682b231e
            checkstyle/maven/com.google.errorprone/error_prone_annotations/pom/2.28.0 2.28.0 SHA-256/0ce909f13a568148476e5ee200f380f98c75ba08971aaf15db295eb77598ef3a
            checkstyle/maven/com.google.errorprone/error_prone_parent/pom/2.28.0 2.28.0 SHA-256/accefdbb5416cef5fcd2ddc37db4f1fcb34a2193cc1a55dfe5970a131b3e7683
            checkstyle/maven/com.google.guava/failureaccess 1.0.2 SHA-256/8a8f81cf9b359e3f6dfa691a1e776985c061ef2f223c9b2c80753e1b458e8064
            checkstyle/maven/com.google.guava/failureaccess/pom/1.0.2 1.0.2 SHA-256/19ebc6f4bdb4edbb3d07b6ee994f846b54ef295582a9b5634719ffa9f31d03b2
            checkstyle/maven/com.google.guava/guava 33.3.1-jre SHA-256/4bf0e2c5af8e4525c96e8fde17a4f7307f97f8478f11c4c8e35a0e3298ae4e90
            checkstyle/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            checkstyle/maven/com.google.guava/guava-parent/pom/33.3.1-jre 33.3.1-jre SHA-256/55441db27e8869dfefe053059bdf478bdc7e95585642bf391f0023345fd56287
            checkstyle/maven/com.google.guava/guava/pom/33.3.1-jre 33.3.1-jre SHA-256/313b67fc13eb3b0634eda715a1229971f5de9b8188bc280762815b83a275f193
            checkstyle/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            checkstyle/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            checkstyle/maven/com.google.j2objc/j2objc-annotations 3.0.0 SHA-256/88241573467ddca44ffd4d74aa04c2bbfd11bf7c17e0c342c94c9de7a70a7c64
            checkstyle/maven/com.google.j2objc/j2objc-annotations/pom/3.0.0 3.0.0 SHA-256/23b3d039e168ad89dd114698e6dd7be383f4a2c577b8877d82c73a6515e74a17
            checkstyle/maven/com.puppycrawl.tools/checkstyle 10.21.0 SHA-256/50a553ca004d048ff7b455bc902cf6501b3005a68e31f55348c221387a2823b5
            checkstyle/maven/com.puppycrawl.tools/checkstyle/pom/10.21.0 10.21.0 SHA-256/460262291c6581133201ef74ae3d9f49699d3e864b8085d0dd10aa23ab115f9d
            checkstyle/maven/commons-beanutils/commons-beanutils 1.9.4 SHA-256/7d938c81789028045c08c065e94be75fc280527620d5bd62b519d5838532368a
            checkstyle/maven/commons-beanutils/commons-beanutils/pom/1.9.4 1.9.4 SHA-256/c35cca7b61d4678d9578cbc0b901b8717b539abf9254441da78b8fe60de064d0
            checkstyle/maven/commons-codec/commons-codec 1.15 SHA-256/b3e9f6d63a790109bf0d056611fbed1cf69055826defeb9894a71369d246ed63
            checkstyle/maven/commons-codec/commons-codec/pom/1.15 1.15 SHA-256/c86ee198a35a3715487860f419cbf642e7e4d9e8714777947dbe6a4e3a20ab58
            checkstyle/maven/commons-collections/commons-collections 3.2.2 SHA-256/eeeae917917144a68a741d4c0dff66aa5c5c5fd85593ff217bced3fc8ca783b8
            checkstyle/maven/commons-collections/commons-collections/pom/3.2.2 3.2.2 SHA-256/d5d81fcc288c0d8c711c302007cada4aa9a226ed1a112d4baa64cb1d6322170b
            checkstyle/maven/commons-logging/commons-logging 1.2 SHA-256/daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636
            checkstyle/maven/commons-logging/commons-logging/pom/1.2 1.2 SHA-256/c91ab5aa570d86f6fd07cc158ec6bc2c50080402972ee9179fe24100739fbb20
            checkstyle/maven/info.picocli/picocli 4.7.6 SHA-256/ed441183f309b93f104ca9e071e314a4062a893184e18a3c7ad72ec9cba12ba0
            checkstyle/maven/info.picocli/picocli/pom/4.7.6 4.7.6 SHA-256/2d2248cccf487ad9ce208dec751745df15cd1e1924a4cc3d36841b5f1990c6fb
            checkstyle/maven/net.sf.saxon/Saxon-HE 12.5 SHA-256/98c3a91e6e5aaf9b3e2b37601e04b214a6e67098493cdd8232fcb705fddcb674
            checkstyle/maven/net.sf.saxon/Saxon-HE/pom/12.5 12.5 SHA-256/e0f3a8551db88b657e7856635b714eb630456405eeb2b161790eb28f18769ad7
            checkstyle/maven/org.antlr/antlr4-master/pom/4.13.2 4.13.2 SHA-256/0adda026685873f651360178bffc446ceee4833081739e3ae9d6e8f07e8d902a
            checkstyle/maven/org.antlr/antlr4-runtime 4.13.2 SHA-256/dd3e8a13a2d669bf84fb8d834de35ce4875f27157698d206241ec8488aadcaf7
            checkstyle/maven/org.antlr/antlr4-runtime/pom/4.13.2 4.13.2 SHA-256/03ce07a2796c511b0c94dc14fd86ccdd6e3828c57967ad2383de304e0d11ba79
            checkstyle/maven/org.apache.commons/commons-lang3 3.8.1 SHA-256/dac807f65b07698ff39b1b07bfef3d87ae3fd46d91bbf8a2bc02b2a831616f68
            checkstyle/maven/org.apache.commons/commons-lang3/pom/3.8.1 3.8.1 SHA-256/ec8e09f75411685205bd0d9d7872cc3622e67c76df44a0a227b278bea04458d5
            checkstyle/maven/org.apache.commons/commons-parent/pom/34 34 SHA-256/3a2e69d06d641d1f3b293126dc9e2e4ea6563bf8c36c87e0ab6fa4292d04b79c
            checkstyle/maven/org.apache.commons/commons-parent/pom/39 39 SHA-256/87cd27e1a02a5c3eb6d85059ce98696bb1b44c2b8b650f0567c86df60fa61da7
            checkstyle/maven/org.apache.commons/commons-parent/pom/45 45 SHA-256/9c88623ecfa91f012c673ee4622c0eeb4367e5e0d222d960f76cd208b1a4fda6
            checkstyle/maven/org.apache.commons/commons-parent/pom/47 47 SHA-256/8a8ecb570553bf9f1ffae211a8d4ca9ee630c17afe59293368fba7bd9b42fcb7
            checkstyle/maven/org.apache.commons/commons-parent/pom/52 52 SHA-256/75dbe8f34e98e4c3ff42daae4a2f9eb4cbcd3b5f1047d54460ace906dbb4502e
            checkstyle/maven/org.apache.commons/commons-text 1.3 SHA-256/8185b3a5311092d83ed1f184c2d093b3105d726bbd76867c32b3511542bb99a8
            checkstyle/maven/org.apache.commons/commons-text/pom/1.3 1.3 SHA-256/deeb2ba9701e495dc31d32633eb86b94b24b0a96e0dc67fb87e7062f153027aa
            checkstyle/maven/org.apache.geronimo.genesis/genesis-default-flava/pom/2.0 2.0 SHA-256/08e6e146f4e245266dff9dd800b12875dd2367d3f67a34ab660a126c811215f8
            checkstyle/maven/org.apache.geronimo.genesis/genesis-java5-flava/pom/2.0 2.0 SHA-256/e7c5358bbbbc27daa5b327f63bc12650a75ddc9f9ac6d4bfa12789be1f2c29db
            checkstyle/maven/org.apache.geronimo.genesis/genesis/pom/2.0 2.0 SHA-256/b9ed2f9dd434631158d77308ee5648622c229ccece1722c5e377a4296923dae6
            checkstyle/maven/org.apache.httpcomponents.client5/httpclient5 5.1.3 SHA-256/28c759254f4e35319e078bb6ffea75676608dc12cb243b24fb3c8732522977fe
            checkstyle/maven/org.apache.httpcomponents.client5/httpclient5-parent/pom/5.1.3 5.1.3 SHA-256/a27b1413aece92a3aa4774915f7589e0c6179d728d2ac6a295d758ee4fa24cc5
            checkstyle/maven/org.apache.httpcomponents.client5/httpclient5/pom/5.1.3 5.1.3 SHA-256/1988ab3d1bdae0f51f22c83dc97b88fa07561adb6244679d8b8f7146cdd13aaf
            checkstyle/maven/org.apache.httpcomponents.core5/httpcore5 5.1.3 SHA-256/f2bf2f2c7772169c9e30699719667ad30f9b46c4e9d7841907deb2d12d9923fe
            checkstyle/maven/org.apache.httpcomponents.core5/httpcore5-h2 5.1.3 SHA-256/d0e78ba15aa8ebe77982b660ac4b09a95d6e035dbdbea762577dc1c8e2935807
            checkstyle/maven/org.apache.httpcomponents.core5/httpcore5-h2/pom/5.1.3 5.1.3 SHA-256/2bc0317a148edc9aefea3ec1535394efced3d137d62f7ff5656d0b03f94c0786
            checkstyle/maven/org.apache.httpcomponents.core5/httpcore5-parent/pom/5.1.3 5.1.3 SHA-256/a67538865ae0f3744b21e91ca47d461054737c552d1ff29da714c860c992d5bb
            checkstyle/maven/org.apache.httpcomponents.core5/httpcore5/pom/5.1.3 5.1.3 SHA-256/7fc2b8045809f3f27ac9d4d9e99b9d34621b6371cf93c7313ecd84a5af0e9bae
            checkstyle/maven/org.apache.httpcomponents/httpclient 4.5.13 SHA-256/6fe9026a566c6a5001608cf3fc32196641f6c1e5e1986d1037ccdbd5f31ef743
            checkstyle/maven/org.apache.httpcomponents/httpclient/pom/4.5.13 4.5.13 SHA-256/78eb9ada74929fcd63d07adc4f49236841a45cc29d5f817bf45801f513fd7e6c
            checkstyle/maven/org.apache.httpcomponents/httpcomponents-client/pom/4.5.13 4.5.13 SHA-256/9cba594c08db7271d0c20e9845d622bb39e69583910b45e7d5df82f6058d4dd9
            checkstyle/maven/org.apache.httpcomponents/httpcomponents-core/pom/4.4.14 4.4.14 SHA-256/209ed931cb57998252dfe027caa9c03ada620613642329679131165f8b1cbad1
            checkstyle/maven/org.apache.httpcomponents/httpcomponents-parent/pom/11 11 SHA-256/a901f87b115c55070c7ee43efff63e20e7b02d30af2443ae292bf1f4e532d3aa
            checkstyle/maven/org.apache.httpcomponents/httpcomponents-parent/pom/12 12 SHA-256/4209f09593212987c29d6801917309dd5e6f71b53b2b1d0e0f0efb984ad11fa1
            checkstyle/maven/org.apache.httpcomponents/httpcore 4.4.14 SHA-256/f956209e450cb1d0c51776dfbd23e53e9dd8db9a1298ed62b70bf0944ba63b28
            checkstyle/maven/org.apache.httpcomponents/httpcore/pom/4.4.14 4.4.14 SHA-256/55716398a978f10203f9e25c8aefc0580daf7f0907c6ed0aead81ec5fb6b7fd8
            checkstyle/maven/org.apache.maven.doxia/doxia-core 1.12.0 SHA-256/5e49cd827bebbcea5829d3b3883d17ad1ce15ebd6394aeb50ad50d7dfd939fcd
            checkstyle/maven/org.apache.maven.doxia/doxia-core/pom/1.12.0 1.12.0 SHA-256/b0388f748a0885e13e7c2c453921f8bb9dd4fb5b1905be745685476cf3456ea3
            checkstyle/maven/org.apache.maven.doxia/doxia-logging-api 1.12.0 SHA-256/985306162c0a9f4c309d46109447f30f02bf6fc9bc16a3e039d59e1dabd0192f
            checkstyle/maven/org.apache.maven.doxia/doxia-logging-api/pom/1.12.0 1.12.0 SHA-256/9dd99b4350223846585310694c446318b1692ba746ed717376d59619a27123b4
            checkstyle/maven/org.apache.maven.doxia/doxia-module-xdoc 1.12.0 SHA-256/e8731ba00a4edd34b20eff9e4a729c2045c62cb796c3e491692607de4476ab01
            checkstyle/maven/org.apache.maven.doxia/doxia-module-xdoc/pom/1.12.0 1.12.0 SHA-256/fb69d95be4b55af2f3b0a9b68e3e8e62063e69594c1fcebe705a5a4db0996d25
            checkstyle/maven/org.apache.maven.doxia/doxia-modules/pom/1.12.0 1.12.0 SHA-256/ab8ff6bb4793cfba59b14fb383ff351a349b1091d071c66b1fcbca728d505bdc
            checkstyle/maven/org.apache.maven.doxia/doxia-sink-api 1.12.0 SHA-256/5dca6aaaa9e70d8a0766e143ddcf9db09de5fde0fbcc78cb635d74e764dfcca5
            checkstyle/maven/org.apache.maven.doxia/doxia-sink-api/pom/1.12.0 1.12.0 SHA-256/26b51fddb69b5ca6e044cda28b8d1c1a37b6fafd0cfb9bfb15a919de57c671d5
            checkstyle/maven/org.apache.maven.doxia/doxia/pom/1.12.0 1.12.0 SHA-256/2d02a0bd64f331b767cee7455b3193c6fb82110143a119858abc968798a5fc29
            checkstyle/maven/org.apache.maven/maven-parent/pom/34 34 SHA-256/1a8faf7a6a2b848acb26a959954ee115c0d79dbe75a6206fb3b8c7c2f45a237f
            checkstyle/maven/org.apache.xbean/xbean-reflect 3.7 SHA-256/104e5e9bb5a669f86722f32281960700f7ec8e3209ef51b23eb9b6d23d1629cb
            checkstyle/maven/org.apache.xbean/xbean-reflect/pom/3.7 3.7 SHA-256/9795c15322c5d19af336eebd9e164fa7d5897c4b004a7d66e21635a173e748a9
            checkstyle/maven/org.apache.xbean/xbean/pom/3.7 3.7 SHA-256/ea717132de8404b62fc9097a1f3215c705d525d457be9a5f12653adc68c33ab7
            checkstyle/maven/org.apache/apache/pom/13 13 SHA-256/ff513db0361fd41237bef4784968bc15aae478d4ec0a9496f811072ccaf3841d
            checkstyle/maven/org.apache/apache/pom/16 16 SHA-256/9f85ff2fd7d6cb3097aa47fb419ee7f0ebe869109f98aba9f4eca3f49e74a40e
            checkstyle/maven/org.apache/apache/pom/19 19 SHA-256/91f7a33096ea69bac2cbaf6d01feb934cac002c48d8c8cfa9c240b40f1ec21df
            checkstyle/maven/org.apache/apache/pom/21 21 SHA-256/af10c108da014f17cafac7b52b2b4b5a3a1c18265fa2af97a325d9143537b380
            checkstyle/maven/org.apache/apache/pom/23 23 SHA-256/bc10624e0623f36577fac5639ca2936d3240ed152fb6d8d533ab4d270543491c
            checkstyle/maven/org.apache/apache/pom/6 6 SHA-256/12edb5096e13f40c362d0bd40902589fa9586505123fa26799ce50b116fa5bb3
            checkstyle/maven/org.checkerframework/checker-qual 3.48.3 SHA-256/443685b1b232803baaf803c15d6f5a425473c6f7b81c5f276dfcf93288e389a5
            checkstyle/maven/org.checkerframework/checker-qual/pom/3.48.3 3.48.3 SHA-256/b5ef91390223cda7d029e96b2b0c1c64f555bb401df657ea2cb9bb7c496ba9b1
            checkstyle/maven/org.codehaus.plexus/plexus-classworlds 2.6.0 SHA-256/52f77c5ec49f787c9c417ebed5d6efd9922f44a202f217376e4f94c0d74f3549
            checkstyle/maven/org.codehaus.plexus/plexus-classworlds/pom/2.6.0 2.6.0 SHA-256/469a6c59f92effa62c0797ce7d52d2c03cf8ee1034b923c360dd78a9f505a7ba
            checkstyle/maven/org.codehaus.plexus/plexus-component-annotations 2.1.0 SHA-256/bde3617ce9b5bcf9584126046080043af6a4b3baea40a3b153f02e7bbc32acac
            checkstyle/maven/org.codehaus.plexus/plexus-component-annotations/pom/2.1.0 2.1.0 SHA-256/0670b605255f7dc9a454daaec7912918ccf1b5475cbfca374363b51fcfd4ea00
            checkstyle/maven/org.codehaus.plexus/plexus-container-default 2.1.0 SHA-256/6dceb1246b188153bdcb6f962d543d51ddb672cca07cad94a78fbabc9edf0a39
            checkstyle/maven/org.codehaus.plexus/plexus-container-default/pom/2.1.0 2.1.0 SHA-256/8b8c20e630bdcc795cc985024c4c1045c147be116052c2c975e30c608f7f3b45
            checkstyle/maven/org.codehaus.plexus/plexus-containers/pom/2.1.0 2.1.0 SHA-256/94d5aedb3c46023265396527cf8ce7fc944b7bd79e4ebab907386418eb5a08d7
            checkstyle/maven/org.codehaus.plexus/plexus-utils 3.3.0 SHA-256/76d174792540e2775af94d03d10fb2d3c776e2cd0ac0ebf427d3e570072bb9ce
            checkstyle/maven/org.codehaus.plexus/plexus-utils/pom/3.3.0 3.3.0 SHA-256/79c9792073fdee3cdbebd61a76ba8c2dd11624a9f85d128bae56bda19e20475c
            checkstyle/maven/org.codehaus.plexus/plexus/pom/5.1 5.1 SHA-256/a343e44ff5796aed0ea60be11454c935ce20ab1c5f164acc8da574482dcbc7e9
            checkstyle/maven/org.javassist/javassist 3.28.0-GA SHA-256/57d0a9e9286f82f4eaa851125186997f811befce0e2060ff0a15a77f5a9dd9a7
            checkstyle/maven/org.javassist/javassist/pom/3.28.0-GA 3.28.0-GA SHA-256/c36a7c13da3a4852aa881bdf9db60b9e4d1af146ec2afb5398f96d5983f6d5dd
            checkstyle/maven/org.reflections/reflections 0.10.2 SHA-256/938a2d08fe54050d7610b944d8ddc3a09355710d9e6be0aac838dbc04e9a2825
            checkstyle/maven/org.reflections/reflections/pom/0.10.2 0.10.2 SHA-256/b6caa3eb7d35bd756ed6eb0a2a81868b8d3c0f6f42244feae41760ac66306d87
            checkstyle/maven/org.slf4j/slf4j-api 1.7.32 SHA-256/3624f8474c1af46d75f98bc097d7864a323c81b3808aa43689a6e1c601c027be
            checkstyle/maven/org.slf4j/slf4j-api/pom/1.7.32 1.7.32 SHA-256/001cde5b3c6ba91070425cfe9f2e695e4aeb8bc290a2d4cd96531127ab244fe5
            checkstyle/maven/org.slf4j/slf4j-parent/pom/1.7.32 1.7.32 SHA-256/5ab349d0f4c7bc08ed0ef1f4d9386cb1940a2f4d6f152150e16dbbecc0b83c70
            checkstyle/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            checkstyle/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            checkstyle/maven/org.xmlresolver/xmlresolver 5.2.2 SHA-256/efc92bd7ed32b3e57095e0b3e872051ccfbbdcc980831ef33e89e38161a85222
            checkstyle/maven/org.xmlresolver/xmlresolver/jar/data 5.2.2 SHA-256/173904bdbd783ba0fac92c5bcc05da5d09f0ce7eed24346666ea0a239461f9b4
            checkstyle/maven/org.xmlresolver/xmlresolver/pom/5.2.2 5.2.2 SHA-256/e15a2922543b3a55c31774f10e83db74e93b2fa90b3d6315dd3a1c42a91f0074
            """;
    private static final String VERSION = "10.21.0";
    private static final String CONFIG = """
            <?xml version="1.0"?>
            <!DOCTYPE module PUBLIC
                "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                "https://checkstyle.org/dtds/configuration_1_3.dtd">
            <module name="Checker">
                <property name="severity" value="error"/>
                <module name="TreeWalker">
                    <module name="TypeName"/>
                </module>
            </module>
            """;

    @TempDir
    private Path root, project;

    @BeforeEach
    public void writeProject() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("checkstyle.xml"), CONFIG);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("badName.java"), """
                package sample;
                public class badName {
                }
                """);
    }

    @Test
    public void writes_a_report_without_failing_the_build_in_report_only_mode() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path resolved = root.resolve("checkstyle").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned Checkstyle version is the one that resolves, not a floated RELEASE")
                    .anyMatch(name -> name.contains("checkstyle") && name.contains(VERSION));
        }
        Path report = root.resolve("checkstyle").resolve("check").resolve("output").resolve("reports").resolve("checkstyle").resolve("checkstyle-report.xml");
        assertThat(report)
                .as("report-only run still produces the Checkstyle XML report")
                .isNotEmptyFile();
        assertThat(report).content().contains("badName");
    }

    @Test
    public void strict_mode_fails_the_build_on_a_violation() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT)
                        .strict(true),
                "project");

        assertThatThrownBy(executor::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("checkstyle found 1 finding, reported in ");
    }

    @Test
    public void the_strict_setting_fails_the_build_on_a_violation() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                CheckstyleModule.ofEnvironment(new Environment(Map.of("strict.checkstyle", "true")),
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");

        assertThatThrownBy(executor::execute)
                .rootCause()
                .hasMessageContaining("checkstyle found 1 finding")
                .hasMessageContaining("jenesis.strict.checkstyle is true");
    }

    @Test
    public void a_report_only_run_prints_the_number_of_findings_and_the_report() throws IOException {
        List<String> printed = new ArrayList<>();
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                CheckstyleModule.ofEnvironment(new Environment(Map.of()).out(printed::add),
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path report = root.resolve("checkstyle").resolve("check").resolve("output").resolve("reports").resolve("checkstyle").resolve("checkstyle-report.xml");
        assertThat(printed).anySatisfy(line -> assertThat(line)
                .contains("[FINDINGS]")
                .contains("checkstyle found 1 finding, reported in " + report));
    }

    @Test
    public void fails_a_report_only_run_when_checkstyle_cannot_load_its_configuration() throws IOException {
        Files.writeString(project.resolve("checkstyle.xml"), """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker">
                    <module name="NoSuchCheck"/>
                </module>
                """);
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");

        assertThatThrownBy(executor::execute)
                .rootCause()
                .as("report-only covers findings, not a Checkstyle that never ran")
                .hasMessageContaining("Unexpected exit code")
                .hasMessageContaining("NoSuchCheck");
    }

    @Test
    public void expands_config_loc_to_the_folder_of_the_configuration() throws IOException {
        Files.writeString(project.resolve("checkstyle.xml"), """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker">
                    <property name="severity" value="error"/>
                    <module name="SuppressionFilter">
                        <property name="file" value="${config_loc}/suppressions.xml"/>
                    </module>
                    <module name="TreeWalker">
                        <module name="TypeName"/>
                    </module>
                </module>
                """);
        Files.writeString(project.resolve("suppressions.xml"), """
                <?xml version="1.0"?>
                <!DOCTYPE suppressions PUBLIC
                    "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN"
                    "https://checkstyle.org/dtds/suppressions_1_2.dtd">
                <suppressions>
                    <suppress checks="TypeName" files="badName"/>
                </suppressions>
                """);
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT)
                        .strict(true),
                "project");
        executor.execute();

        Path report = root.resolve("checkstyle").resolve("check").resolve("output").resolve("reports").resolve("checkstyle").resolve("checkstyle-report.xml");
        assertThat(report).content().doesNotContain("<error");
    }

    @Test
    public void expands_the_properties_it_is_handed_in_the_configuration() throws IOException {
        Files.writeString(project.resolve("checkstyle.xml"), """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker">
                    <property name="severity" value="error"/>
                    <module name="TreeWalker">
                        <module name="TypeName">
                            <property name="format" value="${type.format}"/>
                        </module>
                    </module>
                </module>
                """);
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT)
                        .expansions(new LinkedHashMap<>(Map.of("type.format", "^[a-z][a-zA-Z0-9]*$")))
                        .strict(true),
                "project");
        executor.execute();

        Path report = root.resolve("checkstyle").resolve("check").resolve("output").resolve("reports").resolve("checkstyle").resolve("checkstyle-report.xml");
        assertThat(report).content()
                .as("the format the properties name accepts a type name starting in lower case")
                .doesNotContain("<error");
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
