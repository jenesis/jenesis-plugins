package build.jenesis.plugin.pitest.test;

import module java.base;
import module java.compiler;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.Repository;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.pitest.PiTestModule;
import build.jenesis.step.Dependencies;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PiTestModuleTest {

    @TempDir
    private Path root, project, tool;

    @Test
    public void requires_step_emits_the_pitest_maven_coordinates() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()), "project");
        executor.execute("pitest/required");

        SequencedProperties requires = requiresOutput();
        assertThat(requires.stringPropertyNames()).containsExactly(
                "pitest/runtime/maven/org.pitest/pitest-command-line/RELEASE",
                "pitest/runtime/maven/org.pitest/pitest-junit5-plugin/RELEASE");
    }

    @Test
    public void requires_step_honours_a_custom_tool() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()).tool("custom"), "project");
        executor.execute("pitest/required");

        SequencedProperties requires = requiresOutput();
        assertThat(requires.stringPropertyNames()).containsExactly(
                "custom/runtime/maven/org.pitest/pitest-command-line/RELEASE",
                "custom/runtime/maven/org.pitest/pitest-junit5-plugin/RELEASE");
    }

    @Test
    public void requires_step_pins_the_junit_platform_launcher_from_the_engine_version() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.setProperty("main/maven/org.junit.platform/junit-platform-engine", "1.11.0");
        versions.store(project.resolve(BuildStep.VERSIONS));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()), "project");
        executor.execute("pitest/required");

        SequencedProperties requires = requiresOutput();
        assertThat(requires.stringPropertyNames()).containsExactly(
                "pitest/runtime/maven/org.pitest/pitest-command-line/RELEASE",
                "pitest/runtime/maven/org.pitest/pitest-junit5-plugin/RELEASE",
                "pitest/classpath/maven/org.junit.platform/junit-platform-launcher/1.11.0");
    }

    @Test
    public void requires_step_takes_the_launcher_from_the_engine_the_tests_resolved_where_no_pin_names_it() throws IOException {
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.junit.platform/junit-platform-engine/6.1.3", "engine.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()), "project");
        executor.execute("pitest/required");

        assertThat(requiresOutput().stringPropertyNames())
                .as("a test module that pins no JUnit Platform still hands PIT the launcher of the engine it runs")
                .contains("pitest/classpath/maven/org.junit.platform/junit-platform-launcher/6.1.3");
    }

    @Test
    public void requires_step_pins_the_launcher_again_when_the_engine_version_changes() throws IOException {
        for (String version : List.of("1.11.0", "1.12.0")) {
            SequencedProperties versions = new SequencedProperties();
            versions.setProperty("main/maven/org.junit.platform/junit-platform-engine", version);
            versions.store(project.resolve(BuildStep.VERSIONS));
            BuildExecutor executor = newExecutor();
            executor.addSource("project", project);
            executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()), "project");
            executor.execute("pitest/required");
        }
        assertThat(requiresOutput().stringPropertyNames())
                .as("the launcher follows the engine it runs")
                .contains("pitest/classpath/maven/org.junit.platform/junit-platform-launcher/1.12.0");
    }

    @Test
    public void requires_step_pins_the_launcher_from_commons_and_trims_at_the_first_space() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.setProperty("main/maven/org.junit.platform/junit-platform-commons", "1.11.0 SHA-256/abc");
        versions.store(project.resolve(BuildStep.VERSIONS));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("pitest", new PiTestModule(Map.of(), Map.of()), "project");
        executor.execute("pitest/required");

        SequencedProperties requires = requiresOutput();
        assertThat(requires.stringPropertyNames()).containsExactly(
                "pitest/runtime/maven/org.pitest/pitest-command-line/RELEASE",
                "pitest/runtime/maven/org.pitest/pitest-junit5-plugin/RELEASE",
                "pitest/classpath/maven/org.junit.platform/junit-platform-launcher/1.11.0");
    }

    @Test
    public void mutate_step_rejects_a_jar_entry_that_escapes_the_extraction_root() throws IOException {
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.evil/evil/1.0", "evil.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.evil/evil", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(project.resolve("evil.jar")))) {
            jar.putNextEntry(new JarEntry("../escape.class"));
            jar.write(new byte[] {1, 2, 3});
            jar.closeEntry();
        }

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", files()), Map.of("maven", Resolver.identity())),
                "project");

        assertThatThrownBy(() -> executor.execute("pitest/mutate"))
                .hasStackTraceContaining("Resolved path escapes");
    }

    @Test
    public void mutate_step_extracts_the_first_party_jar_and_leaves_a_library_on_the_class_path()
            throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class");
        jar(project.resolve("library.jar"), "library/Library.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.setProperty("main/runtime/maven/org.example/library/1.0", "library.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.setProperty("vertex/main/runtime/maven/org.example/library", "1.0\t\tfalse\tfalse");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity())),
                "project");
        executor.execute("pitest/mutate");

        Path mutate = root.resolve("pitest").resolve("mutate").resolve("supplement");
        assertThat(mutate.resolve("code").resolve("app").resolve("App.class"))
                .as("the code under test reaches the mutable code path as its own classes")
                .exists();
        assertThat(mutate.resolve("code").resolve("library"))
                .as("a library is never mutated, whatever its jar is called")
                .doesNotExist();
        assertThat(Files.readString(mutate.resolve("command")))
                .as("a library stays on the class path so the mutated code still resolves")
                .contains(project.resolve("library.jar").toString())
                .as("the jar of the code under test follows its classes, so that its resources resolve")
                .contains(project.resolve("app.jar").toString());
    }

    @Test
    public void mutate_step_keeps_the_classes_of_the_tests_off_the_mutable_code_path() throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("app/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity())),
                "project");
        executor.execute("pitest/mutate");

        Path mutate = root.resolve("pitest").resolve("mutate").resolve("supplement");
        assertThat(mutate.resolve("code").resolve("app").resolve("AppTest.class"))
                .as("a test is never mutated, even in the package of the code it tests")
                .doesNotExist();
        assertThat(mutate.resolve("tests").resolve("app").resolve("AppTest.class"))
                .as("the tests reach PIT beside the code they test")
                .exists();
        String command = Files.readString(mutate.resolve("command"));
        assertThat(command.substring(command.indexOf("--classPath"), command.indexOf("--sourceDirs")))
                .as("the tests stay on the class path")
                .contains("supplement" + File.separator + "code")
                .contains("supplement" + File.separator + "tests");
        assertThat(command.substring(command.indexOf("--mutableCodePaths"), command.indexOf("--timestampedReports")))
                .as("only the code under test is mutable")
                .contains("supplement" + File.separator + "code")
                .doesNotContain("supplement" + File.separator + "tests");
    }

    @Test
    public void mutate_step_selects_the_packages_of_the_code_under_test_where_no_classes_are_named()
            throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class", "app/internal/Helper.class", "Main.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity())),
                "project");
        executor.execute("pitest/mutate");

        assertThat(Files.readString(root.resolve("pitest").resolve("mutate").resolve("supplement").resolve("command")))
                .as("PIT refuses *, which would select PIT itself, so the default names what the tests test")
                .contains("Main,app.*,app.internal.*");
    }

    @Test
    public void mutate_step_fails_when_mutations_are_required_and_the_tests_require_no_module_of_the_project()
            throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity()))
                        .failWhenNoMutations(true),
                "project");

        assertThatThrownBy(() -> executor.execute("pitest/mutate"))
                .hasStackTraceContaining("PIT finds no class to mutate under failWhenNoMutations");
    }

    @Test
    public void mutate_step_hands_pit_the_typed_selection_of_classes_tests_formats_and_mutators()
            throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity()))
                        .targetClasses(List.of("app.*", "lib.*"))
                        .targetTests(List.of("apptest.*"))
                        .outputFormats(List.of(PiTestModule.OutputFormat.XML, PiTestModule.OutputFormat.HTML))
                        .mutators(List.of("STRONGER", "REMOVE_CONDITIONALS"))
                        .failWhenNoMutations(true),
                "project");
        executor.execute("pitest/mutate");

        assertThat(Files.readString(root.resolve("pitest").resolve("mutate").resolve("supplement").resolve("command")))
                .contains("app.*,lib.*")
                .contains("XML,HTML")
                .contains("STRONGER,REMOVE_CONDITIONALS")
                .contains("--failWhenNoMutations");
    }

    @ParameterizedTest
    @ValueSource(strings = {"apptest.AppTes?", "**.AppTest", "~apptest\\.App.*", "apptest.*"})
    public void mutate_step_reads_the_target_tests_as_pit_reads_a_glob(String glob) throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity()))
                        .targetTests(List.of(glob)),
                "project");
        executor.execute("pitest/mutate");

        assertThat(root.resolve("pitest").resolve("mutate").resolve("supplement").resolve("command"))
                .as("a glob PIT selects the tests by also starts PIT")
                .exists();
    }

    @Test
    public void mutate_step_runs_nothing_when_no_class_matches_the_target_tests() throws IOException {
        Files.createDirectory(project.resolve(BuildStep.SOURCES));
        test("apptest/AppTest.class");
        jar(project.resolve("app.jar"), "app/App.class");
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/runtime/maven/org.example/app/1.0", "app.jar");
        index.store(project.resolve(BuildStep.DEPENDENCIES));
        SequencedProperties graph = new SequencedProperties();
        graph.setProperty("vertex/main/runtime/maven/org.example/app", "1.0\t\tfalse\ttrue");
        graph.store(project.resolve(Dependencies.GRAPH));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "pitest",
                new PiTestModule(Map.of("maven", serving(cli())), Map.of("maven", Resolver.identity()))
                        .targetTests(List.of("other.*")),
                "project");
        executor.execute("pitest/mutate");

        assertThat(root.resolve("pitest").resolve("mutate").resolve("supplement").resolve("command"))
                .as("PIT is not started when the target tests select no class")
                .doesNotExist();
    }

    @Test
    public void refuses_a_selection_of_classes_or_tests_without_a_glob_or_with_a_comma_in_one() {
        PiTestModule module = new PiTestModule(Map.of(), Map.of());
        assertThatThrownBy(() -> module.targetClasses(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one glob");
        assertThatThrownBy(() -> module.targetTests(List.of("a.*,b.*")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'a.*,b.*'");
    }

    @Test
    public void refuses_no_output_format_and_a_mutator_that_is_no_single_name() {
        PiTestModule module = new PiTestModule(Map.of(), Map.of());
        assertThatThrownBy(() -> module.outputFormats(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("[XML, HTML, CSV]");
        assertThatThrownBy(() -> module.mutators(List.of(" ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("such as DEFAULTS");
    }

    private static void jar(Path jar, String... entries) throws IOException {
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String entry : entries) {
                stream.putNextEntry(new JarEntry(entry));
                stream.write(new byte[] {1, 2, 3});
                stream.closeEntry();
            }
        }
    }

    private void test(String entry) throws IOException {
        Path file = project.resolve(BuildStep.CLASSES).resolve(entry);
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[] {1, 2, 3});
    }

    private Path cli() throws IOException {
        Path source = tool.resolve("MutationCoverageReport.java");
        Files.writeString(source, """
                package org.pitest.mutationtest.commandline;
                public class MutationCoverageReport {
                    public static void main(String[] args) {
                    }
                }
                """);
        Path classes = Files.createDirectory(tool.resolve("classes"));
        if (ToolProvider.getSystemJavaCompiler().run(null,
                null,
                null,
                "-d",
                classes.toString(),
                source.toString()) != 0) {
            throw new IllegalStateException("Failed to compile the PIT double");
        }
        Path jar = tool.resolve("pitest.jar");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jar))) {
            stream.putNextEntry(new JarEntry("org/pitest/mutationtest/commandline/MutationCoverageReport.class"));
            stream.write(Files.readAllBytes(classes.resolve("org")
                    .resolve("pitest")
                    .resolve("mutationtest")
                    .resolve("commandline")
                    .resolve("MutationCoverageReport.class")));
            stream.closeEntry();
        }
        return jar;
    }

    private static Repository serving(Path jar) {
        return (_, _, _) -> Optional.of(RepositoryItem.ofFile(jar));
    }

    private Repository files() {
        return (_, coordinate, _) -> {
            Path file = Files.write(
                    Files.createDirectories(root.resolve("served")).resolve(coordinate.replace('/', '-') + ".jar"),
                    coordinate.getBytes(StandardCharsets.UTF_8));
            return Optional.of(RepositoryItem.ofFile(file));
        };
    }

    private SequencedProperties requiresOutput() throws IOException {
        Path requiredOutput = root.resolve("pitest").resolve("required").resolve("output");
        return SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
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
