package build.jenesis.plugin.spotbugs.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.spotbugs.SpotBugsFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class SpotBugsExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_classes_a_project_compiles() throws IOException {
        ExampleProject project = ExampleProject.of(folder, SpotBugsFactory.class)
                .write("jenesis.plugins.properties", "spotbugs+binary/validate=build.jenesis.plugin.spotbugs\n")
                .write("build.jenesis/plugin-spotbugs.properties", "@rules=../spotbugs-exclude.xml\n")
                .write("spotbugs-exclude.xml", "<FindBugsFilter/>\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                            public boolean check(String value) {
                                return value == "expected";
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("spotbugs-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("ES_COMPARING");
    }
}
