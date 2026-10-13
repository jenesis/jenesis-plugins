package build.jenesis.plugin.groovy.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.groovy.GroovyDocumentationModule;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyDocumentationModuleTest {

    private static final List<String> CAPTURED = new CopyOnWriteArrayList<>();

    @TempDir
    private Path root, project;

    @BeforeEach
    public void clear() {
        CAPTURED.clear();
    }

    @Test
    public void honors_release_from_upstream_javac_properties() throws IOException {
        sources();
        release("11");

        document(UnaryOperator.identity());

        assertThat(CAPTURED)
                .as("--release=11 from process/javac.properties drives -javaVersion JAVA_11")
                .containsSequence("-javaVersion", "JAVA_11");
    }

    @Test
    public void documents_again_when_the_upstream_release_changes() throws IOException {
        sources();
        for (String release : List.of("11", "17")) {
            release(release);
            CAPTURED.clear();
            document(UnaryOperator.identity());
        }

        assertThat(CAPTURED)
                .as("a changed --release documents the sources again for the release they now target")
                .containsSequence("-javaVersion", "JAVA_17");
    }

    @Test
    public void bounds_the_language_level_to_what_groovydoc_reads() throws IOException {
        sources();
        release("1.6");

        document(UnaryOperator.identity());

        assertThat(CAPTURED).containsSequence("-javaVersion", "JAVA_8");
    }

    @Test
    public void documents_the_groovy_packages_without_a_timestamp() throws IOException {
        sources();

        document(UnaryOperator.identity());

        assertThat(CAPTURED)
                .contains("org.codehaus.groovy.tools.groovydoc.Main", "-notimestamp", "sample")
                .doesNotContain("other");
        assertThat(root.resolve("groovydoc").resolve(GroovyDocumentationModule.DOCUMENTED).resolve("output")
                .resolve("javadoc"))
                .as("the documentation is written at the root of javadoc/, where the build places it")
                .isDirectory();
    }

    @Test
    public void documents_the_java_packages_as_well_when_asked_to() throws IOException {
        sources();

        document(module -> module.includeJava(true).timestamped(true));

        assertThat(CAPTURED)
                .contains("sample", "other")
                .doesNotContain("-notimestamp");
    }

    @Test
    public void documents_nothing_without_a_groovy_source() throws IOException {
        Path other = Files.createDirectories(project.resolve(BuildStep.SOURCES + "other"));
        Files.writeString(other.resolve("Other.java"), "package other; public class Other {}");

        document(module -> module.includeJava(true));

        assertThat(CAPTURED).isEmpty();
    }

    private void sources() throws IOException {
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("Sample.groovy"), "package sample\nclass Sample {}");
        Path other = Files.createDirectories(project.resolve(BuildStep.SOURCES + "other"));
        Files.writeString(other.resolve("Other.java"), "package other; public class Other {}");
    }

    private void release(String release) throws IOException {
        SequencedProperties javac = new SequencedProperties();
        javac.setProperty("--release", release);
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "javac", javac);
    }

    private void document(UnaryOperator<GroovyDocumentationModule> configuration) throws IOException {
        Path jar = project.resolve("groovydoc.jar");
        if (!Files.exists(jar)) {
            Files.createFile(jar);
        }
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovydoc",
                configuration.apply(new GroovyDocumentationModule(
                        Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(jar))),
                        Map.of("maven", Resolver.identity()))
                        .factory(commands -> {
                            CAPTURED.addAll(commands);
                            return ProcessHandler.OfTool.of(new Silent()).apply(commands);
                        })),
                "project");
        executor.execute();
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

    private record Silent() implements ToolProvider {

        @Override
        public String name() {
            return "groovydoc";
        }

        @Override
        public int run(PrintWriter out, PrintWriter err, String... args) {
            return 0;
        }
    }
}
