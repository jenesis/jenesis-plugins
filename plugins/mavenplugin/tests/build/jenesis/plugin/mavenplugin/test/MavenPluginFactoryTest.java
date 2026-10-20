package build.jenesis.plugin.mavenplugin.test;

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
import build.jenesis.plugin.mavenplugin.MavenPluginFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MavenPluginFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_maven_and_its_plugin_tools_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
        assertThat(required(Map.of()))
                .containsExactly("mavenplugin:tool/runtime/maven/org.apache.maven/maven-core/3.9.11",
                        "mavenplugin:tool/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-annotations/3.15.1",
                        "mavenplugin:tool/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-generators/3.15.1");
    }

    @Test
    public void requires_the_releases_of_maven_and_its_plugin_tools_it_is_named() throws IOException {
        assertThat(required(Map.of("maven", "3.9.9", "tools", "3.15.0")))
                .containsExactly("mavenplugin:tool/runtime/maven/org.apache.maven/maven-core/3.9.9",
                        "mavenplugin:tool/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-annotations/3.15.0",
                        "mavenplugin:tool/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-generators/3.15.0");
    }

    @Test
    public void refuses_a_value_it_does_not_take() {
        assertThatThrownBy(() -> create(Map.of("prefix", "greet")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names prefix, but takes goalprefix, maven, tools");
    }

    @Test
    public void refuses_a_goal_prefix_that_is_no_name() {
        assertThatThrownBy(() -> create(Map.of("goalprefix", "greet:hello")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The goal prefix greet:hello holds more than letters, digits, . and -");
    }

    @Test
    public void refuses_a_release_that_is_no_version() {
        assertThatThrownBy(() -> create(Map.of("tools", "latest")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The tools version latest is not a version - name a release, as 3.15.1");
    }

    private SequencedSet<String> required(Map<String, String> values) throws IOException {
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
        executor.addModule("mavenplugin", create(values), "project");
        executor.execute("mavenplugin/required");
        return new LinkedHashSet<>(SequencedProperties.ofFiles(root.resolve("mavenplugin")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES)).stringPropertyNames());
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new MavenPluginFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "mavenplugin",
                Map.of(),
                Map.of());
    }
}
