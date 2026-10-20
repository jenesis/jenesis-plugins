package build.jenesis.plugin.gradleplugin.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.gradleplugin.GradlePluginFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class GradlePluginExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void stages_the_marker_of_the_gradle_plugin_a_module_declares() throws IOException {
        ExampleProject project = ExampleProject.of(folder, GradlePluginFactory.class)
                .write("jenesis.plugins.properties",
                        "gradleplugin+stage/transform=build.jenesis.plugin.gradleplugin\n")
                .write("build.jenesis/plugin-gradleplugin.properties", "")
                .write("pom.xml", """
                        <project xmlns="http://maven.apache.org/POM/4.0.0">
                          <modelVersion>4.0.0</modelVersion>
                          <groupId>example</groupId>
                          <artifactId>greet-gradle-plugin</artifactId>
                          <version>1.0</version>
                          <name>Greeting</name>
                        </project>
                        """)
                .write("src/main/java/example/GreetPlugin.java", """
                        package example;

                        public class GreetPlugin {
                        }
                        """)
                .write("src/main/resources/META-INF/gradle-plugins/com.example.greet.properties",
                        "implementation-class=example.GreetPlugin\n");

        project.make("stage");

        assertThat(project.find("com.example.greet.gradle.plugin-1.0.pom"))
                .as("the marker joins the staged Maven repository and names the plugin's jar")
                .content()
                .contains("<artifactId>com.example.greet.gradle.plugin</artifactId>")
                .contains("<artifactId>greet-gradle-plugin</artifactId>")
                .contains("<name>Greeting</name>");
    }
}
