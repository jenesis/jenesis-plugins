package build.jenesis.plugin.princeofspace.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.harness.ExampleProject;
import build.jenesis.plugin.princeofspace.PrinceOfSpaceFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PrinceOfSpaceExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_java_sources_with_the_indentation_the_project_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, PrinceOfSpaceFactory.class)
                .write("jenesis.plugins.properties", "princeofspace+format=build.jenesis.plugin.princeofspace\n")
                .write("build.jenesis/plugin-princeofspace.properties", "indent-size=2\n")
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                        int value(){return 1;}
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "build/::/verify-module-sources/format");

        assertThat(project.root().resolve("sources/example/Example.java"))
                .as("the plugin runs through the build that loads it and rewrites the source with Prince of Space,"
                        + " indenting by the two spaces it is handed")
                .content()
                .contains("public class Example {\n  int value() {\n    return 1;\n  }\n}");
    }
}
