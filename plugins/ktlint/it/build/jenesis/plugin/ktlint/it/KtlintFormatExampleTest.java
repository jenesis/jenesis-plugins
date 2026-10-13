package build.jenesis.plugin.ktlint.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.ktlint.KtlintFormatFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class KtlintFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_kotlin_sources_as_the_editorconfig_a_project_binds_says() throws IOException {
        ExampleProject project = ExampleProject.of(folder, KtlintFormatFactory.class)
                .write("jenesis.plugins.properties", "ktlint+format=build.jenesis.plugin.ktlint@format\n")
                .write("build.jenesis/plugin-ktlint.properties", "@rules=../.editorconfig\n")
                .write(".editorconfig", """
                        root = true
                        [*.kt]
                        indent_size = 4
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/sample/Sample.kt", """
                        package sample

                        fun main() {
                        println("hello")
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "+sources/produce/assemble/format");

        assertThat(project.root().resolve("sources/sample/Sample.kt"))
                .as("the plugin runs through the build that loads it and rewrites the source it formats")
                .content()
                .isNotEqualTo("""
                        package sample

                        fun main() {
                        println("hello")
                        }
                        """);
    }
}
