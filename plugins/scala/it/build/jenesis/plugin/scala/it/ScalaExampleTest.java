package build.jenesis.plugin.scala.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.scala.ScalaFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void compiles_and_documents_the_scala_sources_of_a_module_that_java_calls() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ScalaFactory.class)
                .write("sources/module-info.java", "module example { exports example; }\n")
                .write("sources/example/Greeting.scala", """
                        package example
                        /** Greets in Scala. */
                        class Greeting:
                          def text(): String = "Hello from Scala"
                        """)
                .write("sources/example/Caller.java", """
                        package example;
                        public class Caller {
                            public String call() {
                                return new Greeting().text();
                            }
                        }
                        """);

        project.make("-Djenesis.project.documentation=true", "build");

        List<Set<String>> jars = new ArrayList<>();
        try (Stream<Path> files = Files.walk(project.root().resolve("target"))) {
            for (Path file : files.filter(candidate -> candidate.getFileName().toString().endsWith(".jar")
                    && candidate.getParent().endsWith(Path.of("jar", "output", "artifacts"))).toList()) {
                try (JarFile jar = new JarFile(file.toFile())) {
                    jars.add(jar.stream().map(JarEntry::getName).collect(Collectors.toSet()));
                }
            }
        }
        assertThat(jars)
                .as("the module's jar holds the class the plugin compiled beside the Java class javac compiled"
                        + " against it")
                .anyMatch(entries -> entries.containsAll(Set.of("module-info.class",
                        "example/Greeting.class",
                        "example/Caller.class")));
        assertThat(project.find("Greeting.html"))
                .as("Scaladoc rendered the documentation of the Scala class")
                .isNotEmptyFile();
    }
}
