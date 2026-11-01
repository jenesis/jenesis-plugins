package build.jenesis.plugin.princeofspace.test;

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
import build.jenesis.plugin.princeofspace.PrinceOfSpaceFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PrinceOfSpaceFactoryTest {

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
        executor.addModule("princeofspace", create(Map.of()), "project");
        executor.execute("princeofspace/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("princeofspace")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("the formatter is resolved in the tool group of the plugin the loading build names")
                .containsExactly("princeofspace:tool/runtime/maven/io.github.agustafson.princeofspace"
                        + "/prince-of-space-core/RELEASE");
    }

    @Test
    public void takes_every_option_the_formatter_offers() {
        assertThat(create(Map.of("wrap-style", "narrow",
                "indent-style", "tabs",
                "indent-size", "2",
                "line-length", "100",
                "closing-paren-on-new-line", "false",
                "trailing-commas", "true",
                "java-version", "25"))).isNotNull();
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("style", "wide")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("style")
                .hasMessageContaining("[closing-paren-on-new-line, indent-size, indent-style, java-version,"
                        + " line-length, trailing-commas, wrap-style]");
    }

    @Test
    public void refuses_a_wrap_style_the_formatter_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("wrap-style", "compact")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wrap-style=compact")
                .hasMessageContaining("[balanced, wide, narrow]");
    }

    @Test
    public void refuses_an_indent_style_the_formatter_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("indent-style", "mixed")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indent-style=mixed")
                .hasMessageContaining("[spaces, tabs]");
    }

    @Test
    public void refuses_a_length_that_is_no_positive_number() {
        assertThatThrownBy(() -> create(Map.of("line-length", "wide")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("line-length=wide");
        assertThatThrownBy(() -> create(Map.of("indent-size", "0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indent-size=0")
                .hasMessageContaining("at least 1");
    }

    @Test
    public void refuses_a_java_version_below_eight() {
        assertThatThrownBy(() -> create(Map.of("java-version", "7")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java-version=7")
                .hasMessageContaining("at least 8");
    }

    @Test
    public void refuses_a_flag_that_is_neither_true_nor_false() {
        assertThatThrownBy(() -> create(Map.of("trailing-commas", "sometimes")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sometimes");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new PrinceOfSpaceFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "princeofspace",
                Map.of(),
                Map.of());
    }
}
