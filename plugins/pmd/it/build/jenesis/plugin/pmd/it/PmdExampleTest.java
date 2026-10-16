package build.jenesis.plugin.pmd.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.pmd.PmdFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class PmdExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_a_violation_of_the_rule_set_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, PmdFactory.class)
                .write("jenesis.plugins.properties", "pmd+check=build.jenesis.plugin.pmd\n")
                .write("build.jenesis/plugin-pmd.properties", "@rules=../pmd.xml\n")
                .write("pmd.xml", """
                        <?xml version="1.0"?>
                        <ruleset name="example" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
                            <rule ref="category/java/bestpractices.xml/SystemPrintln"/>
                        </ruleset>
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                            public void run() {
                                System.out.println("hello");
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("pmd-report.xml"))
                .as("the plugin runs PMD over the sources of the example with the rules it binds")
                .content()
                .contains("SystemPrintln");
    }
}
