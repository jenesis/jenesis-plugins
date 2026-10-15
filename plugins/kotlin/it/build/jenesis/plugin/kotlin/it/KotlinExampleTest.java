package build.jenesis.plugin.kotlin.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.kotlin.KotlinFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class KotlinExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void compiles_kotlin_beside_the_java_that_uses_it_and_documents_it_with_dokka() throws IOException {
        ExampleProject project = ExampleProject.of(folder, KotlinFactory.class)
                .write("build.jenesis/dokka.properties", "module-name=Example\n")
                .write("sources/module-info.java", """
                        module example {
                            requires kotlin.stdlib;
                            exports example;
                        }
                        """)
                .write("sources/example/Greeting.kt", """
                        package example

                        /** Greets whoever it is handed. */
                        class Greeting {
                            /** The greeting of [name]. */
                            fun greet(name: String): String = "Hello, " + name
                        }
                        """)
                .write("sources/example/Caller.java", """
                        package example;

                        /** Calls the Kotlin greeting. */
                        public class Caller {
                            /** Greets the world. */
                            public String call() {
                                return new Greeting().greet("world");
                            }
                        }
                        """)
                .write("sources/example/greeting.properties", "greeting=Hello\n");

        project.make("-Djenesis.project.documentation=true", "build");

        try (JarFile jar = new JarFile(project.find("example-0-SNAPSHOT.jar").toFile())) {
            assertThat(jar.stream().map(JarEntry::getName))
                    .as("kotlinc compiles the Kotlin class ahead of javac, which compiles the Java class that uses it")
                    .contains("module-info.class", "example/Greeting.class", "example/Caller.class")
                    .as("the build places the resources beside the classes of both compilers, and only once")
                    .containsOnlyOnce("example/greeting.properties");
        }
        Path greet = project.find("greet.html");
        assertThat(greet.toString())
                .as("Dokka documents the module under the name its value gives it")
                .contains("-example");
        assertThat(greet).content().contains("The greeting of");
    }
}
