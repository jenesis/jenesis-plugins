package build.jenesis.plugin.ktfmt.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.ktfmt.KtfmtFormatFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class KtfmtFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_kotlin_sources_in_the_style_the_project_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, KtfmtFormatFactory.class)
                .write("jenesis.plugins.properties", "ktfmt+format=build.jenesis.plugin.ktfmt\n")
                .write("build.jenesis/plugin-ktfmt.properties", "style=kotlinlang\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/sample/Sample.kt", """
                        package sample

                        fun main() {
                        println("hello")
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "+sources/produce/assemble/format");

        assertThat(project.root().resolve("sources/sample/Sample.kt"))
                .as("the plugin runs through the build that loads it and rewrites the source in the Kotlin language"
                        + " style it is handed, indented by four")
                .content()
                .contains("\n    println(\"hello\")");
    }
}
