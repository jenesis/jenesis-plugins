package build.jenesis.plugin.xjc.test;

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
import build.jenesis.plugin.xjc.XjcFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XjcFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_in_the_group_it_is_handed() throws IOException {
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
        executor.addModule("xjc", new XjcFactory().create(Environment.NONE,
                new LinkedHashMap<>(),
                new Semaphore(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("xjc/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("xjc")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .isNotEmpty()
                .allMatch(key -> key.startsWith("quality/"));
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> new XjcFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("unknown", "value")),
                new Semaphore(1),
                "xjc",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }
}
