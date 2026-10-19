package build.jenesis.plugin.openrewrite.test;

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
import build.jenesis.plugin.openrewrite.OpenRewriteFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OpenRewriteFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_parser_and_a_logger_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
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
        executor.addModule("openrewrite",
                create(Map.of("recipes", "org.openrewrite.java.RemoveUnusedImports")),
                "project");
        executor.execute("openrewrite/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("openrewrite")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("OpenRewrite is resolved in the tool group of the plugin the loading build names")
                .containsExactlyInAnyOrder(
                        "quality:tool/runtime/maven/org.openrewrite/rewrite-java-25/RELEASE",
                        "quality:tool/runtime/maven/org.slf4j/slf4j-nop/RELEASE");
    }

    @Test
    public void takes_the_recipes_comma_separated() {
        assertThat(create(Map.of("recipes", "org.openrewrite.java.RemoveUnusedImports, demo.Cleanup")))
                .isNotNull();
    }

    @Test
    public void takes_no_recipe_to_run_those_its_rewrite_yml_declares() {
        assertThat(create(Map.of())).isNotNull();
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_the_one_it_does() {
        assertThatThrownBy(() -> create(Map.of("recipes", "demo.Cleanup", "activeRecipes", "demo.Cleanup")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("activeRecipes")
                .hasMessageContaining("its one value is recipes");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new OpenRewriteFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of());
    }
}
