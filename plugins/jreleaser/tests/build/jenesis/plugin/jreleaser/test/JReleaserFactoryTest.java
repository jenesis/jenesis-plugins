package build.jenesis.plugin.jreleaser.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Resolver;
import build.jenesis.plugin.jreleaser.JReleaserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JReleaserFactoryTest {

    @TempDir
    private Path root, project, tool;

    @Test
    public void runs_the_command_and_settings_it_is_handed_for_real_when_the_release_publishes() throws IOException {
        BuildExecutorModule module = new JReleaserFactory().create(new Environment(Map.of("release.publish", "true")),
                new LinkedHashMap<>(Map.of("command", "deploy", "JRELEASER_GITHUB_TOKEN", "secret")),
                Permits.of(1),
                "plugin-jreleaser",
                Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity()));
        BuildExecutor executor = FakeTool.executor(root);
        executor.addSource("project", FakeTool.project(project, root, null));
        executor.addModule("jreleaser", module, "project");
        executor.execute("jreleaser/release");

        Path output = root.resolve("jreleaser/release/output");
        assertThat(output.resolve("arguments.txt")).content()
                .startsWith("deploy\n")
                .doesNotContain("--dry-run");
        assertThat(output.resolve("settings.txt")).content().contains("JRELEASER_GITHUB_TOKEN=secret");
        assertThat(root.resolve("jreleaser/dependencies"))
                .as("JReleaser resolves in the group the loading build names")
                .exists();
    }

    @Test
    public void refuses_a_value_it_does_not_take() {
        assertThatThrownBy(() -> create(Map.of("config", "jreleaser.yml")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names config")
                .hasMessageContaining("@jreleaser=<file>");
    }

    @Test
    public void refuses_a_value_that_would_publish_naming_the_setting_that_does() {
        assertThatThrownBy(() -> create(Map.of("dry", "false")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names dry")
                .hasMessageContaining("-Djenesis.release.publish=true");
    }

    private static BuildExecutorModule create(Map<String, String> values) {
        return new JReleaserFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "plugin-jreleaser",
                Map.of(),
                Map.of());
    }
}
