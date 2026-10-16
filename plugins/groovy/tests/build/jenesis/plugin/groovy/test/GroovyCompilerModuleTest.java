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
import build.jenesis.plugin.groovy.GroovyCompilerModule;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GroovyCompilerModuleTest {

    private static final List<String> CAPTURED = new CopyOnWriteArrayList<>();

    @TempDir
    private Path root, project, compiled;

    @BeforeEach
    public void clear() {
        CAPTURED.clear();
    }

    @Test
    public void requires_step_picks_module_coordinate_when_module_resolver_available() throws IOException {
        assertThat(required(Map.of("module", Resolver.identity())))
                .containsExactly("groovy/runtime/module/org.apache.groovy");
    }

    @Test
    public void requires_step_picks_maven_coordinate_when_only_maven_resolver_available() throws IOException {
        assertThat(required(Map.of("maven", Resolver.identity())))
                .containsExactly("groovy/runtime/maven/org.apache.groovy/groovy/RELEASE");
    }

    @Test
    public void requires_step_prefers_maven_when_both_resolvers_are_available() throws IOException {
        assertThat(required(Map.of("module", Resolver.identity(), "maven", Resolver.identity())))
                .containsExactly("groovy/runtime/maven/org.apache.groovy/groovy/RELEASE");
    }

    @Test
    public void requires_step_fails_when_no_supported_prefix_is_registered() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovy",
                new GroovyCompilerModule(Map.of(), Map.of("vendor", Resolver.identity())),
                "project");

        assertThatThrownBy(() -> executor.execute("groovy/" + GroovyCompilerModule.REQUIRED))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("No suitable resolver for the Groovy compiler")
                .hasMessageContaining("[vendor]");
    }

    @Test
    public void exposes_the_classes_and_the_resolved_compiler_alone() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovy",
                new GroovyCompilerModule(Map.of(), Map.of("maven", (_, _, _, _, _, _) -> new Resolver.Resolution(
                        new LinkedHashMap<>(),
                        List.of(),
                        new LinkedHashMap<>()))),
                "project");

        SequencedMap<String, Path> outputs = executor.execute();

        assertThat(outputs)
                .as("the resolved coordinates reach the pin stage beside the classes, and nothing else leaves the module")
                .containsKeys("groovy/classes", "groovy/artifacts")
                .doesNotContainKeys("groovy/required", "groovy/compiled");
    }

    @Test
    public void compiles_the_groovy_sources_against_the_classes_upstream_and_no_resource() throws IOException {
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("Sample.groovy"), "package sample\nclass Sample {}");
        Files.writeString(sample.resolve("Greeter.java"), "package sample; public class Greeter {}");
        Files.writeString(sample.resolve("app.properties"), "key=value");
        Path classes = Files.createDirectories(compiled.resolve(BuildStep.CLASSES));

        compile();

        assertThat(CAPTURED)
                .as("groovyc runs with the downloaded compiler and reads the classes javac wrote")
                .contains("org.codehaus.groovy.tools.FileSystemCompiler")
                .contains(sample.resolve("Sample.groovy").toString())
                .doesNotContain(sample.resolve("Greeter.java").toString());
        assertThat(CAPTURED.get(CAPTURED.indexOf("--classpath") + 1)).contains(classes.toString());
        assertThat(root.resolve("groovy").resolve(GroovyCompilerModule.COMPILED).resolve("output")
                .resolve(BuildStep.CLASSES).resolve("sample/app.properties"))
                .as("the build copies the resources of a module beside a language, so groovyc copies none")
                .doesNotExist();
    }

    @Test
    public void runs_no_compiler_without_a_groovy_source() throws IOException {
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("Greeter.java"), "package sample; public class Greeter {}");

        compile();

        assertThat(CAPTURED).isEmpty();
    }

    private void compile() throws IOException {
        Path jar = Files.createFile(project.resolve("groovy.jar"));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addSource("classes", compiled);
        executor.addModule("groovy",
                new GroovyCompilerModule(Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(jar))),
                        Map.of("maven", Resolver.identity()))
                        .factory(commands -> {
                            CAPTURED.addAll(commands);
                            return ProcessHandler.OfTool.of(new Silent()).apply(commands);
                        }),
                "project",
                "classes");
        executor.execute();
    }

    private Set<String> required(Map<String, Resolver> resolvers) throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovy", new GroovyCompilerModule(Map.of(), resolvers), "project");
        executor.execute("groovy/" + GroovyCompilerModule.REQUIRED);

        Path output = root.resolve("groovy").resolve(GroovyCompilerModule.REQUIRED).resolve("output");
        assertThat(output.resolve(BuildStep.VERSIONS)).doesNotExist();
        return SequencedProperties.ofFiles(output.resolve(BuildStep.REQUIRES)).stringPropertyNames();
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
            return "groovyc";
        }

        @Override
        public int run(PrintWriter out, PrintWriter err, String... args) {
            return 0;
        }
    }
}
