package build.jenesis.plugin.openrewrite.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.harness.ExampleProject;
import build.jenesis.plugin.openrewrite.OpenRewriteFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenRewriteExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void applies_the_recipe_the_project_declares_to_its_java_sources() throws IOException {
        ExampleProject project = ExampleProject.of(folder, OpenRewriteFactory.class)
                .write("jenesis.plugins.properties", "openrewrite+format=build.jenesis.plugin.openrewrite\n")
                .write("build.jenesis/plugin-openrewrite.properties", """
                        recipes=example.Cleanup
                        @rules=../rewrite.yml
                        """)
                .write("rewrite.yml", """
                        type: specs.openrewrite.org/v1beta/recipe
                        name: example.Cleanup
                        displayName: Example cleanup
                        recipeList:
                          - org.openrewrite.java.RemoveUnusedImports
                        """)
                .write("sources/module-info.java", "module example {}\n")
                .write("sources/example/Example.java", """
                        package example;

                        import java.util.List;
                        import java.util.Map;

                        public class Example {
                            List<String> names() {
                                return List.of("Ada");
                            }
                        }
                        """);

        project.make("-Djenesis.format.rewrite=true", "build/::/verify-module-sources/format");

        assertThat(project.root().resolve("sources/example/Example.java"))
                .as("the plugin runs through the build that loads it and applies the recipe the project declares")
                .content()
                .contains("import java.util.List;")
                .doesNotContain("import java.util.Map;");
    }
}
