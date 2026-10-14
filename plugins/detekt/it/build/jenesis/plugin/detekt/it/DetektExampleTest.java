package build.jenesis.plugin.detekt.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.detekt.DetektFactory;
import build.jenesis.plugin.kotlin.KotlinFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class DetektExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_kotlin_sources_with_the_configuration_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, DetektFactory.class, KotlinFactory.class)
                .write("jenesis.plugins.properties", "kotlin+language=build.jenesis.plugin.kotlin\ndetekt+check=build.jenesis.plugin.detekt\n")
                .write("build.jenesis/plugin-detekt.properties", "@rules=../detekt.yml\n")
                .write("detekt.yml", """
                        style:
                          active: true
                          MaxLineLength:
                            active: true
                            maxLineLength: 10
                        """)
                .write("sources/module-info.java", "module example { requires kotlin.stdlib; }\n")
                .write("sources/sample/Sample.kt", """
                        package sample
                        fun greetTheEntireWorld(): Int = 42
                        """);

        project.make("build/::/verify-module-sources/check");

        assertThat(project.find("detekt-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
