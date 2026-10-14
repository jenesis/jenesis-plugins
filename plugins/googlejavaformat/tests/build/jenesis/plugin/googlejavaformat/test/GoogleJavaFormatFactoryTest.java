package build.jenesis.plugin.googlejavaformat.test;

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
import build.jenesis.plugin.googlejavaformat.GoogleJavaFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GoogleJavaFormatFactoryTest {

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
        executor.addModule("googlejavaformat", create(Map.of()), "project");
        executor.execute("googlejavaformat/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("googlejavaformat")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("the formatter is resolved in the group the loading build names")
                .containsExactly("quality/runtime/maven/com.google.googlejavaformat/google-java-format/RELEASE");
    }

    @Test
    public void takes_the_style_the_reflow_the_javadoc_and_the_imports() {
        assertThat(create(Map.of("style", "aosp", "reflow", "false", "javadoc", "false", "imports", "sort")))
                .isNotNull();
        assertThat(create(Map.of("imports", "none"))).isNotNull();
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("formatter", "google")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formatter")
                .hasMessageContaining("[imports, javadoc, reflow, style]");
    }

    @Test
    public void refuses_a_style_google_java_format_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("style", "palantir")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("style=palantir")
                .hasMessageContaining("google or aosp");
    }

    @Test
    public void refuses_imports_google_java_format_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("imports", "sort,order")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imports=sort,order")
                .hasMessageContaining("none");
    }

    @Test
    public void refuses_a_reflow_that_is_neither_true_nor_false() {
        assertThatThrownBy(() -> create(Map.of("reflow", "never")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("never");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new GoogleJavaFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of());
    }
}
