package build.jenesis.plugin.ktfmt.test;

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
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.ktfmt.KtfmtFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KtfmtFormatFactoryTest {

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
        executor.addModule("ktfmt", create(Map.of()), "project");
        executor.execute("ktfmt/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("ktfmt")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("the formatter is resolved in the group the loading build names")
                .containsExactly("quality/runtime/maven/com.facebook/ktfmt/RELEASE");
    }

    @Test
    public void takes_the_style_and_the_imports() {
        assertThat(create(Map.of("style", "kotlinlang", "imports", "sort"))).isNotNull();
        assertThat(create(Map.of("style", "google", "imports", "remove,sort"))).isNotNull();
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("reflow", "false")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reflow")
                .hasMessageContaining("[imports, style]");
    }

    @Test
    public void refuses_a_style_ktfmt_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("style", "dropbox")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("style=dropbox")
                .hasMessageContaining("meta, google or kotlinlang");
    }

    @Test
    public void refuses_imports_that_do_not_sort_since_ktfmt_always_sorts_them() {
        assertThatThrownBy(() -> create(Map.of("imports", "remove")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imports=remove")
                .hasMessageContaining("always sorts");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new KtfmtFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                new Semaphore(1),
                "quality",
                Map.of(),
                Map.of());
    }
}
