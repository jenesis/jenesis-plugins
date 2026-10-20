package build.jenesis.plugin.jreleaser.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.jreleaser.JReleaserFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class JReleaserExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void runs_jreleaser_in_the_build_with_the_version_the_build_resolved() throws Exception {
        ExampleProject project = ExampleProject.of(folder, JReleaserFactory.class)
                .write("build.jenesis/jreleaser.properties", """
                        command=config
                        JRELEASER_GITHUB_TOKEN=unused
                        configuration=../jreleaser.yml
                        """)
                .write("jreleaser.yml", """
                        project:
                          name: example
                          description: An example
                          copyright: none
                          authors: [someone]
                          license: Apache-2.0
                          links:
                            homepage: https://example.org
                        release:
                          github:
                            owner: someone
                            skipRelease: true
                            skipTag: true
                        """)
                .write("sources/module-info.java", "module example { }\n");
        for (List<String> command : List.of(List.of("git", "init", "-q"),
                List.of("git", "remote", "add", "origin", "https://github.com/someone/example.git"),
                List.of("git",
                        "-c",
                        "user.name=someone",
                        "-c",
                        "user.email=someone@example.org",
                        "commit",
                        "-q",
                        "--allow-empty",
                        "-m",
                        "initial"))) {
            assertThat(new ProcessBuilder(command).directory(project.root().toFile()).inheritIO().start().waitFor())
                    .as("git prepares the checkout JReleaser reads")
                    .isZero();
        }

        String printed = project.make("-Djenesis.release.publish=true", "-Djenesis.project.version=1.2.3", "release");

        assertThat(printed)
                .as("JReleaser runs in the build's JVM, prints through it, and releases the version the build resolved")
                .contains("Project version set to 1.2.3");
        assertThat(project.find("trace.log"))
                .as("JReleaser writes its output into the step's folder, not into the project")
                .content()
                .contains("example");
        assertThat(project.root().resolve("out")).doesNotExist();
    }
}
