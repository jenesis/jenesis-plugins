package build.jenesis.plugin.spotless.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.harness.ExampleProject;
import build.jenesis.plugin.spotless.SpotlessFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SpotlessExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void formats_the_json_files_of_a_module_with_the_steps_the_project_names() throws IOException {
        ExampleProject project = ExampleProject.of(folder, SpotlessFactory.class)
                .write("jenesis.plugins.properties", "spotless+format=build.jenesis.plugin.spotless\n")
                .write("build.jenesis/plugin-spotless.properties", """
                        json.target=*.json
                        json.steps=jacksonJson,endWithNewline
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/settings.json", "{\"name\":\"example\"}");

        project.make("-Djenesis.format.rewrite=true", "build/::/verify-module-sources/format");

        assertThat(project.root().resolve("sources/example/settings.json"))
                .as("the plugin runs through the build that loads it and rewrites the JSON with Jackson,"
                        + " ending it with a newline")
                .content()
                .isEqualTo("{\n  \"name\": \"example\"\n}\n");
    }
}
