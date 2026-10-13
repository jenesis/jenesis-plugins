package build.jenesis.plugin.scalafmt.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.scalafmt.ScalafmtFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalafmtExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_on_the_scala_sources_as_the_configuration_a_project_binds_says() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ScalafmtFactory.class)
                .write("jenesis.plugins.properties", "scalafmt+check=build.jenesis.plugin.scalafmt@check\n")
                .write("build.jenesis/plugin-scalafmt.properties", "@rules=../.scalafmt.conf\n")
                .write(".scalafmt.conf", """
                        version = "3.8.3"
                        runner.dialect = scala213
                        """)
                .write("sources/module-info.java", """
                        /**
                         * @jenesis.pin scalafmt/maven/org.scalameta/scalafmt-cli_2.13 3.8.3
                         */
                        module example {}
                        """)
                .write("sources/sample/Sample.scala", """
                        package sample
                        class Sample {   def  f( ) :Int=42 }
                        """);

        String output = project.make("+sources/produce/assemble/check");

        assertThat(output)
                .as("the plugin runs through the build that loads it and reports what scalafmt finds")
                .contains("scalafmt found 1 finding");
    }
}
