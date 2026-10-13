package build.jenesis.plugin.eclipse.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class EclipseJavaFormatExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void rewrites_the_java_sources_with_the_profile_it_binds() throws IOException {
        ExampleProject project = ExampleProject.of(folder, EclipseJavaFormatFactory.class)
                .write("jenesis.plugins.properties", "eclipse+format=build.jenesis.plugin.eclipse\n")
                .write("build.jenesis/plugin-eclipse.properties", "@profile/eclipse-formatter.xml=../build.jenesis/formatter.xml\n")
                .write("build.jenesis/formatter.xml", """
                        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                        <profiles version="23">
                        <profile kind="CodeFormatterProfile" name="Sample" version="23">
                        <setting id="org.eclipse.jdt.core.formatter.tabulation.char" value="space"/>
                        <setting id="org.eclipse.jdt.core.formatter.tabulation.size" value="3"/>
                        <setting id="org.eclipse.jdt.core.formatter.indentation.size" value="3"/>
                        <setting id="org.eclipse.jdt.core.compiler.source" value="1.8"/>
                        </profile>
                        </profiles>
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                        int value(){return 1;}
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "+sources/produce/assemble/format");

        assertThat(project.root().resolve("sources/example/Example.java"))
                .as("the plugin runs through the build that loads it and rewrites the source it formats")
                .content()
                .isNotEqualTo("""
                        package example;
                        public class Example {
                        int value(){return 1;}
                        }
                        """);
    }
}
