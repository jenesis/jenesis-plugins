package build.jenesis.plugin.groovy.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Language;
import build.jenesis.Permits;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.groovy.GroovyCompilerModule;
import build.jenesis.plugin.groovy.GroovyDocumentationModule;
import build.jenesis.plugin.groovy.GroovyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GroovyFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void compiles_groovy_after_javac_and_documents_it() throws IOException {
        Language language = create(Map.of(), Map.of());

        assertThat(language.extensions()).containsExactly(".groovy");
        assertThat(language.order())
                .as("Groovy reads the classes javac wrote, so that it may use the Java types of its module")
                .isEqualTo(Language.Order.AFTER_JAVAC);
        assertThat(language.documentation()).isNotNull();
    }

    @Test
    public void resolves_groovy_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
        Language language = create(Map.of(), Map.of("maven", Resolver.identity()));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("compiler", language.compiler(), "project");
        executor.addModule("documentation", language.documentation(), "project");
        executor.execute("compiler/" + GroovyCompilerModule.REQUIRED,
                "documentation/" + GroovyDocumentationModule.REQUIRED);

        assertThat(requires(root.resolve("compiler").resolve(GroovyCompilerModule.REQUIRED)))
                .as("the compiler resolves and is pinned in the tool group of the plugin the loading build names")
                .containsExactly("lang:tool/runtime/maven/org.apache.groovy/groovy/RELEASE");
        assertThat(requires(root.resolve("documentation").resolve(GroovyDocumentationModule.REQUIRED)))
                .containsExactly("lang:tool/runtime/maven/org.apache.groovy/groovy-groovydoc/RELEASE");
    }

    @Test
    public void exposes_the_classes_alone() throws IOException {
        Language language = create(Map.of(), Map.of("maven", (_, _, _, _, _, _) -> new Resolver.Resolution(
                new LinkedHashMap<>(),
                List.of(),
                new LinkedHashMap<>())));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("compiler", language.compiler(), "project");

        SequencedMap<String, Path> outputs = executor.execute();

        assertThat(outputs)
                .as("the requirement and the classes before their versions are processed stay inside the plugin")
                .containsKey("compiler/classes")
                .doesNotContainKeys("compiler/required", "compiler/compiled");
    }

    @Test
    public void refuses_a_value_it_does_not_take() {
        assertThatThrownBy(() -> create(Map.of("version", "5.0.1"), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("takes no value version")
                .hasMessageContaining("include-java");
    }

    @Test
    public void refuses_an_include_java_that_is_no_flag() {
        assertThatThrownBy(() -> create(Map.of("include-java", "sometimes"), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sometimes");
    }

    private static Language create(Map<String, String> values, Map<String, Resolver> resolvers) throws IOException {
        return new GroovyFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "lang",
                Map.of(),
                resolvers);
    }

    private static Set<String> requires(Path step) throws IOException {
        return SequencedProperties.ofFiles(step.resolve("output").resolve(BuildStep.REQUIRES)).stringPropertyNames();
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
