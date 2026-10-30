package build.jenesis.plugin.wsimport.test;

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
import build.jenesis.plugin.wsimport.WsImportModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WsImportModuleRunTest {

    private static final String PINS = """
            wsimport/maven/com.fasterxml.woodstox/woodstox-core 7.2.0 SHA-256/689f5be95f9bb369d6c0e8db99a99f930b0c48d6a69b46151782b5816c7ea820
            wsimport/maven/com.fasterxml.woodstox/woodstox-core/pom/7.2.0 7.2.0 SHA-256/24a55f1439060131dfd62943245bfdf79c536ae4372fef5f9446b8a0121a3945
            wsimport/maven/com.fasterxml/oss-parent/pom/68 68 SHA-256/25eafd96dae242b6b5a7108f55b2c14015b828daa5abe234289fd6e774a1ce57
            wsimport/maven/com.fasterxml/oss-parent/pom/79 79 SHA-256/9206b7dd67fb138036576c3dfca947a2a8f3e2c4d2d9c1f2d1dea519b3a4dae1
            wsimport/maven/com.sun.xml.bind/jaxb-core 4.0.9 SHA-256/209eb94452d89644380e398e4ac126974718eeb3aa94ed1786654837ebcd3097
            wsimport/maven/com.sun.xml.bind/jaxb-core/pom/4.0.9 4.0.9 SHA-256/520b0778102fd2a95b6fcb0b69d5fd0dad72442c4aee23f89198aad01ad15ca5
            wsimport/maven/com.sun.xml.bind/jaxb-impl 4.0.9 SHA-256/6ec4361ade0b7e0267ee4d1ef495928bf722570af25904e1618c9949b67fdd86
            wsimport/maven/com.sun.xml.bind/jaxb-impl/pom/4.0.9 4.0.9 SHA-256/b7f6c41e0e1f396074fc925ce5f1318dc0e3ecbb8cb57ef6bbaf9db0e954f189
            wsimport/maven/com.sun.xml.bind/jaxb-jxc 4.0.9 SHA-256/734d18a38b9941d263aa65a1fb56c516b62a5138b671396e4ca7c62696106953
            wsimport/maven/com.sun.xml.bind/jaxb-jxc/pom/4.0.9 4.0.9 SHA-256/5d7abb97a9be402969edee49b4b8d83c9eca30d59544e7e4014157bfe4d47aaf
            wsimport/maven/com.sun.xml.bind/jaxb-xjc 4.0.9 SHA-256/eac40b7db01233395d69a996f183f8c9f697dfb5038115ba2d443a9f355378ab
            wsimport/maven/com.sun.xml.bind/jaxb-xjc/pom/4.0.9 4.0.9 SHA-256/0d7e3106aebc529c74727218e91c56ef34ab270149582451d1d9af5852bbe183
            wsimport/maven/com.sun.xml.fastinfoset/FastInfoset 2.1.1 SHA-256/75d6635e09101ef1545d9a59bb4d9524ec6bf15246540af5435750f271612765
            wsimport/maven/com.sun.xml.fastinfoset/FastInfoset/pom/2.1.1 2.1.1 SHA-256/49fdcb0e1b738752e7da4ba58e7a5e9e486c908f19b8535355069daefab73a7e
            wsimport/maven/com.sun.xml.fastinfoset/fastinfoset-project/pom/2.1.1 2.1.1 SHA-256/1e80efb1a0915b0f8626ba75d7296b5f1bb0269a0a2f9d7387ad9987f76e4c09
            wsimport/maven/com.sun.xml.messaging.saaj/metro-saaj/pom/3.0.6 3.0.6 SHA-256/d82e6b9a3e990ef8606a45b9c29041672e071d2401bda92fda2d19a1e608db20
            wsimport/maven/com.sun.xml.messaging.saaj/saaj-impl 3.0.6 SHA-256/70d657b7a6bf525b98cf6780ccf0c6fc27c623e7c195574258e882c4e76c48ae
            wsimport/maven/com.sun.xml.messaging.saaj/saaj-impl/pom/3.0.6 3.0.6 SHA-256/b430221db0b6958714ef50d5d09e16925afd7d5685d96af587ea3c637cddf769
            wsimport/maven/com.sun.xml.stream.buffer/streambuffer 2.1.0 SHA-256/4fc371f13aa58b77223600cfb4651a3f98e7809ede1af70a3f00df49e2a4d028
            wsimport/maven/com.sun.xml.stream.buffer/streambuffer/pom/2.1.0 2.1.0 SHA-256/7ac22ee1c78b0c27dd7903aa208bf64c0859bea19967daffc6628b50cdface60
            wsimport/maven/com.sun.xml.ws/bundles/pom/4.0.5 4.0.5 SHA-256/40f57c994bb1396037cab2a7a54a8514ad05aac85fb2662f4787f7c5839e2fb8
            wsimport/maven/com.sun.xml.ws/jaxws-ri-bom-ext/pom/4.0.5 4.0.5 SHA-256/a73fc56ffef7896b2177a2138a9c4c45a093f98a9f87dfb822525d58ea5aa1da
            wsimport/maven/com.sun.xml.ws/jaxws-ri-bom/pom/4.0.5 4.0.5 SHA-256/78071be1d6e918f4a930ea121d537cf31372ea1680560a3f4a6218673a96ab4f
            wsimport/maven/com.sun.xml.ws/jaxws-rt 4.0.5 SHA-256/d46f7562dca03f342093630a07c9822abe752d56867766db4e9a58a21521653d
            wsimport/maven/com.sun.xml.ws/jaxws-rt/pom/4.0.5 4.0.5 SHA-256/b4f1b7045989c32796e82964545be25b6d6437fdbcc27160af2a3336b7b8e404
            wsimport/maven/com.sun.xml.ws/jaxws-tools 4.0.5 SHA-256/7eb07d9fdb654264579173d6a98898f550f083cc80abd0043252e46f122f9ecd
            wsimport/maven/com.sun.xml.ws/jaxws-tools/pom/4.0.5 4.0.5 SHA-256/0407df5ff37bec4915e4fe6f6feb8f186cbdb76e85138fe7f32a4be18e1e279a
            wsimport/maven/com.sun.xml.ws/project/pom/4.0.5 4.0.5 SHA-256/ab19cae842273453623c2dd5d04f5b2210453af1cf8adcfb5d8db072765230ad
            wsimport/maven/jakarta.activation/jakarta.activation-api 2.1.4 SHA-256/c9db52100ce6c8aac95cc39075f95720d2e561b11f8051b81c121ad4effd7004
            wsimport/maven/jakarta.activation/jakarta.activation-api/pom/2.1.4 2.1.4 SHA-256/7577970dc09f1131a4a42769e1771ed062f08d22f40da3041ff3e16d5b2fdea8
            wsimport/maven/jakarta.annotation/jakarta.annotation-api 3.0.0 SHA-256/b01f55552284cfb149411e64eabca75e942d26d2e1786b32914250e4330afaa2
            wsimport/maven/jakarta.annotation/jakarta.annotation-api/pom/3.0.0 3.0.0 SHA-256/9fc66a87375df8443aba6bdcc1d4ff07f126542583785a482a9262a19bfe8eae
            wsimport/maven/jakarta.mail/jakarta.mail-api 2.1.5 SHA-256/aa493753acb7a8c45ba8f4c9cf1230a74e20237056dd5b5c8bc86c583e8cfa0e
            wsimport/maven/jakarta.mail/jakarta.mail-api/pom/2.1.5 2.1.5 SHA-256/f03bcb59bb77e1a0099c89063d9d09a6bb4d50d56997a4d5ee1bf58e3864ef43
            wsimport/maven/jakarta.xml.bind/jakarta.xml.bind-api 4.0.5 SHA-256/5e489b6c874c4119e003ff1403db523ee3a8959ec499f3de29e77245efccf216
            wsimport/maven/jakarta.xml.bind/jakarta.xml.bind-api-parent/pom/4.0.5 4.0.5 SHA-256/68e78c21a934cd37ab48c07a95aa7f8cbb98cafcfa2bb9ef92d54399accdbf15
            wsimport/maven/jakarta.xml.bind/jakarta.xml.bind-api/pom/4.0.5 4.0.5 SHA-256/596f65494651b79eb589b2f579a5686fabcd5cbb68f2c928c1cf360341d68c16
            wsimport/maven/jakarta.xml.soap/jakarta.xml.soap-api 3.0.2 SHA-256/62ecd5c3b5c107779e5ffe84922594c381f7a8e397320a05c3ee3957b5b7863f
            wsimport/maven/jakarta.xml.soap/jakarta.xml.soap-api/pom/3.0.2 3.0.2 SHA-256/28a46919604b9d0e84a057463579f3e81779c866da2e6feedff51f76c9785f09
            wsimport/maven/jakarta.xml.ws/jakarta.xml.ws-api 4.0.3 SHA-256/e2244d1046363f21a1dbb9485d592f6697b219b8086de70de759c7c5d725517f
            wsimport/maven/jakarta.xml.ws/jakarta.xml.ws-api/pom/4.0.3 4.0.3 SHA-256/40578a6b6317792e5665c3b135686279511b4bdb1e7759a4f5e9ccc90a2077bd
            wsimport/maven/org.codehaus.woodstox/stax2-api 4.3.0 SHA-256/7c805f36129ea9fa42b696093b7ae1eb20bb6ccec65c8280d6f33db5609ca5e1
            wsimport/maven/org.codehaus.woodstox/stax2-api/pom/4.3.0 4.3.0 SHA-256/8588b8871817e28e74f647e41511d00bdf3f93304463f746b25659f283daf229
            wsimport/maven/org.eclipse.angus/all/pom/2.0.5 2.0.5 SHA-256/624dd24f7a14f080dd0846cae6620da116fe254bfeb2704ad2e0f195da8d6da8
            wsimport/maven/org.eclipse.angus/angus-activation 2.0.3 SHA-256/a6bd35c538cf90fff941ad6258c40c08fca0b5c9c3f536c657114f27ce0527a7
            wsimport/maven/org.eclipse.angus/angus-activation-project/pom/2.0.3 2.0.3 SHA-256/d34967386837899646292a451a3ecf1e4efd1262c291b37d15e10d3d90ec2bcd
            wsimport/maven/org.eclipse.angus/angus-activation/pom/2.0.3 2.0.3 SHA-256/35c9e16209b2721261354e8aea971626648ad9d4201d221bd96901d900372da0
            wsimport/maven/org.eclipse.angus/angus-mail 2.0.5 SHA-256/b4d8c30d35f455def6c7a05fe595a1e62ea2b80cac3efec1e9ccf4118b23168a
            wsimport/maven/org.eclipse.angus/angus-mail/pom/2.0.5 2.0.5 SHA-256/b2fb76a9ea807b355b8ffa634de84adefac2f279cf60d58cb59c6a6c114c55af
            wsimport/maven/org.eclipse.ee4j/project/pom/1.0.6 1.0.6 SHA-256/4e7d8329d8da7dcf30779d824241be145f27108932f5a5a24eb907677bc8d72d
            wsimport/maven/org.eclipse.ee4j/project/pom/1.0.7 1.0.7 SHA-256/205c039a42cbae3556efbeb04a483eb3a3cf9550bd75bf84260dc8f28218f105
            wsimport/maven/org.eclipse.ee4j/project/pom/1.0.9 1.0.9 SHA-256/825379934a1cf29249f349f2d18adaf79a7b2cb2dbe231518974e1ee70941c17
            wsimport/maven/org.eclipse.ee4j/project/pom/2.0.2 2.0.2 SHA-256/bb75d1a7d6fed055951846e5982225d772d6dec0f1bd8ae5da7948bb0b7eb6b9
            wsimport/maven/org.eclipse.ee4j/project/pom/2.0.4 2.0.4 SHA-256/5cefee62eab949f2d311aa4dd5ba004646260c5de3dfd7c8b31e26220d3574f9
            wsimport/maven/org.glassfish.external/management-api 3.3.0 SHA-256/317e5b0ca2e90542f4462eb87e47dc3c23d5b2fa3d992b0d68e6aa14658efda7
            wsimport/maven/org.glassfish.external/management-api/pom/3.3.0 3.3.0 SHA-256/64fc76bb0556d573fdd8f79ec0fe4dfed90bae368d3a27b0faf99c89deb2c84e
            wsimport/maven/org.glassfish.gmbal/gmbal-api-only 4.1.2 SHA-256/9a0cbc5116745141b713b863fcf367740864301050db81fa9dc37ee828ac4f87
            wsimport/maven/org.glassfish.gmbal/gmbal-api-only/pom/4.1.2 4.1.2 SHA-256/2406a23b0029daed32ab1ad6ec0ef56b614d59b6ac7fc751d394877e69295031
            wsimport/maven/org.glassfish.gmbal/gmbal-project/pom/4.1.2 4.1.2 SHA-256/8e6fd3dd09e87ce61e558bc42d4c0118aa4c58c867f920e7dec0cc5304b3c3a0
            wsimport/maven/org.glassfish.ha/ha-api 3.1.13 SHA-256/49770bf120f76d78ead363e00fa09d02a76d5f12a88e654009066aec67f8d8ad
            wsimport/maven/org.glassfish.ha/ha-api/pom/3.1.13 3.1.13 SHA-256/f839018b3b259f75ccaa566721cc3d6c8c112e19e55c4695c4b2a34394db5498
            wsimport/maven/org.glassfish.jaxb/jaxb-bom-ext/pom/4.0.9 4.0.9 SHA-256/f1b97cda5ce02447a2a5ae548ca0562c09a71b91083c02a260dbf236ac663490
            wsimport/maven/org.glassfish.jaxb/jaxb-bom/pom/4.0.9 4.0.9 SHA-256/a4f7de9940a5b8faed3491d36dbe70cee67452fe7a864f3501e6541110f37d1e
            wsimport/maven/org.glassfish.jaxb/jaxb-bundles/pom/4.0.9 4.0.9 SHA-256/cf3d1c21362e685b8e74eae50c61859cc8c9e3d8ce72a7c076fc4ff3b7a375b6
            wsimport/maven/org.glassfish.jaxb/jaxb-parent/pom/4.0.9 4.0.9 SHA-256/fa633fabcd4c925e54f92edebda13b048d634124940a764dfbb08e7312481a6f
            wsimport/maven/org.glassfish.pfl/pfl-basic 5.1.1 SHA-256/d6162cbd121913412622042b0f76c9cab61d8348fe0f45b41008d5978bb774e4
            wsimport/maven/org.glassfish.pfl/pfl-basic/pom/5.1.1 5.1.1 SHA-256/74ae40335112a308fc5bf480c218a1aa022cfb0f3ace43dfb602c1728e4c3400
            wsimport/maven/org.glassfish.pfl/pfl-dynamic 5.1.1 SHA-256/411360c3fbea6034289a4dbe8622131e447a53a2edf995e348601e1d2543ea6c
            wsimport/maven/org.glassfish.pfl/pfl-dynamic/pom/5.1.1 5.1.1 SHA-256/b8cf8ae50819b6ed8b60c1c06106c16d6f241951ef6fad7994e80db6beff60fa
            wsimport/maven/org.glassfish.pfl/pfl-tf 5.1.1 SHA-256/7ebe4d6a191b1191eb8502c17634071fb58d2ce06e0e10bfd44dbd2eb0b9ded8
            wsimport/maven/org.glassfish.pfl/pfl-tf/pom/5.1.1 5.1.1 SHA-256/e0d0a8d109b842f0ae6750454598373deb36870ec811c114c1c63552b7abe996
            wsimport/maven/org.glassfish.pfl/pfl/pom/5.1.1 5.1.1 SHA-256/6a1a4d9779fe7e4b0bd89b1ae74dc17b087b7a9d738f7d3f4d87fecc38138479
            wsimport/maven/org.jvnet.mimepull/mimepull 1.11.0 SHA-256/af26c386c4baf48dab68f38b132d5990bd8f8324424c8579edd3ffce250d329e
            wsimport/maven/org.jvnet.mimepull/mimepull/pom/1.11.0 1.11.0 SHA-256/c435b01ba2145684165e09cb7fc41b2e5d6004cbe94f93a2e3e60253b06459ae
            wsimport/maven/org.jvnet.staxex/stax-ex 2.1.0 SHA-256/9f786ab52392106a53491bd1ddd8bd9028c95bb280e30387b70d498a8647cf35
            wsimport/maven/org.jvnet.staxex/stax-ex/pom/2.1.0 2.1.0 SHA-256/514b53339b91b506f67f2ce0e8497ab45da34e1c9e4bf4bdea214d265ca40324
            wsimport/maven/org.ow2.asm/asm 9.9.1 SHA-256/6f3828a215c920059a5efa2fb55c233d6c54ec5cadca99ce1b1bdd10077c7ddd
            wsimport/maven/org.ow2.asm/asm-analysis 9.9.1 SHA-256/6260bffc8ec008dd1b713702c7994e2c94d188a3da5bef9e87278a16df6a7522
            wsimport/maven/org.ow2.asm/asm-analysis/pom/9.9.1 9.9.1 SHA-256/36217e82d8a60134cf210f80eb76ddae595591c6085cab816b5aa10ce99fcba0
            wsimport/maven/org.ow2.asm/asm-tree 9.9.1 SHA-256/0f3555096b720b820bbacab0b515589bee0200bee099bda14c561738ae837ba1
            wsimport/maven/org.ow2.asm/asm-tree/pom/9.9.1 9.9.1 SHA-256/67fee446ba512871300738f0f956f61a500a852f6861ff70f0a3c7bf0c0fa405
            wsimport/maven/org.ow2.asm/asm-util 9.9.1 SHA-256/c5ebbbeaf68126af094b42fa4800f59bc4413abd02d95b9aefad722cd257e207
            wsimport/maven/org.ow2.asm/asm-util/pom/9.9.1 9.9.1 SHA-256/527969d08a4d75f9bdaf9cbf3837e98696a29890bc017d95fca6ff810f0d704c
            wsimport/maven/org.ow2.asm/asm/pom/9.9.1 9.9.1 SHA-256/aca68dee9ba2f6cd90ffde728efdc7e3ebfcf59f3f41fbfe248d2d01d5b866af
            wsimport/maven/org.ow2/ow2/pom/1.5.1 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
            """;
    private static final String DESCRIPTION = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions name="Greeter"
                         targetNamespace="https://jenesis.build/test/greeter"
                         xmlns:tns="https://jenesis.build/test/greeter"
                         xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                         xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                         xmlns="http://schemas.xmlsoap.org/wsdl/">
                <types>
                    <xsd:schema targetNamespace="https://jenesis.build/test/greeter">
                        <xsd:element name="greet" type="xsd:string"/>
                        <xsd:element name="greetResponse" type="xsd:string"/>
                    </xsd:schema>
                </types>
                <message name="greetRequest"><part name="parameters" element="tns:greet"/></message>
                <message name="greetResponse"><part name="parameters" element="tns:greetResponse"/></message>
                <portType name="GreeterPort">
                    <operation name="greet">
                        <input message="tns:greetRequest"/>
                        <output message="tns:greetResponse"/>
                    </operation>
                </portType>
                <binding name="GreeterBinding" type="tns:GreeterPort">
                    <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
                    <operation name="greet">
                        <soap:operation soapAction="greet"/>
                        <input><soap:body use="literal"/></input>
                        <output><soap:body use="literal"/></output>
                    </operation>
                </binding>
                <service name="GreeterService">
                    <port name="GreeterPort" binding="tns:GreeterBinding">
                        <soap:address location="https://example.invalid/greeter"/>
                    </port>
                </service>
            </definitions>
            """;

    @TempDir
    private Path root, project;

    @BeforeEach
    public void writeProject() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
    }

    @Test
    public void generates_a_service_client_from_a_description_among_the_inputs() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(WsImportModule.FOLDER)).resolve("greeter.wsdl"),
                DESCRIPTION);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("wsimport",
                newModule().packageName("test.greeter").location("https://example.invalid/greeter?wsdl"),
                "project");
        executor.execute();

        Path generated = root.resolve("wsimport").resolve("generate").resolve("output")
                .resolve("sources").resolve("test").resolve("greeter");
        assertThat(generated.resolve("GreeterService.java"))
                .as("the description's service becomes a generated client class")
                .isNotEmptyFile();
        assertThat(generated.resolve("GreeterPort.java")).content().contains("package test.greeter;");
    }

    @Test
    public void states_a_configured_location_verbatim() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(WsImportModule.FOLDER)).resolve("greeter.wsdl"),
                DESCRIPTION);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("wsimport",
                newModule().packageName("test.greeter").location("https://example.invalid/greeter?wsdl"),
                "project");
        executor.execute();

        Path generated = root.resolve("wsimport").resolve("generate").resolve("output")
                .resolve("sources").resolve("test").resolve("greeter").resolve("GreeterService.java");
        assertThat(generated).content().contains("https://example.invalid/greeter?wsdl");
    }

    @Test
    public void refuses_to_generate_a_client_that_cannot_state_where_it_reads_its_description() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(WsImportModule.FOLDER)).resolve("greeter.wsdl"),
                DESCRIPTION);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("wsimport", newModule().packageName("test.greeter"), "project");

        assertThatThrownBy(executor::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("nothing states where it is served");
    }

    @Test
    public void generates_nothing_when_no_description_is_among_the_inputs() throws IOException {
        Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("wsimport", newModule(), "project");
        executor.execute();

        assertThat(root.resolve("wsimport").resolve("generate").resolve("output").resolve("sources"))
                .as("a shared wsimport.properties activates the module for every project module, "
                        + "so one without a description contributes no sources rather than failing")
                .doesNotExist();
    }

    private WsImportModule newModule() {
        return new WsImportModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                .pinning(Pinning.STRICT);
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
