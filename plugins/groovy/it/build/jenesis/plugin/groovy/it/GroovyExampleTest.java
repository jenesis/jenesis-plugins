package build.jenesis.plugin.groovy.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.groovy.GroovyFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void compiles_and_documents_the_groovy_sources_of_a_module_beside_its_java_sources() throws IOException {
        ExampleProject project = ExampleProject.of(folder, GroovyFactory.class)
                .write("sources/module-info.java", "module example { exports example; }\n")
                .write("sources/example/Greeter.java", """
                        package example;
                        /** Says hello. */
                        public class Greeter {
                            /** @return the greeting */
                            public String hello() {
                                return "Hello";
                            }
                        }
                        """)
                .write("sources/example/Welcome.groovy", """
                        package example
                        /** Welcomes with the greeting of the Java class. */
                        class Welcome {
                            String welcome() { new Greeter().hello() + " from Groovy" }
                        }
                        """);

        project.make("-Djenesis.project.documentation=true", "build");

        try (JarFile jar = new JarFile(project.find("example-0-SNAPSHOT.jar").toFile())) {
            assertThat(jar.getEntry("example/Welcome.class"))
                    .as("the plugin compiles the Groovy class after javac into the module's jar")
                    .isNotNull();
            assertThat(jar.getEntry("example/Greeter.class")).isNotNull();
        }
        assertThat(project.find("Welcome.html"))
                .as("Groovydoc documents the Groovy class")
                .content()
                .contains("Welcomes with the greeting of the Java class.");
        try (Stream<Path> files = Files.walk(project.root().resolve("target"))) {
            assertThat(files.filter(file -> file.endsWith(Path.of("javadoc", "groovy", "example", "Welcome.html"))))
                    .as("beside Java the documentation of Groovy has a folder of its own, named after the plugin")
                    .isNotEmpty();
        }
        assertThat(project.find("Greeter.html")).isNotEmptyFile();
    }
}
