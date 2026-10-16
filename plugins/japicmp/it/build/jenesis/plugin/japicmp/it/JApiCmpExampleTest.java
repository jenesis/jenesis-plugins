package build.jenesis.plugin.japicmp.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.japicmp.JApiCmpFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class JApiCmpExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void compares_the_artifact_a_project_builds_with_the_baseline_it_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, JApiCmpFactory.class)
                .write("jenesis.plugins.properties", "japicmp+artifact=build.jenesis.plugin.japicmp\n")
                .write("build.jenesis/plugin-japicmp.properties", """
                        baseline=org.apiguardian/apiguardian-api/1.1.2
                        format=xml
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                        }
                        """);

        project.make();

        assertThat(project.find("japicmp-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
