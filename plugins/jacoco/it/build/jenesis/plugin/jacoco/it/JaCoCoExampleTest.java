package build.jenesis.plugin.jacoco.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.jacoco.JaCoCoFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class JaCoCoExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_the_coverage_of_the_tests_it_observes() throws IOException {
        ExampleProject project = ExampleProject.of(folder, JaCoCoFactory.class)
                .write("jenesis.plugins.properties", "jacoco+observe=build.jenesis.plugin.jacoco\n")
                .write("build.jenesis/plugin-jacoco.properties", "minimum.line=0.5\n")
                .write("sources/module-info.java", "module example { exports example; }\n")
                .write("sources/example/Calculator.java", """
                        package example;
                        public class Calculator {
                            public int add(int left, int right) {
                                return left + right;
                            }
                            public int subtract(int left, int right) {
                                return left - right;
                            }
                        }
                        """)
                .write("tests/module-info.java", """
                        /**
                         * @jenesis.test example
                         */
                        module example.test {
                            requires example;
                            requires org.junit.jupiter.api;
                        }
                        """)
                .write("tests/example/test/CalculatorTest.java", """
                        package example.test;
                        import example.Calculator;
                        import org.junit.jupiter.api.Test;
                        import static org.junit.jupiter.api.Assertions.assertEquals;
                        class CalculatorTest {
                            @Test
                            void adds() {
                                assertEquals(3, new Calculator().add(1, 2));
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("jacoco.xml"))
                .as("the agent the plugin attaches records the tests' run, and its report reads what it recorded")
                .content()
                .contains("name=\"example/Calculator\"")
                .contains("name=\"add\"");
    }
}
