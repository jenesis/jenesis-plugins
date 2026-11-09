package build.jenesis.plugin.clojure.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.clojure.ClojureFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ClojureExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void compiles_the_clojure_namespaces_of_a_module_beside_its_java_sources() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ClojureFactory.class)
                .write("jenesis.properties", "jenesis.maven.sources=clojure\n")
                .write("pom.xml", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <project xmlns="http://maven.apache.org/POM/4.0.0">
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>example</groupId>
                            <artifactId>example</artifactId>
                            <version>0-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.clojure</groupId>
                                    <artifactId>clojure</artifactId>
                                    <version>1.12.6</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """)
                .write("src/main/java/example/Greeter.java", """
                        package example;

                        public class Greeter {
                            public String hello() {
                                return "Hello";
                            }
                        }
                        """)
                .write("src/main/clojure/example/welcome.clj", """
                        (ns example.welcome
                          (:import [example Greeter]))

                        (defn welcome []
                          (str (.hello (Greeter.)) " from Clojure"))
                        """);

        project.make("build");

        try (JarFile jar = new JarFile(project.find("example~example~0-SNAPSHOT.jar").toFile())) {
            assertThat(jar.getEntry("example/welcome__init.class"))
                    .as("the plugin compiles the namespace after javac, against the Java class, into the module's jar")
                    .isNotNull();
            assertThat(jar.getEntry("example/welcome.clj"))
                    .as("the source ships beside its classes, as a Clojure library's jar holds it")
                    .isNotNull();
            assertThat(jar.getEntry("example/Greeter.class")).isNotNull();
        }
    }
}
