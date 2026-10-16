package build.jenesis.plugin.kotlin.test;

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
import build.jenesis.Json;
import build.jenesis.Language;
import build.jenesis.Permits;
import build.jenesis.RepositoryItem;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.kotlin.DokkaDocumentationModule;
import build.jenesis.plugin.kotlin.KotlinFactory;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KotlinFactoryTest {

    private static final List<String> CONFIGURATIONS = new CopyOnWriteArrayList<>();

    @TempDir
    private Path root, project;

    @BeforeEach
    public void clear() {
        CONFIGURATIONS.clear();
    }

    @Test
    public void compiles_kotlin_sources_before_javac_and_documents_them_with_dokka() throws IOException {
        Language language = create(Map.of());

        assertThat(language.extensions()).containsExactly(".kt");
        assertThat(language.order())
                .as("javac compiles against the classes of the Kotlin sources, as Java code may use them")
                .isEqualTo(Language.Order.BEFORE_JAVAC);
        assertThat(language.documentation()).isInstanceOf(DokkaDocumentationModule.class);
    }

    @Test
    public void resolves_the_compiler_in_the_tool_group_of_the_plugin_and_dokka_in_a_group_beside_it() throws IOException {
        Language language = create(Map.of());

        assertThat(required(language.compiler(), "kotlin", "kotlin"))
                .containsExactly("kotlin:tool/runtime/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/RELEASE");
        assertThat(required(language.documentation(), "dokka", "dokka"))
                .as("Dokka runs on a Kotlin of its own, so it is pinned apart from the compiler")
                .contains("kotlin:dokka/runtime/maven/org.jetbrains.dokka/dokka-cli/RELEASE");
    }

    @Test
    public void exposes_the_classes_of_the_compiler_alone() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("kotlin", create(Map.of()).compiler(), "project");

        assertThat(executor.execute())
                .as("the build merges every classes folder it is handed, so the unversioned ones stay inside")
                .containsKeys("kotlin/classes", "kotlin/artifacts")
                .doesNotContainKey("kotlin/compiled");
    }

    @Test
    public void hands_dokka_the_values_it_is_configured_with() throws IOException {
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.kt"),
                "package sample\nclass Sample");
        Language language = create(Map.of("dokka.module-name", "Configured",
                "dokka.documented-visibilities", "public, Protected",
                "dokka.skip-deprecated", "true",
                "dokka.suppressed-packages", "sample\\.internal.*",
                "dokka.jdk-version", "17",
                "dokka.source-link", "https://github.com/example/sample/tree/main/src",
                "dokka.footer-message", "Configured footer",
                "dokka.external-link.okio", "https://square.github.io/okio/3.x/okio/",
                "dokka.external-link.okio.package-list", "https://square.github.io/okio/3.x/okio/okio/package-list",
                "dokka.external-link.coroutines", "https://kotlinlang.org/api/kotlinx.coroutines"));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("dokka",
                ((DokkaDocumentationModule) language.documentation()).factory(ProcessHandler.OfTool.of(new Recording())),
                "project");
        executor.execute();

        assertThat(CONFIGURATIONS).as("Dokka ran once").hasSize(1);
        Map<String, Object> configuration = map(Json.parse(CONFIGURATIONS.getFirst()));
        assertThat(configuration).containsEntry("moduleName", "Configured");
        assertThat(map(Json.parse((String) map(list(configuration.get("pluginsConfiguration")).getFirst()).get("values"))))
                .containsEntry("footerMessage", "Configured footer");
        Map<String, Object> sourceSet = map(list(configuration.get("sourceSets")).getFirst());
        assertThat(sourceSet)
                .containsEntry("documentedVisibilities", List.of("PUBLIC", "PROTECTED"))
                .containsEntry("skipDeprecated", true)
                .containsEntry("jdkVersion", 17.0);
        assertThat(list(sourceSet.get("perPackageOptions"))).singleElement()
                .satisfies(options -> assertThat(map(options)).containsEntry("matchingRegex", "sample\\.internal.*"));
        assertThat(list(sourceSet.get("sourceLinks"))).singleElement()
                .satisfies(link -> assertThat(map(link)).containsEntry("remoteLineSuffix", "#L"));
        assertThat(list(sourceSet.get("externalDocumentationLinks"))).containsExactly(
                Map.of("url", "https://kotlinlang.org/api/kotlinx.coroutines",
                        "packageListUrl", "https://kotlinlang.org/api/kotlinx.coroutines/package-list"),
                Map.of("url", "https://square.github.io/okio/3.x/okio/",
                        "packageListUrl", "https://square.github.io/okio/3.x/okio/okio/package-list"));
    }

    @Test
    public void refuses_a_value_it_does_not_take() {
        assertThatThrownBy(() -> create(Map.of("incremental", "true")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("takes no value incremental")
                .hasMessageContaining("dokka.<key>");
        assertThatThrownBy(() -> create(Map.of("dokka.includes", "Module.md")))
                .as("a file reaches a plugin as an input, which a language plugin is handed none of")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("takes no value dokka.includes");
    }

    @Test
    public void refuses_a_package_list_of_no_link() {
        assertThatThrownBy(() -> create(Map.of("dokka.external-link.okio.package-list", "https://example.com/package-list")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dokka.external-link.okio=<url>");
    }

    @Test
    public void refuses_a_format_and_a_flag_it_cannot_read() {
        assertThatThrownBy(() -> create(Map.of("dokka.format", "markdown")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names markdown")
                .hasMessageContaining("[html, javadoc]");
        assertThatThrownBy(() -> create(Map.of("dokka.skip-deprecated", "maybe")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maybe");
    }

    private Language create(Map<String, String> values) throws IOException {
        Path jar = Files.exists(root.resolve("tool.jar")) ? root.resolve("tool.jar") : Files.createFile(root.resolve("tool.jar"));
        return new KotlinFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "kotlin",
                Map.of("maven", (_, _, _) -> Optional.of(RepositoryItem.ofFile(jar))),
                Map.of("maven", Resolver.identity()));
    }

    private Set<String> required(BuildExecutorModule module, String name, String path) throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(name, module, "project");
        executor.execute(path + "/required");
        return SequencedProperties.ofFiles(root.resolve(path).resolve("required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) {
        return (List<Object>) value;
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

    private record Recording() implements ToolProvider {

        @Override
        public String name() {
            return "dokka";
        }

        @Override
        public int run(PrintWriter out, PrintWriter err, String... args) {
            try {
                CONFIGURATIONS.add(Files.readString(Path.of(args[args.length - 1])));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return 0;
        }
    }
}
