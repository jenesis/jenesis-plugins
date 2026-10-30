package build.jenesis.plugin.kotlin.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.Repository;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.kotlin.KotlinCompilerModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KotlinCompilerModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_picks_module_coordinate_when_module_resolver_available() throws IOException {
        assertThat(required(Map.of("module", empty())))
                .containsExactly("kotlin/runtime/module/kotlin.compiler.embeddable");
        assertThat(root.resolve("kotlin/required/output").resolve(BuildStep.VERSIONS)).doesNotExist();
    }

    @Test
    public void requires_step_picks_maven_coordinate_when_only_maven_resolver_available() throws IOException {
        assertThat(required(Map.of("maven", empty())))
                .containsExactly("kotlin/runtime/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/RELEASE");
        assertThat(root.resolve("kotlin/required/output").resolve(BuildStep.VERSIONS)).doesNotExist();
    }

    @Test
    public void requires_step_prefers_maven_over_module_when_both_available() throws IOException {
        assertThat(required(Map.of("module", empty(), "maven", empty())))
                .singleElement()
                .satisfies(name -> assertThat(name).startsWith("kotlin/runtime/maven/"));
    }

    @Test
    public void requires_the_build_tools_implementation_when_compiling_incrementally() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("kotlin",
                new KotlinCompilerModule(Map.of(), Map.of("maven", empty())).incremental(true),
                "project");
        executor.execute("kotlin/required");

        assertThat(SequencedProperties.ofFiles(root.resolve("kotlin/required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames())
                .containsExactly("kotlin/runtime/maven/org.jetbrains.kotlin/kotlin-build-tools-impl/RELEASE");
    }

    @Test
    public void requires_step_fails_when_no_supported_prefix_is_registered() {
        assertThatThrownBy(() -> required(Map.of("vendor", empty())))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("No suitable resolver");
    }

    @Test
    public void tool_emits_an_independent_resolution_trail() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("kotlin",
                new KotlinCompilerModule(Map.of("maven", files()), Map.of("maven", Resolver.identity())).tool("custom"),
                "project");
        executor.execute("kotlin/artifacts");

        assertThat(SequencedProperties.ofFiles(root.resolve("kotlin/artifacts/resolve/output")
                .resolve(BuildStep.DEPENDENCIES)).stringPropertyNames())
                .containsExactly("custom/runtime/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/RELEASE");
    }

    @Test
    public void exposes_resolved_alongside_classes_so_coordinates_reach_the_pin_stage() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("kotlin", new KotlinCompilerModule(Map.of(), Map.of("maven", empty())), "project");

        assertThat(executor.execute()).containsKeys("kotlin/classes", "kotlin/artifacts");
    }

    private Set<String> required(Map<String, Resolver> resolvers) throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("kotlin", new KotlinCompilerModule(Map.of(), resolvers), "project");
        executor.execute("kotlin/required");
        return SequencedProperties.ofFiles(root.resolve("kotlin/required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames();
    }

    private static Resolver empty() {
        return (_, _, _, _, _, _, _, _) -> new Resolver.Resolution(new LinkedHashMap<>(),
                List.of(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>());
    }

    private Repository files() {
        return (_, coordinate, _) -> {
            Path file = Files.write(
                    Files.createDirectories(root.resolve("served")).resolve(coordinate.replace('/', '-') + ".jar"),
                    coordinate.getBytes(StandardCharsets.UTF_8));
            return Optional.of(RepositoryItem.ofFile(file));
        };
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
