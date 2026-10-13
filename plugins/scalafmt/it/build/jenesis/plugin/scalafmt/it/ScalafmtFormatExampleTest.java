package build.jenesis.plugin.scalafmt.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.scalafmt.ScalafmtFormatFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalafmtFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_scala_sources_as_the_configuration_a_project_binds_says() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ScalafmtFormatFactory.class)
                .write("jenesis.plugins.properties", "scalafmt+format=build.jenesis.plugin.scalafmt@format\n")
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

        project.make("-Djenesis.format.rewrite=true", "+sources/produce/assemble/format");

        assertThat(project.root().resolve("sources/sample/Sample.scala"))
                .as("the plugin runs through the build that loads it and rewrites the source it formats")
                .content()
                .isNotEqualTo("""
                        package sample
                        class Sample {   def  f( ) :Int=42 }
                        """);
    }
}
