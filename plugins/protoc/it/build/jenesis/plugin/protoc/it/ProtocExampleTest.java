package build.jenesis.plugin.protoc.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.protoc.ProtocFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtocExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void generates_classes_from_the_definition_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ProtocFactory.class)
                .write("jenesis.plugins.properties", "protoc+binary/generated=build.jenesis.plugin.protoc\n")
                .write("build.jenesis/plugin-protoc.properties", "@definitions/protoc=../protoc\n")
                .write("protoc/greeting.proto", """
                        syntax = "proto3";
                        package test;
                        option java_package = "test.greeting";
                        option java_outer_classname = "GreetingProto";
                        message Greeting {
                          string text = 1;
                        }
                        service Greeter {
                          rpc Greet (Greeting) returns (Greeting);
                        }
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("+sources/produce/assemble/binary/generated");

        assertThat(project.find("GreetingProto.java"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("package test.greeting;");
    }
}
