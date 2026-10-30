package build.jenesis.plugin.antlr.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.antlr.AntlrFactory;
import build.jenesis.plugin.antlr.AntlrModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AntlrModuleRunTest {

    private static final String PINS = """
            antlr/maven/com.ibm.icu/icu4j 72.1 SHA-256/3df572b240a68d13b5cd778ad2393e885d26411434cd8f098ac5987ea2e64ce3
            antlr/maven/com.ibm.icu/icu4j/pom/72.1 72.1 SHA-256/3def2b29af4a19ad805cb153056925a89a903f92fbee1adee12ed2fc14c44ee8
            antlr/maven/org.abego.treelayout/org.abego.treelayout.core 1.0.3 SHA-256/fa5e31395c39c2e7d46aca0f81f72060931607b2fa41bd36038eb2cb6fb93326
            antlr/maven/org.abego.treelayout/org.abego.treelayout.core/pom/1.0.3 1.0.3 SHA-256/a3b2b223794370355e792433af012fc993667c0331be2bacad84dbc09ace4a0c
            antlr/maven/org.antlr/ST4 4.3.4 SHA-256/f927ac384c46d749f8b5ec68972a53aed21e00313509299616edb73bfa15ff33
            antlr/maven/org.antlr/ST4/pom/4.3.4 4.3.4 SHA-256/9e7c1f3e489919440e8c13089e5963729d5b7f80f70233bfbae309c649abca3e
            antlr/maven/org.antlr/antlr-master/pom/3.5.3 3.5.3 SHA-256/ea9e37250f5c4c2e76b6538be97b57f3349bda585edf53f3ca97e207b385b895
            antlr/maven/org.antlr/antlr-runtime 3.5.3 SHA-256/68bf9f5a33dfcb34033495c587e6236bef4e37aa6612919f5b1e843b90669fb9
            antlr/maven/org.antlr/antlr-runtime/pom/3.5.3 3.5.3 SHA-256/13298e0e0aafaf414ff7d4406427cab6ec4fbfa36427f6d7103c432f32c07d25
            antlr/maven/org.antlr/antlr4 4.13.2 SHA-256/e6f0b10d2ad206f338afe16867fc47148b6729d6e3a260ea28379b91f03a3657
            antlr/maven/org.antlr/antlr4-master/pom/4.13.2 4.13.2 SHA-256/0adda026685873f651360178bffc446ceee4833081739e3ae9d6e8f07e8d902a
            antlr/maven/org.antlr/antlr4-runtime 4.13.2 SHA-256/dd3e8a13a2d669bf84fb8d834de35ce4875f27157698d206241ec8488aadcaf7
            antlr/maven/org.antlr/antlr4-runtime/pom/4.13.2 4.13.2 SHA-256/03ce07a2796c511b0c94dc14fd86ccdd6e3828c57967ad2383de304e0d11ba79
            antlr/maven/org.antlr/antlr4/pom/4.13.2 4.13.2 SHA-256/809ee49706dce3674988babfcad36b3c5a0e2fd5cfa0a51209103963e857261b
            antlr/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            antlr/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            """;
    private static final String GRAMMAR = """
            grammar Greeting;

            greeting : HELLO NAME EOF ;

            HELLO : 'hello' ;
            NAME : [a-zA-Z]+ ;
            WHITESPACE : [ \\t\\r\\n]+ -> skip ;
            """;

    @TempDir
    private Path root, project;

    @BeforeEach
    public void writeProject() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        for (String key : List.copyOf(versions.stringPropertyNames())) {
            versions.setProperty("antlr:tool" + key.substring(key.indexOf('/')), versions.getProperty(key));
        }
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(
                Files.createDirectories(project.resolve(AntlrModule.FOLDER)).resolve("Greeting.g4"),
                GRAMMAR);
    }

    @Test
    public void generates_a_lexer_and_parser_into_the_configured_package() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("antlr", newModule().packageName("test.antlr"), "project");
        executor.execute();

        Path generated = generated().resolve("test").resolve("antlr");
        assertThat(generated.resolve("GreetingLexer.java")).isNotEmptyFile();
        assertThat(generated.resolve("GreetingParser.java")).isNotEmptyFile();
        assertThat(generated.resolve("GreetingParser.java"))
                .as("the configured package is declared by the generated sources")
                .content()
                .contains("package test.antlr;");
    }

    @Test
    public void keeps_only_the_sources_the_compiler_reads() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("antlr", newModule().packageName("test.antlr"), "project");
        executor.execute();

        Path generated = generated().resolve("test").resolve("antlr");
        assertThat(generated.resolve("Greeting.tokens"))
                .as("the token files ANTLR writes beside its sources do not reach the jar")
                .doesNotExist();
        assertThat(generated.resolve("GreetingLexer.interp")).doesNotExist();
    }

    @Test
    public void passes_arguments_to_the_tool() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("antlr",
                newModule().packageName("test.antlr").arguments(List.of("-visitor", "-no-listener")),
                "project");
        executor.execute();

        Path generated = generated().resolve("test").resolve("antlr");
        assertThat(generated.resolve("GreetingVisitor.java")).isNotEmptyFile();
        assertThat(generated.resolve("GreetingListener.java"))
                .as("-no-listener reached the tool")
                .doesNotExist();
    }

    @Test
    public void generates_with_the_package_and_arguments_it_is_handed_as_values() throws IOException {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("package", "test.antlr");
        values.put("arguments", "-visitor -no-listener");
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("antlr",
                new AntlrFactory().create(new Environment(Map.of("dependency.pin", "strict")),
                        values,
                        Permits.of(1),
                        "antlr",
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))),
                "project");
        executor.execute();

        Path generated = generated().resolve("test").resolve("antlr");
        assertThat(generated.resolve("GreetingVisitor.java")).isNotEmptyFile();
        assertThat(generated.resolve("GreetingListener.java")).doesNotExist();
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> new AntlrFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("visitor", "true")),
                Permits.of(1),
                "antlr",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no value visitor");
    }

    private Path generated() {
        return root.resolve("antlr").resolve(AntlrModule.GENERATE).resolve("output").resolve(BuildStep.SOURCES);
    }

    private AntlrModule newModule() {
        return new AntlrModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
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
