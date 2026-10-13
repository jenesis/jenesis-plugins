package build.jenesis.plugin.pitest.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.pitest.PiTestFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class PiTestExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void mutates_the_code_a_test_module_tests_and_reports_the_mutants_its_tests_kill() throws IOException {
        ExampleProject project = ExampleProject.of(folder, PiTestFactory.class)
                .write("jenesis.plugins.properties", "pitest+observed=build.jenesis.plugin.pitest\n")
                .write("build.jenesis/plugin-pitest.properties", """
                        targetClasses=example.Calculator
                        targetTests=example.test.*
                        """)
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
                         * @jenesis.pin org.junit.platform/junit-platform-engine 6.1.3
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
                .contains("status='KILLED'");
    }
}
