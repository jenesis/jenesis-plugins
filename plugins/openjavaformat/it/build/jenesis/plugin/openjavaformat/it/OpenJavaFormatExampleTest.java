package build.jenesis.plugin.openjavaformat.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.openjavaformat.OpenJavaFormatFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenJavaFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_java_sources_leaving_the_imports_as_the_project_says() throws IOException {
        ExampleProject project = ExampleProject.of(folder, OpenJavaFormatFactory.class)
                .write("jenesis.plugins.properties", "openjavaformat+format=build.jenesis.plugin.openjavaformat\n")
                .write("build.jenesis/plugin-openjavaformat.properties", "imports=none\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        import java.util.Map;
                        import java.util.List;
                        public class Example {
                        int value(){return 1;}
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "build/::/verify-module-sources/format");

        assertThat(project.root().resolve("sources/example/Example.java"))
                .as("the plugin runs through the build that loads it, rewrites the source in Open Java Format style and"
                        + " keeps the unused imports in their order as it is handed")
                .content()
                .contains("import java.util.Map;\nimport java.util.List;")
                .contains("public class Example {\n    int value() {\n        return 1;\n    }\n}");
    }
}
