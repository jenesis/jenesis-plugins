package build.jenesis.plugin.mavenplugin.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.harness.ExampleProject;
import build.jenesis.plugin.mavenplugin.MavenPluginFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenPluginExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void writes_the_descriptor_of_the_mojos_a_module_declares_into_its_jar() throws IOException {
        ExampleProject project = ExampleProject.of(folder, MavenPluginFactory.class)
                .write("jenesis.plugins.properties",
                        "mavenplugin+binary/transform=build.jenesis.plugin.mavenplugin\n")
                .write("src/main/build.jenesis/plugin-mavenplugin.properties", "goalprefix=greet\n")
                .write("pom.xml", """
                        <project xmlns="http://maven.apache.org/POM/4.0.0">
                          <modelVersion>4.0.0</modelVersion>
                          <groupId>example</groupId>
                          <artifactId>greet-maven-plugin</artifactId>
                          <version>1.0</version>
                          <packaging>jar</packaging>
                          <properties>
                            <maven.compiler.release>17</maven.compiler.release>
                          </properties>
                          <dependencies>
                            <dependency>
                              <groupId>org.apache.maven</groupId>
                              <artifactId>maven-plugin-api</artifactId>
                              <version>3.9.11</version>
                              <scope>provided</scope>
                            </dependency>
                            <dependency>
                              <groupId>org.apache.maven.plugin-tools</groupId>
                              <artifactId>maven-plugin-annotations</artifactId>
                              <version>3.15.1</version>
                              <scope>provided</scope>
                            </dependency>
                          </dependencies>
                        </project>
                        """)
                .write("src/main/java/example/GreetMojo.java", """
                        package example;

                        import org.apache.maven.plugin.AbstractMojo;
                        import org.apache.maven.plugins.annotations.Mojo;
                        import org.apache.maven.plugins.annotations.Parameter;

                        /**
                         * Greets whoever runs the build.
                         */
                        @Mojo(name = "hello", threadSafe = true)
                        public class GreetMojo extends AbstractMojo {

                            /**
                             * The name to greet.
                             */
                            @Parameter(property = "greet.name", defaultValue = "${user.name}")
                            private String name;

                            @Override
                            public void execute() {
                                getLog().info("Hello, " + name + "!");
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("plugin.xml"))
                .as("the descriptor names the mojo, its parameter and the javadoc of both")
                .content()
                .contains("<goalPrefix>greet</goalPrefix>")
                .contains("<goal>hello</goal>")
                .contains("<implementation>example.GreetMojo</implementation>")
                .contains("<description>Greets whoever runs the build.</description>")
                .contains("<description>The name to greet.</description>")
                .contains("<requiredJavaVersion>17</requiredJavaVersion>")
                .contains("<requiredMavenVersion>3.9.11</requiredMavenVersion>");
        try (JarFile file = new JarFile(project.find("example~greet-maven-plugin~1.0.jar").toFile())) {
            assertThat(file.getEntry("META-INF/maven/plugin.xml"))
                    .as("the descriptor is packaged where Maven looks for it")
                    .isNotNull();
            assertThat(file.getEntry("META-INF/maven/example/greet-maven-plugin/plugin-help.xml"))
                    .as("the help descriptor is packaged beside the plugin's POM")
                    .isNotNull();
        }
    }
}
