package build.jenesis.plugin.spotless.test;

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
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.spotless.SpotlessFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpotlessFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_spotless_in_the_tool_group_and_the_tool_of_each_step_in_a_group_of_its_own()
            throws IOException {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("json.target", "*.json");
        values.put("json.steps", "jacksonJson,endWithNewline");
        values.put("java.target", "*.java");
        values.put("java.steps", "googleJavaFormat");
        values.put("java.googleJavaFormat.version", "1.28.0");

        Path root = execute(values);

        assertThat(required(root.resolve("spotless")))
                .as("Spotless resolves in the tool group of the plugin it is handed")
                .containsExactly("spotless:tool/runtime/maven/com.diffplug.spotless/spotless-lib/RELEASE",
                        "spotless:tool/runtime/maven/org.slf4j/slf4j-api/RELEASE");
        assertThat(required(root.resolve("spotless").resolve("jacksonjson")))
                .as("a step's tool resolves in a group named after the step, so it shares no version with another")
                .containsExactly("spotless:jacksonjson/runtime/maven/com.fasterxml.jackson.core/jackson-databind/RELEASE");
        assertThat(required(root.resolve("spotless").resolve("googlejavaformat")))
                .as("the version a set names is the one resolved")
                .containsExactly("spotless:googlejavaformat/runtime/maven/com.google.googlejavaformat"
                        + "/google-java-format/1.28.0");
        assertThat(root.resolve("spotless").resolve("endwithnewline"))
                .as("a step that needs no tool resolves nothing")
                .doesNotExist();
    }

    @Test
    public void refuses_two_versions_of_one_step_across_sets() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("main.target", "main/*.java");
        values.put("main.steps", "googleJavaFormat");
        values.put("main.googleJavaFormat.version", "1.28.0");
        values.put("test.target", "test/*.java");
        values.put("test.steps", "googleJavaFormat");

        assertThatThrownBy(() -> execute(values))
                .hasStackTraceContaining("googleJavaFormat")
                .hasStackTraceContaining("give every set the same version of it");
    }

    @Test
    public void takes_the_options_of_every_step_that_has_one() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("java.target", "*.java");
        values.put("java.steps", "importOrder,leadingTabsToSpaces,replaceRegex,googleJavaFormat,formatAnnotations");
        values.put("java.googleJavaFormat.style", "AOSP");
        values.put("java.googleJavaFormat.reflowLongStrings", "");
        values.put("java.googleJavaFormat.formatJavadoc", "false");
        values.put("java.importOrder.order", "java,javax,,");
        values.put("java.leadingTabsToSpaces.size", "2");
        values.put("java.replaceRegex.regex", "\\s+$");
        values.put("java.replaceRegex.replacement", "");
        values.put("tabs.target", "Makefile");
        values.put("tabs.steps", "leadingSpacesToTabs");

        assertThat(create(values)).isNotNull();
    }

    @Test
    public void refuses_properties_that_name_no_set() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("<set>.target=<glob>")
                .hasMessageContaining("<set>.steps=<step>");
    }

    @Test
    public void refuses_a_key_of_another_shape_naming_those_it_takes() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("indent", "4"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indent")
                .hasMessageContaining("<set>.target, <set>.steps and <set>.<step>.<option>");
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("json.files", "*.json"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("json.files")
                .hasMessageContaining("json.target and json.steps");
    }

    @Test
    public void refuses_a_step_it_does_not_take_naming_those_it_does() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("js.target", "*.js");
        values.put("js.steps", "prettier");

        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prettier")
                .hasMessageContaining("[endWithNewline, flexmark, formatAnnotations, googleJavaFormat, importOrder,"
                        + " jacksonJson, jacksonYaml, leadingSpacesToTabs, leadingTabsToSpaces, palantirJavaFormat,"
                        + " removeUnusedImports, replaceRegex, trimTrailingWhitespace]");
    }

    @Test
    public void refuses_an_option_a_step_does_not_take() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("json.target", "*.json");
        values.put("json.steps", "jacksonJson,endWithNewline");
        values.put("json.endWithNewline.indent", "2");

        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endWithNewline takes no option indent")
                .hasMessageContaining("as it takes none");
        values.remove("json.endWithNewline.indent");
        values.put("json.jacksonJson.indent", "2");
        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jacksonJson takes no option indent")
                .hasMessageContaining("its options are [version]");
    }

    @Test
    public void refuses_a_set_without_files_or_without_steps() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("json.steps", "jacksonJson"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("json.target=<glob>");
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("json.target", "*.json"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("json.steps=<step>");
    }

    @Test
    public void refuses_an_option_of_a_step_the_set_does_not_run() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("misc.target", "*.txt");
        values.put("misc.steps", "trimTrailingWhitespace");
        values.put("misc.leadingTabsToSpaces.size", "2");

        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configures the step leadingTabsToSpaces, which misc.steps does not name");
    }

    @Test
    public void refuses_a_size_that_is_no_positive_number_and_a_pattern_that_does_not_compile() {
        SequencedMap<String, String> size = new LinkedHashMap<>();
        size.put("misc.target", "*.txt");
        size.put("misc.steps", "leadingTabsToSpaces");
        size.put("misc.leadingTabsToSpaces.size", "0");
        assertThatThrownBy(() -> create(size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("misc.leadingTabsToSpaces.size=0")
                .hasMessageContaining("at least 1");

        SequencedMap<String, String> regex = new LinkedHashMap<>();
        regex.put("misc.target", "*.txt");
        regex.put("misc.steps", "replaceRegex");
        assertThatThrownBy(() -> create(regex))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("misc.replaceRegex.regex=<pattern>");
        regex.put("misc.replaceRegex.regex", "(unclosed");
        assertThatThrownBy(() -> create(regex))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("misc.replaceRegex.regex=(unclosed")
                .hasMessageContaining("no regular expression");
    }

    private Path execute(SequencedMap<String, String> values) throws IOException {
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
        executor.addModule("spotless", create(values), "project");
        executor.execute("spotless/required", "spotless/jacksonjson/required", "spotless/googlejavaformat/required");
        return root;
    }

    private static List<String> required(Path folder) throws IOException {
        return List.copyOf(SequencedProperties.ofFiles(folder.resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES)).stringPropertyNames());
    }

    @Test
    public void refuses_a_style_and_a_flag_it_does_not_know() {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("java.target", "*.java");
        values.put("java.steps", "palantirJavaFormat");
        values.put("java.palantirJavaFormat.style", "SQUARE");
        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.palantirJavaFormat.style=SQUARE")
                .hasMessageContaining("[PALANTIR, AOSP, GOOGLE]");
        values.put("java.palantirJavaFormat.style", "AOSP");
        values.put("java.palantirJavaFormat.formatJavadoc", "sometimes");
        assertThatThrownBy(() -> create(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.palantirJavaFormat.formatJavadoc=sometimes")
                .hasMessageContaining("true or false");
    }

    private static BuildExecutorModule create(SequencedMap<String, String> values) {
        return new SpotlessFactory().create(Environment.NONE,
                values,
                Permits.of(1),
                "spotless",
                Map.of(),
                Map.of());
    }
}
