package build.jenesis.plugin.clojure.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.clojure.ClojureCompilerModule;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClojureCompilerModuleTest {

    private static final List<String> CAPTURED = new CopyOnWriteArrayList<>();

    @TempDir
    private Path root, project, dependencies;

    @BeforeEach
    public void clear() {
        CAPTURED.clear();
    }

    @Test
    public void names_a_namespace_after_the_path_of_its_source() {
        assertThat(ClojureCompilerModule.namespace("sample/core.clj")).isEqualTo("sample.core");
        assertThat(ClojureCompilerModule.namespace("my_app/data_util.cljc")).isEqualTo("my-app.data-util");
        assertThatThrownBy(() -> ClojureCompilerModule.namespace("sample/Core.java"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sample/Core.java")
                .hasMessageContaining(".clj or .cljc");
    }

    @Test
    public void attributes_the_classes_of_a_namespace_to_it() {
        List<String> namespaces = List.of("my-app.core");

        assertThat(ClojureCompilerModule.defines("my_app/core__init", namespaces)).isTrue();
        assertThat(ClojureCompilerModule.defines("my_app/core$greet", namespaces)).isTrue();
        assertThat(ClojureCompilerModule.defines("my_app/core", namespaces)).isTrue();
        assertThat(ClojureCompilerModule.defines("my_app/core/Record", namespaces)).isTrue();
        assertThat(ClojureCompilerModule.defines("my_app/core_util__init", namespaces))
                .as("a namespace sharing a prefix is a namespace of its own")
                .isFalse();
        assertThat(ClojureCompilerModule.defines("my_app/other__init", namespaces)).isFalse();
    }

    @Test
    public void compiles_every_namespace_with_the_clojure_of_the_module_and_ships_its_sources() throws IOException {
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("core.clj"), "(ns sample.core)");
        Files.writeString(sample.resolve("data_util.cljc"), "(ns sample.data-util)");
        Files.writeString(sample.resolve("Greeter.java"), "package sample; public class Greeter {}");
        Files.writeString(project.resolve(BuildStep.SOURCES + "data_readers.clj"), "{}");
        Path jar = clojure();

        compile(true);

        Path classes = root.resolve("clojure").resolve(ClojureCompilerModule.COMPILED).resolve("output")
                .resolve(BuildStep.CLASSES);
        assertThat(CAPTURED.getFirst()).startsWith("-Dclojure.compile.path=");
        assertThat(CAPTURED.subList(1, CAPTURED.size()))
                .as("the namespaces compile in order, the data readers being no namespace")
                .containsSubsequence("-cp", "clojure.lang.Compile", "sample.core", "sample.data-util")
                .doesNotContain("data-readers");
        assertThat(CAPTURED.get(CAPTURED.indexOf("-cp") + 1))
                .as("the compile path, which holds the sources, leads the class path of the module's own Clojure")
                .startsWith(CAPTURED.getFirst().substring("-Dclojure.compile.path=".length()))
                .contains(jar.toString());
        assertThat(classes.resolve("sample/core.clj")).hasContent("(ns sample.core)");
        assertThat(classes.resolve("sample/data_util.cljc")).exists();
        assertThat(classes.resolve("data_readers.clj")).exists();
        assertThat(classes.resolve("sample/Greeter.java")).doesNotExist();
    }

    @Test
    public void ships_the_sources_alone_where_it_compiles_nothing_ahead_of_time() throws IOException {
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("core.clj"), "(ns sample.core)");

        compile(false);

        assertThat(CAPTURED).isEmpty();
        assertThat(root.resolve("clojure").resolve(ClojureCompilerModule.VERSIONED).resolve("output")
                .resolve(BuildStep.CLASSES).resolve("sample/core.clj"))
                .hasContent("(ns sample.core)");
    }

    @Test
    public void refuses_to_compile_without_clojure_on_the_class_path() throws IOException {
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("core.clj"),
                "(ns sample.core)");

        assertThatThrownBy(() -> compile(true))
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("[sample.core]")
                .hasMessageContaining("org.clojure:clojure")
                .hasMessageContaining("aot=false");
    }

    @Test
    public void runs_no_compiler_without_a_clojure_source() throws IOException {
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Greeter.java"),
                "package sample; public class Greeter {}");
        clojure();

        compile(true);

        assertThat(CAPTURED).isEmpty();
    }

    private Path clojure() throws IOException {
        Path jar = dependencies.resolve("clojure.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("clojure/lang/Compile.class"));
            out.closeEntry();
        }
        SequencedProperties index = new SequencedProperties();
        index.setProperty("main/compile/maven/org.clojure/clojure", "clojure.jar");
        index.store(dependencies.resolve(BuildStep.DEPENDENCIES));
        return jar;
    }

    private void compile(boolean aot) throws IOException {
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
        executor.addSource("dependencies", dependencies);
        executor.addModule("clojure",
                new ClojureCompilerModule()
                        .aot(aot)
                        .factory(commands -> {
                            CAPTURED.addAll(commands);
                            return ProcessHandler.OfTool.of(new Silent()).apply(commands);
                        }),
                "project",
                "dependencies");
        executor.execute();
    }

    private record Silent() implements ToolProvider {

        @Override
        public String name() {
            return "clojure";
        }

        @Override
        public int run(PrintWriter out, PrintWriter err, String... args) {
            return 0;
        }
    }
}
