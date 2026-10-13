package build.jenesis.plugin.forbiddenapis.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;
import build.jenesis.plugin.testing.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class ForbiddenApisExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void reports_the_calls_that_the_bundled_and_the_bound_signatures_forbid() throws IOException {
        ExampleProject project = ExampleProject.of(folder, ForbiddenApisFactory.class)
                .write("jenesis.plugins.properties",
                        "forbiddenapis+binary/validate=build.jenesis.plugin.forbiddenapis\n")
                .write("build.jenesis/plugin-forbiddenapis.properties", """
                        bundled=jdk-unsafe
                        signatures=forbidden.txt
                        @signatures=../forbidden.txt
                        """)
                .write("forbidden.txt", """
                        @defaultMessage Name the thread
                        java.lang.Thread#<init>(java.lang.Runnable)
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;
                        public class Example {
                            public Thread start(Runnable task) {
                                System.out.println("start".toUpperCase());
                                return new Thread(task);
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("forbiddenapis.txt"))
                .as("the plugin runs through the build that loads it and checks against what it is handed")
                .content()
                .contains("java.lang.String#toUpperCase() [Uses default locale]")
                .contains("java.lang.Thread#<init>(java.lang.Runnable) [Name the thread]");
    }
}
