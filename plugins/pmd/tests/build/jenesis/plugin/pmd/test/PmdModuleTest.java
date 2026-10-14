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
import build.jenesis.Permits;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.pmd.PmdFactory;
import build.jenesis.plugin.pmd.PmdModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PmdModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_emits_the_pmd_maven_coordinate() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pmd", new PmdModule(Map.of(), Map.of()), "project");
        executor.execute("pmd/required");

        Path requiredOutput = root.resolve("pmd").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("pmd/runtime/maven/net.sourceforge.pmd/pmd-dist/RELEASE");
    }

    @Test
    public void refuses_a_rule_priority_that_pmd_does_not_know() {
        assertThatThrownBy(() -> PmdModule.ofEnvironment(new Environment(Map.of("pmd.priority", "6")), Map.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jenesis.pmd.priority")
                .hasMessageContaining("from 1, the highest, to 5, the lowest");
    }

    @Test
    public void creates_the_module_from_the_settings_of_the_build_that_loads_it() {
        assertThatThrownBy(() -> new PmdFactory().create(new Environment(Map.of("pmd.priority", "0")),
                new LinkedHashMap<>(),
                Permits.of(1),
                "pmd",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The PMD rule priority 0");
    }

    @Test
    public void refuses_values_since_its_settings_configure_it() {
        assertThatThrownBy(() -> new PmdFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("priority", "3")),
                Permits.of(1),
                "pmd",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("[priority]")
                .hasMessageContaining("jenesis.pmd.priority");
    }

    @Test
    public void requires_the_tool_in_the_group_it_is_handed() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pmd", new PmdFactory().create(Environment.NONE,
                new LinkedHashMap<>(),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("pmd/required");

        Path requiredOutput = root.resolve("pmd").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("quality/runtime/maven/net.sourceforge.pmd/pmd-dist/RELEASE");
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
