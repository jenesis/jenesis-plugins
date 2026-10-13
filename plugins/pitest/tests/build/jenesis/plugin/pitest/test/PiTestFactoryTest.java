package build.jenesis.plugin.pitest.test;

import module java.base;
import module java.compiler;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Repository;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.pitest.PiTestFactory;
import build.jenesis.step.Dependencies;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PiTestFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_in_the_group_it_is_handed() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", create(Map.of(), "mutation", Map.of(), Map.of()), "project");
        executor.execute("pitest/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("pitest")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames()).containsExactly(
                "mutation/runtime/maven/org.pitest/pitest-command-line/RELEASE",
                "mutation/runtime/maven/org.pitest/pitest-junit5-plugin/RELEASE");
    }

    @Test
    public void hands_pit_every_value_it_takes_reading_a_format_whatever_its_case() throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(project.resolve("app.jar")))) {
            jar.putNextEntry(new JarEntry("app/App.class"));
            jar.write(new byte[] {1, 2, 3});
            jar.closeEntry();
        }
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));
        Path folder = Files.createDirectories(root.resolve("served"));
        Path source = folder.resolve("MutationCoverageReport.java");
        Files.writeString(source, """
                package org.pitest.mutationtest.commandline;
                public class MutationCoverageReport {
                    public static void main(String[] args) {
                    }
                }
                """);
        Path classes = Files.createDirectory(folder.resolve("classes"));
        if (ToolProvider.getSystemJavaCompiler().run(null,
                null,
                null,
                "-d",
                classes.toString(),
                source.toString()) != 0) {
            throw new IllegalStateException("Failed to compile the PIT double");
        }
        Path tool = folder.resolve("pitest.jar");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(tool))) {
            String entry = "org/pitest/mutationtest/commandline/MutationCoverageReport.class";
            stream.putNextEntry(new JarEntry(entry));
            stream.write(Files.readAllBytes(classes.resolve(entry)));
            stream.closeEntry();
        }

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", create(Map.of(
                        "targetClasses", "app.*, lib.*",
                        "targetTests", "app.*",
                        "outputFormats", "xml,Html",
                        "mutators", "STRONGER,REMOVE_CONDITIONALS",
                        "failWhenNoMutations", ""),
                "pitest",
                Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(tool))),
                Map.of("maven", Resolver.identity())), "project");
        executor.execute("pitest/mutate");

        assertThat(root.resolve("pitest").resolve("mutate").resolve("supplement").resolve("command"))
                .as("each value reaches PIT's command line, a format in the spelling PIT takes")
                .content()
                .contains("app.*,lib.*")
                .contains("XML,HTML")
                .contains("STRONGER,REMOVE_CONDITIONALS")
                .contains("--failWhenNoMutations true");
    }

    @Test
    public void refuses_a_value_that_names_no_option_of_the_mutation() {
        assertThatThrownBy(() -> create(Map.of("targetClass", "sample.*"), "pitest", Map.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names targetClass")
                .hasMessageContaining("[targetClasses, targetTests, outputFormats, mutators, failWhenNoMutations]");
    }

    @Test
    public void refuses_an_output_format_or_a_flag_that_pit_does_not_know() {
        assertThatThrownBy(() -> create(Map.of("outputFormats", "XML,PDF"), "pitest", Map.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("the format PDF of outputFormats")
                .hasMessageContaining("[XML, HTML, CSV]");
        assertThatThrownBy(() -> create(Map.of("failWhenNoMutations", "yes"), "pitest", Map.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("the value yes of failWhenNoMutations")
                .hasMessageContaining("expected true, false");
    }

    private static BuildExecutorModule create(Map<String, String> values,
                                              String group,
                                              Map<String, Repository> repositories,
                                              Map<String, Resolver> resolvers) {
        return new PiTestFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                new Semaphore(1),
                group,
                repositories,
                resolvers);
    }

    private BuildExecutor newExecutor() throws IOException {
        return BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
    }
}
