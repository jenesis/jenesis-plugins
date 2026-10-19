package build.jenesis.plugin.asciidoctor.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.asciidoctor.AsciidoctorFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class AsciidoctorExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void stages_the_documents_of_the_project_converted_to_html() throws IOException {
        ExampleProject project = ExampleProject.of(folder, AsciidoctorFactory.class)
                .write("jenesis.plugins.properties", "asciidoctor+postprocess/transform=build.jenesis.plugin.asciidoctor\n")
                .write("jenesis.plugins.arguments.properties", """
                        asciidoctor.@docs=docs
                        asciidoctor.attribute.product-version=1.2.3
                        """)
                .write("docs/index.adoc", """
                        = Example

                        Version {product-version}.
                        """)
                .write("sources/module-info.java", "module example {}\n");

        project.make("stage");

        assertThat(project.root().resolve("target/stage/project/output/docs/index.html"))
                .as("the plugin runs through the build that loads it and stages what it converts beside the modules")
                .content()
                .contains("Version 1.2.3.");
    }
}
