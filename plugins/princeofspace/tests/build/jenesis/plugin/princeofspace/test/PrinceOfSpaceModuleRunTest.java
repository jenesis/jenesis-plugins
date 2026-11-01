package build.jenesis.plugin.princeofspace.test;

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
import build.jenesis.plugin.princeofspace.PrinceOfSpaceModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PrinceOfSpaceModuleRunTest {

    private static final String PINS = """
            prince-of-space/maven/com.github.javaparser/javaparser-core 3.28.2 SHA-256/b5499a3b1c40b16c0671fabe478c9aafeab38160c6fde74a6c13f42d86716ecd
            prince-of-space/maven/com.github.javaparser/javaparser-core/pom/3.28.2 3.28.2 SHA-256/87d2b333dfec96896d5a49e80a95b92ef572f610e848276b45eaa061e05b4132
            prince-of-space/maven/com.github.javaparser/javaparser-parent/pom/3.28.2 3.28.2 SHA-256/0358ce75bca1a54c141e16b7f1621e973fa177ddf64fe02b0f38df23ad400543
            prince-of-space/maven/io.github.agustafson.princeofspace/prince-of-space-core 2.3.0 SHA-256/cd74d0ff8e8fa1553dcbefa9fe07709a417a7b2b371d2b13083778c41132066e
            prince-of-space/maven/io.github.agustafson.princeofspace/prince-of-space-core/pom/2.3.0 2.3.0 SHA-256/22bb5e09b8bdde300bc59f4e85904d9977593f452065c526ca0bcbc20758f409
            prince-of-space/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            prince-of-space/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            prince-of-space/maven/org.junit/junit-bom/pom/5.14.4 5.14.4 SHA-256/5706e8f29a0a07f56efbbea4a0670793414194bb8d24d8143ba1e787a2f32856
            prince-of-space/maven/org.slf4j/slf4j-api 2.0.20 SHA-256/7e1446499b359675d8aabe6af2de86b85e72ae8a2a932c0b40d0e5ba57097439
            prince-of-space/maven/org.slf4j/slf4j-api/pom/2.0.20 2.0.20 SHA-256/2e625c3f553cddd49f8554cea9926cbf70673b3d6833464f0c5a02f4d9130594
            prince-of-space/maven/org.slf4j/slf4j-bom/pom/2.0.20 2.0.20 SHA-256/351a3d34f0886942268efcf291708c48dfadce7f2643837efdb4a6954d4e6c62
            prince-of-space/maven/org.slf4j/slf4j-parent/pom/2.0.20 2.0.20 SHA-256/d931cf99cfd3554d322809df03c43ef897082cd480d6ff91b44d8f067ca324c8
            """;
    private static final String VERSION = "2.3.0";

    @TempDir
    private Path root, project;

    @Test
    public void formats_in_place_then_skips_an_unchanged_file_on_a_second_run() throws IOException {
        Path sample = sample("package sample;class Sample{int f(){return 42;}}\n");

        newExecutor(module -> module.verify(false)).execute();

        Path resolved = root.resolve("prince-of-space")
                .resolve("dependencies")
                .resolve("resolve")
                .resolve("output")
                .resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned prince-of-space version resolves")
                    .anyMatch(name -> name.contains("prince-of-space") && name.contains(VERSION));
        }
        String formatted = Files.readString(sample);
        assertThat(formatted)
                .as("the source is reformatted in place, indented by four spaces")
                .contains("class Sample {")
                .contains("\n    int f() {");
        Path command = root.resolve("prince-of-space").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();

        newExecutor(module -> module.verify(false)).execute();

        assertThat(Files.readString(sample)).as("the already-formatted file is left byte-identical").isEqualTo(formatted);
        assertThat(command).as("the formatter is not forked again when nothing changed").doesNotExist();
    }

    @Test
    public void indents_with_one_tab_a_level_as_told() throws IOException {
        Path sample = sample("package sample;class Sample{int f(){return 42;}}\n");

        newExecutor(module -> module.verify(false).indentStyle("tabs").indentSize(1)).execute();

        assertThat(Files.readString(sample))
                .as("each level is indented by one tab, the indent size counting tabs")
                .contains("\n\tint f() {\n\t\treturn 42;");
    }

    @Test
    public void fails_verification_of_a_file_it_would_change_and_leaves_it_as_it_was() throws IOException {
        String source = "package sample;class Sample{int f(){return 42;}}\n";
        Path sample = sample(source);

        assertThatThrownBy(() -> newExecutor(module -> module.verify(true)).execute())
                .as("verification fails on a file the formatter would change");
        assertThat(Files.readString(sample)).as("verification writes nothing").isEqualTo(source);
    }

    private Path sample(String source) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, source);
        return sample;
    }

    private BuildExecutor newExecutor(UnaryOperator<PrinceOfSpaceModule> configuration) throws IOException {
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
        executor.addModule("prince-of-space",
                configuration.apply(new PrinceOfSpaceModule(
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT)),
                "project");
        return executor;
    }
}
