package build.jenesis.plugin.spotbugs.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.spotbugs.SpotBugsFactory;
import build.jenesis.plugin.spotbugs.SpotBugsModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpotBugsModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_emits_the_spotbugs_maven_coordinate() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("spotbugs", new SpotBugsModule(Map.of(), Map.of()), "project");
        executor.execute("spotbugs/required");

        Path requiredOutput = root.resolve("spotbugs").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("spotbugs/runtime/maven/com.github.spotbugs/spotbugs/RELEASE");
    }

    @Test
    public void refuses_values_since_its_settings_configure_it() {
        assertThatThrownBy(() -> new SpotBugsFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("effort", "max")),
                new Semaphore(1),
                "spotbugs",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("[effort]")
                .hasMessageContaining("jenesis.validator.spotbugs");
    }

    @Test
    public void requires_the_tool_in_the_group_it_is_handed() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("spotbugs", new SpotBugsFactory().create(Environment.NONE,
                new LinkedHashMap<>(),
                new Semaphore(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("spotbugs/required");

        Path requiredOutput = root.resolve("spotbugs").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("quality/runtime/maven/com.github.spotbugs/spotbugs/RELEASE");
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
