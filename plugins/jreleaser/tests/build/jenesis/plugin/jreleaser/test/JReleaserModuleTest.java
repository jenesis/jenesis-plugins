package build.jenesis.plugin.jreleaser.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorException;
import build.jenesis.BuildStep;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.jreleaser.JReleaserModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JReleaserModuleTest {

    @TempDir
    private Path root, project, tool;

    @Test
    public void requires_the_tool_provider_of_jreleaser_in_its_group() throws IOException {
        BuildExecutor executor = FakeTool.executor(root);
        executor.addSource("project", FakeTool.project(project, root, "1.2.3"));
        executor.addModule("jreleaser", new JReleaserModule(Map.of(), Map.of()).tool("plugin-jreleaser"), "project");
        executor.execute("jreleaser/required");

        assertThat(SequencedProperties.ofFiles(root.resolve("jreleaser/required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames())
                .containsExactly("plugin-jreleaser/runtime/maven/org.jreleaser/jreleaser-tool-provider/RELEASE");
    }

    @Test
    public void runs_jreleaser_in_the_build_jvm_in_the_root_with_what_the_build_resolved() throws IOException {
        List<String> printed = new ArrayList<>();
        Path output = release(new JReleaserModule(Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity())).out(printed::add), "1.2.3");

        assertThat(output.resolve("arguments.txt")).content()
                .as("JReleaser runs the command it is set to, in the project's root, as a rehearsal by default")
                .startsWith("full-release\n--basedir\n" + root + "\n--config-file\n" + project.resolve("jreleaser.yml"))
                .contains("--dry-run")
                .as("the metadata and the staged folders the build resolved are template properties")
                .contains("--set-property\njenesisScmUrl=https://example.org/sample")
                .contains("--set-property\njenesisStageMaven=" + root.resolve("staged/maven"))
                .contains("--set-property\njenesisVersion=1.2.3")
                .as("the root is the base folder, not a property")
                .doesNotContain("jenesisRoot");
        assertThat(output.resolve("settings.txt")).content()
                .as("the version the build resolved is the version JReleaser releases")
                .contains("JRELEASER_PROJECT_VERSION=1.2.3");
        assertThat(output.resolve("loader.txt"))
                .as("the tool runs with its own loader as the context class loader")
                .hasContent("true");
        assertThat(printed).contains("released full-release");
    }

    @Test
    public void hands_jreleaser_every_property_whose_name_is_as_long_as_the_properties_are_many_less_one() throws IOException {
        BuildExecutor executor = FakeTool.executor(root);
        Path folder = FakeTool.project(project, root, "1.2.3");
        SequencedProperties metadata = SequencedProperties.ofFiles(folder.resolve(BuildStep.METADATA));
        for (int index = 1; index <= 12; index++) {
            metadata.setProperty("k" + index, "v" + index);
        }
        metadata.store(folder.resolve(BuildStep.METADATA));
        executor.addSource("project", folder);
        executor.addModule("jreleaser", new JReleaserModule(Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity())), "project");
        executor.execute("jreleaser/release");

        assertThat(root.resolve("jreleaser/release/output/arguments.txt")).content()
                .as("fifteen properties and a name of fourteen characters, which JReleaser refuses unless more follow")
                .contains("--set-property\njenesisVersion=1.2.3")
                .contains("--set-property\njenesis\n");
    }

    @Test
    public void hands_jreleaser_its_settings_and_no_settings_file_without_any() throws IOException {
        Path output = release(new JReleaserModule(Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity()))
                .command("release")
                .dry(false)
                .settings(new LinkedHashMap<>(Map.of("JRELEASER_GITHUB_TOKEN", "secret"))), null);

        assertThat(output.resolve("arguments.txt")).content()
                .startsWith("release\n")
                .doesNotContain("--dry-run");
        assertThat(output.resolve("settings.txt")).content()
                .contains("JRELEASER_GITHUB_TOKEN=secret")
                .doesNotContain("JRELEASER_PROJECT_VERSION");
        assertThat(root.resolve("jreleaser/release/supplement/settings.properties"))
                .as("the settings, which may hold a credential, do not outlive the run")
                .doesNotExist();
    }

    @Test
    public void fails_when_jreleaser_fails() throws IOException {
        assertThatThrownBy(() -> release(new JReleaserModule(Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity())).command("fail"), "1.2.3"))
                .isInstanceOf(BuildExecutorException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("JReleaser fail failed with exit code 3 - its log is jreleaser/trace.log in"
                        + " the output of the step");
    }

    @Test
    public void refuses_to_run_outside_the_hook_point_release() throws IOException {
        Files.writeString(project.resolve("jreleaser.yml"), "project:\n  name: sample\n");
        BuildExecutor executor = FakeTool.executor(root);
        executor.addSource("project", project);
        executor.addModule("jreleaser", new JReleaserModule(Map.of("maven", FakeTool.of(tool).repository()),
                Map.of("maven", Resolver.identity())), "project");

        assertThatThrownBy(() -> executor.execute("jreleaser/release"))
                .isInstanceOf(BuildExecutorException.class)
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hook point release");
    }

    @Test
    public void refuses_a_command_that_is_blank_and_a_setting_that_is_none_of_jreleaser() {
        assertThatThrownBy(() -> new JReleaserModule(Map.of(), Map.of()).command(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("full-release");
        assertThatThrownBy(() -> new JReleaserModule(Map.of(), Map.of())
                .settings(new LinkedHashMap<>(Map.of("GITHUB_TOKEN", "secret"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JRELEASER_*");
    }

    private Path release(JReleaserModule module, String version) throws IOException {
        BuildExecutor executor = FakeTool.executor(root);
        executor.addSource("project", FakeTool.project(project, root, version));
        executor.addModule("jreleaser", module, "project");
        executor.execute("jreleaser/release");
        return root.resolve("jreleaser/release/output");
    }
}
