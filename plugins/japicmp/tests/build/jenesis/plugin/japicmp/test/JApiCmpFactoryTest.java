package build.jenesis.plugin.japicmp.test;

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
import build.jenesis.plugin.japicmp.JApiCmpFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JApiCmpFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_and_the_baseline_it_names_in_the_tool_group_of_the_plugin() throws IOException {
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
        executor.addModule("japicmp", create(Map.of("baseline", "com.example/library/1.0"), "quality"), "project");
        executor.execute("japicmp/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("japicmp")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .contains("quality:tool/baseline/maven/com.example/library/1.0")
                .allMatch(key -> key.startsWith("quality:tool/"));
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("unknown", "value"), "japicmp"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names unknown");
    }

    @Test
    public void refuses_an_access_that_japicmp_does_not_know_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("access", "internal"), "japicmp"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("internal")
                .hasMessageContaining("[public, protected, package, private]");
    }

    private build.jenesis.BuildExecutorModule create(Map<String, String> values, String group) {
        return new JApiCmpFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                group,
                Map.of(),
                Map.of());
    }
}
