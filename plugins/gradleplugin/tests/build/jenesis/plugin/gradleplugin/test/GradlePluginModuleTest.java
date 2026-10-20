package build.jenesis.plugin.gradleplugin.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.plugin.gradleplugin.GradlePluginFactory;
import build.jenesis.plugin.gradleplugin.GradlePluginModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GradlePluginModuleTest {

    @TempDir
    private Path root, staged;

    @Test
    public void stages_a_marker_that_depends_on_the_jar_declaring_the_plugin_with_the_metadata_of_its_pom()
            throws IOException {
        stage("example", "greet-gradle-plugin", "", Map.of(
                "META-INF/gradle-plugins/com.example.greet.properties", "implementation-class=example.GreetPlugin\n",
                "example/GreetPlugin.class", ""));

        execute();

        Path marker = root.resolve("gradleplugin").resolve(GradlePluginModule.MARKERS)
                .resolve("output/maven/com/example/greet/com.example.greet.gradle.plugin/1.0")
                .resolve("com.example.greet.gradle.plugin-1.0.pom");
        assertThat(marker)
                .as("the marker is staged where Gradle looks up the plugin ID, and names the jar")
                .content()
                .contains("<groupId>com.example.greet</groupId>")
                .contains("<artifactId>com.example.greet.gradle.plugin</artifactId>")
                .contains("<packaging>pom</packaging>")
                .contains("<artifactId>greet-gradle-plugin</artifactId>")
                .as("the marker carries the metadata a repository such as Maven Central requires")
                .contains("<name>Greeting</name>")
                .contains("<url>https://example.com</url>")
                .contains("<name>Apache-2.0</name>");
    }

    @Test
    public void leaves_alone_what_is_staged_beside_the_jar_and_in_other_trees() throws IOException {
        stage("example", "greet-gradle-plugin", "-sources", Map.of(
                "META-INF/gradle-plugins/com.example.greet.properties", "implementation-class=example.GreetPlugin\n"));
        Files.createDirectories(staged.resolve("modular"));

        execute();

        assertThat(root.resolve("gradleplugin").resolve(GradlePluginModule.MARKERS).resolve("output/maven"))
                .as("only the jar of an artifact itself declares its plugins")
                .doesNotExist();
    }

    @Test
    public void refuses_a_descriptor_naming_a_class_its_jar_does_not_hold() throws IOException {
        stage("example", "greet-gradle-plugin", "", Map.of(
                "META-INF/gradle-plugins/com.example.greet.properties", "implementation-class=example.Missing\n"));

        assertThatThrownBy(this::execute)
                .hasStackTraceContaining("The Gradle plugin com.example.greet of example:greet-gradle-plugin:1.0 names"
                        + " the implementation-class example.Missing, which its jar does not hold");
    }

    @Test
    public void refuses_a_plugin_two_jars_declare() throws IOException {
        Map<String, String> declared = Map.of(
                "META-INF/gradle-plugins/com.example.greet.properties", "implementation-class=example.GreetPlugin\n",
                "example/GreetPlugin.class", "");
        stage("example", "first", "", declared);
        stage("example", "second", "", declared);

        assertThatThrownBy(this::execute)
                .hasStackTraceContaining("The Gradle plugin com.example.greet of example:second:1.0 is declared by"
                        + " example:first:1.0 as well");
    }

    @Test
    public void refuses_a_plugin_in_a_namespace_gradle_reserves() throws IOException {
        stage("example", "greet-gradle-plugin", "", Map.of(
                "META-INF/gradle-plugins/org.gradle.greet.properties", "implementation-class=example.GreetPlugin\n",
                "example/GreetPlugin.class", ""));

        assertThatThrownBy(this::execute)
                .hasStackTraceContaining("takes a namespace Gradle reserves for its own plugins");
    }

    @Test
    public void takes_no_values() {
        assertThatThrownBy(() -> new GradlePluginFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("id", "com.example.greet")),
                Permits.of(1),
                "gradleplugin",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names id, but takes no values");
    }

    private void stage(String group, String artifact, String classifier, Map<String, String> entries)
            throws IOException {
        Path folder = Files.createDirectories(staged.resolve("maven").resolve(group).resolve(artifact).resolve("1.0"));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(folder.resolve(artifact + "-1.0"
                + classifier + ".jar")))) {
            for (Map.Entry<String, String> entry : new TreeMap<>(entries).entrySet()) {
                jar.putNextEntry(new JarEntry(entry.getKey()));
                jar.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                jar.closeEntry();
            }
        }
        Files.writeString(folder.resolve(artifact + "-1.0.pom"), """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>%s</groupId>
                  <artifactId>%s</artifactId>
                  <version>1.0</version>
                  <name>Greeting</name>
                  <url>https://example.com</url>
                  <licenses>
                    <license>
                      <name>Apache-2.0</name>
                    </license>
                  </licenses>
                </project>
                """.formatted(group, artifact));
    }

    private void execute() throws IOException {
        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        if (Files.isDirectory(staged.resolve("maven"))) {
            executor.addSource("maven", staged.resolve("maven"));
        }
        if (Files.isDirectory(staged.resolve("modular"))) {
            executor.addSource("modular", staged.resolve("modular"));
        }
        executor.addModule("gradleplugin", new GradlePluginModule(), Stream.of("maven", "modular")
                .filter(tree -> Files.isDirectory(staged.resolve(tree)))
                .toList()
                .toArray(String[]::new));
        executor.execute();
    }
}
