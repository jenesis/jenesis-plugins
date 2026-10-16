package build.jenesis.plugin.avro.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.avro.AvroFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class AvroExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_classes_from_the_schema_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, AvroFactory.class)
                .write("jenesis.plugins.properties", "avro+binary/generated=build.jenesis.plugin.avro\n")
                .write("build.jenesis/plugin-avro.properties", "@schemas/avro=../avro\n")
                .write("avro/user.avsc", """
                        {
                          "namespace": "test.avro",
                          "type": "record",
                          "name": "User",
                          "fields": [{"name": "name", "type": "string"}]
                        }
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("User.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("package test.avro;");
    }
}
