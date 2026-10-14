package build.jenesis.plugin.revapi.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.revapi.RevapiFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RevapiFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_and_the_baseline_it_names_in_the_group_it_is_handed() throws IOException {
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
        executor.addModule("revapi", create(Map.of("baseline", "com.example/library/1.0"), "quality"), "project");
        executor.execute("revapi/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("revapi")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .contains("quality/baseline/maven/com.example/library/1.0")
                .allMatch(key -> key.startsWith("quality/"));
    }

    @Test
    public void refuses_a_configuration_named_as_a_value_since_a_file_is_bound() {
        assertThatThrownBy(() -> create(Map.of("configuration", "analysis.json"), "revapi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@analysis/revapi.json=<file>");
    }

    @Test
    public void refuses_a_severity_that_revapi_does_not_know_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("severity", "fatal"), "revapi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("[equivalent, non-breaking, potentially-breaking, breaking]");
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("unknown", "value"), "revapi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names unknown");
    }

    private BuildExecutorModule create(Map<String, String> values, String group) {
        return new RevapiFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                group,
                Map.of(),
                Map.of());
    }

    @Test
    public void wires_nothing_when_its_setting_says_ignore() throws IOException {
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
        executor.addModule("revapi", new RevapiFactory().create(new Environment(Map.of("artifact.revapi", "ignore")),
                new LinkedHashMap<>(),
                Permits.of(1),
                "revapi",
                Map.of(),
                Map.of()), "project");

        assertThat(executor.execute().sequencedKeySet()).noneMatch(key -> key.startsWith("revapi/"));
    }
}
