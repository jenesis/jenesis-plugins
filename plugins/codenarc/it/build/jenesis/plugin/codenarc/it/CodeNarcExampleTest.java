package build.jenesis.plugin.codenarc.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.codenarc.CodeNarcFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class CodeNarcExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_groovy_sources_with_the_rule_set_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, CodeNarcFactory.class)
                .write("jenesis.plugins.properties", "codenarc+check=build.jenesis.plugin.codenarc\n")
                .write("build.jenesis/plugin-codenarc.properties", "@rules=../codenarc.xml\n")
                .write("codenarc.xml", """
                        <ruleset xmlns="http://codenarc.org/ruleset/1.0">
                            <rule class="org.codenarc.rule.basic.EmptyIfStatementRule"/>
                        </ruleset>
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/sample/Sample.groovy", """
                        package sample
                        class Sample {
                            void run() {
                                if (true) { }
                            }
                        }
                        """);

        project.make("+sources/produce/assemble/check");

        assertThat(project.find("codenarc-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
