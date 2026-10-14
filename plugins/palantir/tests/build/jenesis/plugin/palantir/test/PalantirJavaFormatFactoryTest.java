package build.jenesis.plugin.palantir.test;

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
import build.jenesis.plugin.palantir.PalantirJavaFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PalantirJavaFormatFactoryTest {

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
        executor.addModule("palantir", create(Map.of()), "project");
        executor.execute("palantir/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("palantir")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("the formatter is resolved in the group the loading build names")
                .containsExactly("quality/runtime/maven/com.palantir.javaformat/palantir-java-format/RELEASE");
    }

    @Test
    public void takes_the_reflow_and_the_imports() {
        assertThat(create(Map.of("reflow", "false", "imports", "remove"))).isNotNull();
        assertThat(create(Map.of("imports", "none"))).isNotNull();
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("javadoc", "false")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("javadoc")
                .hasMessageContaining("[imports, reflow]");
    }

    @Test
    public void refuses_imports_palantir_java_format_does_not_know() {
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
        return new PalantirJavaFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of());
    }
}
