package build.jenesis.plugin.ktlint.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.kotlin.KotlinFactory;
import build.jenesis.plugin.ktlint.KtlintFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class KtlintExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_kotlin_sources_as_the_editorconfig_a_project_binds_says() throws IOException {
        ExampleProject project = ExampleProject.of(folder, KtlintFactory.class, KotlinFactory.class)
                .write("jenesis.plugins.properties", "kotlin+language=build.jenesis.plugin.kotlin\nktlint+check=build.jenesis.plugin.ktlint@check\n")
                .write("build.jenesis/plugin-ktlint.properties", "@rules=../.editorconfig\n")
                .write(".editorconfig", """
                        root = true
                        [*.kt]
                        indent_size = 4
                        """)
                .write("sources/module-info.java", "module example { requires kotlin.stdlib; }\n")
                .write("sources/sample/Sample.kt", """
                        package sample
                        fun main() {
                        println("hello")
                        }
                        """);

        project.make("build/::/verify-module-sources/check");

        assertThat(project.find("ktlint-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
