package build.jenesis.plugin.xjc.test;

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
import build.jenesis.plugin.xjc.XjcModule;

import static org.assertj.core.api.Assertions.assertThat;

public class XjcModuleRunTest {

    private static final String PINS = """
            xjc/maven/com.sun.istack/istack-commons-runtime 4.1.2 SHA-256/7fd6792361f4dd00f8c56af4a20cecc0066deea4a8f3dec38348af23fc2296ee
            xjc/maven/com.sun.istack/istack-commons-runtime/pom/4.1.2 4.1.2 SHA-256/1222a290beddb6f6e92bc999bcebc3696fcd6d72215856d224727a7bc2dc3aca
            xjc/maven/com.sun.istack/istack-commons-tools 4.1.2 SHA-256/85b4fe7ad6fdfc64a586133f039d3de7b51db2c8111a1aa98a267891e27f386f
            xjc/maven/com.sun.istack/istack-commons-tools/pom/4.1.2 4.1.2 SHA-256/fb9633448c942248dc9b131f94a4404dfbdde87de1b45370cdfc9230ea0606ca
            xjc/maven/com.sun.istack/istack-commons/pom/4.1.2 4.1.2 SHA-256/d8883eb70364701dae0e37449c88fd9275117ac3d86040e89f1be3e9d47e9c9d
            xjc/maven/com.sun.xml.bind.external/relaxng-datatype 4.0.9 SHA-256/89933721c2dc654049ebea84a33e2f3d36e918e5871ccb6f08df1960779c5cb9
            xjc/maven/com.sun.xml.bind.external/relaxng-datatype/pom/4.0.9 4.0.9 SHA-256/9a2b14fbee37749a0cd5599cb7241ced75272b318fb983214313300c487866f3
            xjc/maven/com.sun.xml.bind.external/rngom 4.0.9 SHA-256/b3daa8902535f33ff90d66c2c327b95b3827f0b36a31f9a2c1094d7b82263e1b
            xjc/maven/com.sun.xml.bind.external/rngom/pom/4.0.9 4.0.9 SHA-256/9e7db1d17809836f0fb5c645a51e1dd0309ae5be89ea888d990aa9ac993b5118
            xjc/maven/com.sun.xml.dtd-parser/dtd-parser 1.5.1 SHA-256/fd07c1b528e8649e956bd4c759a63badb79e8076260b5c23bdf37f2b11d06711
            xjc/maven/com.sun.xml.dtd-parser/dtd-parser/pom/1.5.1 1.5.1 SHA-256/287e2942dc7c5919c5dc9dead826619ffe6cb507a1504aae36a9863fbee8c68b
            xjc/maven/jakarta.activation/jakarta.activation-api 2.1.4 SHA-256/c9db52100ce6c8aac95cc39075f95720d2e561b11f8051b81c121ad4effd7004
            xjc/maven/jakarta.activation/jakarta.activation-api/pom/2.1.4 2.1.4 SHA-256/7577970dc09f1131a4a42769e1771ed062f08d22f40da3041ff3e16d5b2fdea8
            xjc/maven/jakarta.xml.bind/jakarta.xml.bind-api 4.0.5 SHA-256/5e489b6c874c4119e003ff1403db523ee3a8959ec499f3de29e77245efccf216
            xjc/maven/jakarta.xml.bind/jakarta.xml.bind-api-parent/pom/4.0.5 4.0.5 SHA-256/68e78c21a934cd37ab48c07a95aa7f8cbb98cafcfa2bb9ef92d54399accdbf15
            xjc/maven/jakarta.xml.bind/jakarta.xml.bind-api/pom/4.0.5 4.0.5 SHA-256/596f65494651b79eb589b2f579a5686fabcd5cbb68f2c928c1cf360341d68c16
            xjc/maven/org.eclipse.angus/angus-activation 2.0.3 SHA-256/a6bd35c538cf90fff941ad6258c40c08fca0b5c9c3f536c657114f27ce0527a7
            xjc/maven/org.eclipse.angus/angus-activation-project/pom/2.0.3 2.0.3 SHA-256/d34967386837899646292a451a3ecf1e4efd1262c291b37d15e10d3d90ec2bcd
            xjc/maven/org.eclipse.angus/angus-activation/pom/2.0.3 2.0.3 SHA-256/35c9e16209b2721261354e8aea971626648ad9d4201d221bd96901d900372da0
            xjc/maven/org.eclipse.ee4j/project/pom/1.0.7 1.0.7 SHA-256/205c039a42cbae3556efbeb04a483eb3a3cf9550bd75bf84260dc8f28218f105
            xjc/maven/org.eclipse.ee4j/project/pom/1.0.8 1.0.8 SHA-256/0d0c7b6e54a35eaf6c246e107eb1a8c7ac8ff0a0b84c489b07a357713adf674b
            xjc/maven/org.eclipse.ee4j/project/pom/1.0.9 1.0.9 SHA-256/825379934a1cf29249f349f2d18adaf79a7b2cb2dbe231518974e1ee70941c17
            xjc/maven/org.eclipse.ee4j/project/pom/2.0.2 2.0.2 SHA-256/bb75d1a7d6fed055951846e5982225d772d6dec0f1bd8ae5da7948bb0b7eb6b9
            xjc/maven/org.eclipse.ee4j/project/pom/2.0.4 2.0.4 SHA-256/5cefee62eab949f2d311aa4dd5ba004646260c5de3dfd7c8b31e26220d3574f9
            xjc/maven/org.glassfish.jaxb/codemodel 4.0.9 SHA-256/12c9ba587b85af187a71b25c531c9929bd1a53da334d102e485468a493a1f1d7
            xjc/maven/org.glassfish.jaxb/codemodel/pom/4.0.9 4.0.9 SHA-256/aa8f271bb828a0cdf29386b614c8724f1f8214aea01e521cf7dec528097780e1
            xjc/maven/org.glassfish.jaxb/jaxb-bom-ext/pom/4.0.9 4.0.9 SHA-256/f1b97cda5ce02447a2a5ae548ca0562c09a71b91083c02a260dbf236ac663490
            xjc/maven/org.glassfish.jaxb/jaxb-bom/pom/4.0.9 4.0.9 SHA-256/a4f7de9940a5b8faed3491d36dbe70cee67452fe7a864f3501e6541110f37d1e
            xjc/maven/org.glassfish.jaxb/jaxb-codemodel-parent/pom/4.0.9 4.0.9 SHA-256/50e178c3d08b29dcb2d27f3fa6a8bd109bee96eb2c154eeff78256b1e2ff694a
            xjc/maven/org.glassfish.jaxb/jaxb-core 4.0.9 SHA-256/6b014df25d22c8446cc2df0a378c5b2137747f197a731d99eb24c756b26f7e8d
            xjc/maven/org.glassfish.jaxb/jaxb-core/pom/4.0.9 4.0.9 SHA-256/f900cce43c065ae8f801323c1ad85bfda56fc2a8ea6808c7054b106920b992c4
            xjc/maven/org.glassfish.jaxb/jaxb-external-parent/pom/4.0.9 4.0.9 SHA-256/e8147cf333123b2cc8330e4f6c024110b0e33330b23801b6e480b0029f4768c1
            xjc/maven/org.glassfish.jaxb/jaxb-parent/pom/4.0.9 4.0.9 SHA-256/fa633fabcd4c925e54f92edebda13b048d634124940a764dfbb08e7312481a6f
            xjc/maven/org.glassfish.jaxb/jaxb-txw-parent/pom/4.0.9 4.0.9 SHA-256/ae6ddd1c12d7708857ecfc184d125fadc762b2bca9e67e6255169dc93517cfa0
            xjc/maven/org.glassfish.jaxb/jaxb-xjc 4.0.9 SHA-256/5556b3d06a4af1bbcd9d68ef80dbaa04bdc686c246c89b3d98db4acf7f8faf90
            xjc/maven/org.glassfish.jaxb/jaxb-xjc/pom/4.0.9 4.0.9 SHA-256/fb6dff8034fa4f16fb6f73fc55ce96d83634e8791f6545010eeb8d14c89bc996
            xjc/maven/org.glassfish.jaxb/txw2 4.0.9 SHA-256/d6343e5945d87266ca4e8974a0a2a876e1bd147a00fb65ed3f2e1a3a00f3a82a
            xjc/maven/org.glassfish.jaxb/txw2/pom/4.0.9 4.0.9 SHA-256/b0f46b869ef5acf9577506ee031f22b00a1f37849c2c71b8a02248f12be7e456
            xjc/maven/org.glassfish.jaxb/xsom 4.0.9 SHA-256/be584192033acb7424e0edb283e18f83ae681a46c9e3218fb4377f4df870ed07
            xjc/maven/org.glassfish.jaxb/xsom/pom/4.0.9 4.0.9 SHA-256/1034a55bb32b788535bd7de9be75377685b896855141cd52cfa3c0c193407960
            """;
    private static final String SCHEMA = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:tns="https://jenesis.build/test/order"
                       targetNamespace="https://jenesis.build/test/order"
                       elementFormDefault="qualified">
                <xs:element name="order" type="tns:Order"/>
                <xs:complexType name="Order">
                    <xs:attribute name="id" type="xs:string" use="required"/>
                </xs:complexType>
            </xs:schema>
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
    public void generates_java_sources_from_a_schema_among_the_inputs() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(XjcModule.FOLDER)).resolve("order.xsd"),
                SCHEMA);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("xjc", newModule().packageName("test.order"), "project");
        executor.execute();

        Path generated = root.resolve("xjc").resolve("generate").resolve("output").resolve("sources").resolve("test").resolve("order");
        assertThat(generated.resolve("Order.java"))
                .as("the schema's complex type reaches javac as a generated source file")
                .isNotEmptyFile();
        assertThat(generated.resolve("Order.java")).content().contains("package test.order;");
    }

    @Test
    public void generates_the_same_bytes_on_every_run() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(XjcModule.FOLDER)).resolve("order.xsd"),
                SCHEMA);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("xjc", newModule().packageName("test.order"), "project");
        executor.execute();

        Path generated = root.resolve("xjc").resolve("generate").resolve("output")
                .resolve("sources").resolve("test").resolve("order").resolve("Order.java");
        assertThat(generated).content()
                .as("xjc stamps the wall clock into a file header unless told not to, "
                        + "which would give the step a different output on every run")
                .doesNotContain("Generated on:");
    }

    @Test
    public void ignores_a_schema_outside_the_folder_the_step_reads() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(BuildStep.SOURCES + "schema")).resolve("order.xsd"),
                SCHEMA);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("xjc", newModule().packageName("test.order"), "project");
        executor.execute();

        assertThat(root.resolve("xjc").resolve("generate").resolve("output").resolve("sources"))
                .as("the step reads its own folder and never searches the module it belongs to")
                .doesNotExist();
    }

    @Test
    public void compiles_a_schema_below_the_folder_the_step_reads() throws IOException {
        Files.writeString(
                Files.createDirectories(project.resolve(XjcModule.FOLDER + "order")).resolve("order.xsd"),
                SCHEMA);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("xjc", newModule().packageName("test.order"), "project");
        executor.execute();

        assertThat(root.resolve("xjc").resolve("generate").resolve("output")
                .resolve("sources").resolve("test").resolve("order").resolve("Order.java"))
                .as("a schema keeps the path it holds within the folder, so imports resolve as they are written")
                .isNotEmptyFile();
    }

    @Test
    public void generates_nothing_when_no_schema_is_among_the_inputs() throws IOException {
        Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("xjc", newModule(), "project");
        executor.execute();

        assertThat(root.resolve("xjc").resolve("generate").resolve("output").resolve("sources"))
                .as("a shared xjc.properties activates the module for every project module, "
                        + "so one without a schema contributes no sources rather than failing")
                .doesNotExist();
    }

    private XjcModule newModule() {
        return new XjcModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
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
