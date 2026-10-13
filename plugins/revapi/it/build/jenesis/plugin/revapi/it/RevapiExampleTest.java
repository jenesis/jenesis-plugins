package build.jenesis.plugin.revapi.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.revapi.RevapiFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class RevapiExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void analyses_the_artifact_a_project_builds_against_the_baseline_it_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, RevapiFactory.class)
                .write("jenesis.plugins.properties", "revapi+artifact=build.jenesis.plugin.revapi\n")
                .write("build.jenesis/plugin-revapi.properties", "baseline=javax.inject/javax.inject/1\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                        }
                        """);

        project.make();

        assertThat(project.find("revapi.json"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
