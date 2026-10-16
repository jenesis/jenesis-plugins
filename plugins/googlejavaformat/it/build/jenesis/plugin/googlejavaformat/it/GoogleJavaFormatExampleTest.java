package build.jenesis.plugin.googlejavaformat.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.googlejavaformat.GoogleJavaFormatFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleJavaFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_java_sources_in_the_style_the_project_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, GoogleJavaFormatFactory.class)
                .write("jenesis.plugins.properties", "googlejavaformat+format=build.jenesis.plugin.googlejavaformat\n")
                .write("build.jenesis/plugin-googlejavaformat.properties", "style=aosp\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                        int value(){return 1;}
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "build/::/verify-module-sources/format");

        assertThat(project.root().resolve("sources/example/Example.java"))
                .as("the plugin runs through the build that loads it and rewrites the source in the AOSP style it is"
                        + " handed, indented by four")
                .content()
                .contains("public class Example {\n    int value() {\n        return 1;\n    }\n}");
    }
}
