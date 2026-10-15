package build.jenesis.plugin.forbiddenapis.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.forbiddenapis.ForbiddenApisFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ForbiddenApisFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("forbiddenapis",
                create(Map.of("bundled", "jdk-unsafe"), "quality", Map.of(), Map.of()),
                "project");
        executor.execute("forbiddenapis/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("forbiddenapis")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("quality:tool/runtime/maven/de.thetaphi/forbiddenapis/RELEASE");
    }

    @Test
    public void hands_forbiddenapis_every_value_it_takes() throws IOException {
        Files.writeString(project.resolve("forbidden.txt"), "java.util.Vector\n");
        Path tool = classesAndTool();

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("forbiddenapis", create(Map.of(
                        "bundled", "jdk-unsafe, jdk-system-out",
                        "signatures", "forbidden.txt",
                        "suppress", "**.SuppressForbidden",
                        "includes", "sample.**",
                        "excludes", "sample.Generated",
                        "warn", "java.lang.String#toUpperCase()",
                        "allowmissingclasses", "true",
                        "ignoresignaturesofmissingclasses", ""),
                "forbiddenapis",
                Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(tool))),
                Map.of("maven", Resolver.identity())), "project");
        executor.execute("forbiddenapis/check");

        assertThat(root.resolve("forbiddenapis").resolve("check").resolve("supplement").resolve("command"))
                .as("each value reaches the command line, a bundled signature of the JDK at the release compiled for")
                .content()
                .contains("--bundledsignatures jdk-unsafe-17 --bundledsignatures jdk-system-out")
                .contains("--signaturesfile " + project.resolve("forbidden.txt"))
                .contains("SuppressForbidden")
                .contains("--includes")
                .contains("--excludes sample.Generated")
                .contains("--signatureswithseveritywarn")
                .contains("--allowmissingclasses --ignoresignaturesofmissingclasses");
    }

    @Test
    public void fails_where_a_signatures_file_it_names_is_not_bound() throws IOException {
        Path tool = classesAndTool();

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("forbiddenapis", create(Map.of("signatures", "rules/forbidden.txt"),
                "forbiddenapis",
                Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(tool))),
                Map.of("maven", Resolver.identity())), "project");

        assertThatThrownBy(() -> executor.execute("forbiddenapis/check"))
                .hasStackTraceContaining("bind it as @<input>/rules/forbidden.txt=<file>");
    }

    @Test
    public void refuses_to_check_against_no_signatures() {
        assertThatThrownBy(() -> create(Map.of("suppress", "**.SuppressForbidden"),
                "forbiddenapis",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names no signatures");
    }

    @Test
    public void refuses_a_signatures_file_that_is_no_name_below_an_input() {
        assertThatThrownBy(() -> create(Map.of("signatures", "../outside.txt"), "forbiddenapis", Map.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name it relative to the input it is bound into");
    }

    @Test
    public void refuses_a_flag_that_is_neither_true_nor_false() {
        assertThatThrownBy(() -> create(Map.of("bundled", "jdk-unsafe", "allowmissingclasses", "yes"),
                "forbiddenapis",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allowmissingclasses");
    }

    @Test
    public void refuses_a_value_it_does_not_know_naming_those_it_takes() {
        assertThatThrownBy(() -> create(Map.of("bundled", "jdk-unsafe", "target", "17"),
                "forbiddenapis",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names target, but takes allowmissingclasses, bundled");
    }

    private Path classesAndTool() throws IOException {
        Path classes = Files.createDirectories(project.resolve(BuildStep.CLASSES).resolve("sample"));
        Files.write(classes.resolve("Sample.class"),
                new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 61});
        Path tool = Files.createDirectories(root.resolve("served")).resolve("forbiddenapis.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(tool))) {
            jar.putNextEntry(new JarEntry("META-INF/"));
            jar.closeEntry();
        }
        return tool;
    }

    private static BuildExecutorModule create(Map<String, String> values,
                                              String group,
                                              Map<String, Repository> repositories,
                                              Map<String, Resolver> resolvers) {
        return new ForbiddenApisFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
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

    @Test
    public void wires_nothing_when_its_setting_says_ignore() throws IOException {
        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        executor.addSource("project", project);
        executor.addModule("forbiddenapis", new ForbiddenApisFactory().create(new Environment(Map.of("validator.forbiddenapis", "ignore")),
                new LinkedHashMap<>(Map.of("bundled", "jdk-unsafe")),
                Permits.of(1),
                "forbiddenapis",
                Map.of(),
                Map.of()), "project");

        assertThat(executor.execute().sequencedKeySet()).noneMatch(key -> key.startsWith("forbiddenapis/"));
    }
}
