package build.jenesis.plugin.checkstyle.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.checkstyle.CheckstyleFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckstyleExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_sources_with_the_configuration_a_project_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, CheckstyleFactory.class)
                .write("jenesis.plugins.properties", "checkstyle+check=build.jenesis.plugin.checkstyle\n")
                .write("build.jenesis/plugin-checkstyle.properties", "@rules=../checkstyle.xml\n")
                .write("checkstyle.xml", """
                        <?xml version="1.0"?>
                        <!DOCTYPE module PUBLIC
                            "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                            "https://checkstyle.org/dtds/configuration_1_3.dtd">
                        <module name="Checker">
                            <property name="severity" value="error"/>
                            <module name="TreeWalker">
                                <module name="TypeName"/>
                            </module>
                        </module>
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/badName.java", """
                        package example;
                        public class badName {
                        }
                        """);

        project.make("+sources/produce/assemble/check");

        assertThat(project.find("checkstyle-report.xml"))
                .as("the plugin runs through the build that loads it and writes what its tool produces")
                .content().contains("badName");
    }
}
