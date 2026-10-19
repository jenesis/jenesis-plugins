package build.jenesis.plugin.asciidoctor.test;

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
import build.jenesis.plugin.asciidoctor.AsciidoctorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsciidoctorFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_asciidoctorj_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
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
        executor.addModule("asciidoctor", create(Map.of()), "project");
        executor.execute("asciidoctor/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("asciidoctor")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("AsciidoctorJ is resolved in the tool group of the plugin the loading build names")
                .containsExactly("manual:tool/runtime/maven/org.asciidoctor/asciidoctorj/RELEASE");
    }

    @Test
    public void takes_a_backend_an_output_folder_and_attributes() {
        assertThat(create(Map.of("backend", "docbook5", "output", "manual/html", "attribute.product-version", "1.2.3")))
                .isNotNull();
    }

    @Test
    public void refuses_a_backend_it_does_not_convert_to_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("backend", "pdf")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backend=pdf")
                .hasMessageContaining("html5");
    }

    @Test
    public void refuses_an_output_folder_outside_the_staged_files() {
        assertThatThrownBy(() -> create(Map.of("output", "../elsewhere")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("output=../elsewhere")
                .hasMessageContaining("name a relative folder");
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_those_it_does() {
        assertThatThrownBy(() -> create(Map.of("doctype", "book")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doctype")
                .hasMessageContaining("attribute.<name>");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new AsciidoctorFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "manual",
                Map.of(),
                Map.of());
    }
}
