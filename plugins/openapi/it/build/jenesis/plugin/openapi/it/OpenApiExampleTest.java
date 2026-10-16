package build.jenesis.plugin.openapi.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.openapi.OpenApiFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_models_from_the_specification_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, OpenApiFactory.class)
                .write("jenesis.plugins.properties", "openapi+binary/generated=build.jenesis.plugin.openapi\n")
                .write("build.jenesis/plugin-openapi.properties", """
                        package=example.api
                        @specification/openapi/openapi.yml=../openapi.yml
                        """)
                .write("openapi.yml", """
                        openapi: 3.0.3
                        info:
                          title: Greeting API
                          version: 1.0.0
                        paths: {}
                        components:
                          schemas:
                            Greeting:
                              type: object
                              properties:
                                text:
                                  type: string
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("Greeting.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
