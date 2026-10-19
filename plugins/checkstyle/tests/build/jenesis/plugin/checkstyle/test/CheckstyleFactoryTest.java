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
import build.jenesis.Permits;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.checkstyle.CheckstyleFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CheckstyleFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
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
        executor.addModule("checkstyle", new CheckstyleFactory().create(Environment.NONE,
                new LinkedHashMap<>(),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("checkstyle/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("checkstyle")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .isNotEmpty()
                .allMatch(key -> key.startsWith("quality:tool/"));
    }

    @Test
    public void takes_its_values_as_the_properties_the_configuration_expands() {
        assertThat(new CheckstyleFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("checkstyle.severity", "warning")),
                Permits.of(1),
                "checkstyle",
                Map.of(),
                Map.of())).isNotNull();
    }

    @Test
    public void refuses_a_value_for_config_loc_which_the_build_sets() {
        assertThatThrownBy(() -> new CheckstyleFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("config_loc", "elsewhere")),
                Permits.of(1),
                "checkstyle",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("config_loc");
    }
}
