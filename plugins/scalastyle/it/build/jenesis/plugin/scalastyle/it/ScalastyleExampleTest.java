package build.jenesis.plugin.scalastyle.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.scalastyle.ScalastyleFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalastyleExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_scala_sources_with_the_configuration_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ScalastyleFactory.class)
                .write("jenesis.plugins.properties", "scalastyle+check=build.jenesis.plugin.scalastyle\n")
                .write("build.jenesis/plugin-scalastyle.properties", "@rules=../scalastyle-config.xml\n")
                .write("scalastyle-config.xml", """
                        <scalastyle>
                            <name>test</name>
                            <check level="error" class="org.scalastyle.file.FileLineLengthChecker" enabled="true">
                                <parameters>
                                    <parameter name="maxLineLength">10</parameter>
                                </parameters>
                            </check>
                        </scalastyle>
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/sample/Sample.scala", """
                        package sample
                        class Sample {
                          def greetTheEntireWorld(): Int = 42
                        }
                        """);

        project.make("+sources/produce/assemble/check");

        assertThat(project.find("scalastyle-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .isNotEmptyFile();
    }
}
