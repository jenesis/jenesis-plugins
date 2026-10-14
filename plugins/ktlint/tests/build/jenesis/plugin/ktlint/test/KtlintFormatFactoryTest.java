package build.jenesis.plugin.ktlint.test;

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
import build.jenesis.plugin.ktlint.KtlintFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KtlintFormatFactoryTest {

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
        executor.addModule("ktlint", new KtlintFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("ktlint/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("ktlint")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .isNotEmpty()
                .allMatch(key -> key.startsWith("quality/"));
    }

    @Test
    public void refuses_values_since_its_settings_configure_it() {
        assertThatThrownBy(() -> new KtlintFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("unknown", "value")),
                Permits.of(1),
                "ktlint",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }
}
