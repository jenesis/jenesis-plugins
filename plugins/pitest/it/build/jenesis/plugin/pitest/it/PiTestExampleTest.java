package build.jenesis.plugin.pitest.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.pitest.PiTestFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class PiTestExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void mutates_the_code_a_test_module_tests_but_not_the_tests_and_reports_the_mutants_they_kill()
            throws IOException {
        ExampleProject project = ExampleProject.of(folder, PiTestFactory.class)
                .write("jenesis.plugins.properties", "pitest+observed=build.jenesis.plugin.pitest\n")
                .write("build.jenesis/plugin-pitest.properties", "outputFormats=XML\n")
                .write("sources/module-info.java", "module example { exports example; }\n")
                .write("sources/example/Calculator.java", """
                        package example;
                        public class Calculator {
                            public int add(int left, int right) {
                                return left + right;
                            }
                        }
                        """)
                .write("tests/module-info.java", """
                        /**
                         * @jenesis.test example
                         */
                        module example.test {
                            requires example;
                            requires org.junit.jupiter;
                        }
                        """)
                .write("tests/example/test/CalculatorTest.java", """
                        package example.test;
                        import example.Calculator;
                        import org.junit.jupiter.api.Test;
                        import static org.junit.jupiter.api.Assertions.assertEquals;
                        public class CalculatorTest {
                            @Test
                            public void adds() {
                                assertEquals(5, new Calculator().add(2, 3));
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("mutations.xml"))
                .as("the plugin runs PIT over the tested code through the build that loads it")
                .content()
                .contains("<mutatedClass>example.Calculator</mutatedClass>")
                .contains("status='KILLED'")
                .doesNotContain("<mutatedClass>example.test.");
    }

    @Test
    public void runs_the_tests_against_the_mutants_with_the_resources_of_the_tested_code() throws IOException {
        ExampleProject project = ExampleProject.of(folder, PiTestFactory.class)
                .write("jenesis.plugins.properties", "pitest+observed=build.jenesis.plugin.pitest\n")
                .write("build.jenesis/plugin-pitest.properties", "outputFormats=XML\n")
                .write("sources/module-info.java", "module example { exports example; opens example; }\n")
                .write("sources/example/factor.txt", "3\n")
                .write("sources/example/Multiplier.java", """
                        package example;
                        public class Multiplier {
                            public int times(int value) throws java.io.IOException {
                                try (var in = Multiplier.class.getResourceAsStream("factor.txt")) {
                                    return value * Integer.parseInt(new String(in.readAllBytes()).trim());
                                }
                            }
                        }
                        """)
                .write("tests/module-info.java", """
                        /**
                         * @jenesis.test example
                         */
                        module example.test {
                            requires example;
                            requires org.junit.jupiter;
                        }
                        """)
                .write("tests/example/test/MultiplierTest.java", """
                        package example.test;
                        import example.Multiplier;
                        import org.junit.jupiter.api.Test;
                        import static org.junit.jupiter.api.Assertions.assertEquals;
                        public class MultiplierTest {
                            @Test
                            public void multiplies() throws Exception {
                                assertEquals(6, new Multiplier().times(2));
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("mutations.xml"))
                .as("the tests pass without mutation, as they find the resources of the code they test")
                .content()
                .contains("<mutatedClass>example.Multiplier</mutatedClass>")
                .contains("status='KILLED'");
    }
}
