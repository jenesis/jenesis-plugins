package build.jenesis.plugin.scala.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.scala.ScalaCompilerModule;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScalaCompilerModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_picks_module_coordinate_when_module_resolver_available() throws IOException {
        assertThat(required(new ScalaCompilerModule(Map.of(), Map.of("module", empty()))))
                .containsExactly("scalac/runtime/module/org.scala.lang.scala3.compiler");
        assertThat(root.resolve("scala/required/output").resolve(BuildStep.VERSIONS)).doesNotExist();
    }

    @Test
    public void requires_step_picks_maven_coordinate_when_only_maven_resolver_available() throws IOException {
        assertThat(required(new ScalaCompilerModule(Map.of(), Map.of("maven", empty()))))
                .containsExactly("scalac/runtime/maven/org.scala-lang/scala3-compiler_3/RELEASE");
    }

    @Test
    public void requires_step_prefers_maven_over_module_when_both_available() throws IOException {
        assertThat(required(new ScalaCompilerModule(Map.of(), Map.of("module", empty(), "maven", empty()))))
                .singleElement()
                .satisfies(name -> assertThat(name).startsWith("scalac/runtime/maven/"));
    }

    @Test
    public void requires_step_fails_when_no_supported_prefix_is_registered() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("scala", new ScalaCompilerModule(Map.of(), Map.of("vendor", empty())), "project");

        assertThatThrownBy(() -> executor.execute("scala/required"))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("No suitable resolver");
    }

    @Test
    public void requires_zinc_and_the_bridge_in_their_groups_when_compiling_incrementally() throws IOException {
        assertThat(required(new ScalaCompilerModule(Map.of(), Map.of("maven", empty()))
                .incremental(true)
                .tool("compiler")
                .zinc("engine")))
                .containsExactlyInAnyOrder("compiler/runtime/maven/org.scala-lang/scala3-compiler_3/RELEASE",
                        "compiler/runtime/maven/org.scala-lang/scala3-sbt-bridge/RELEASE",
                        "engine/runtime/maven/org.scala-sbt/zinc_2.13/RELEASE");
    }

    @Test
    public void refuses_an_incremental_compile_without_a_maven_resolver() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("scala",
                new ScalaCompilerModule(Map.of(), Map.of("module", empty())).incremental(true),
                "project");

        assertThatThrownBy(() -> executor.execute("scala/required"))
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name a Maven repository, or set jenesis.scala.incremental=false");
    }

    @Test
    public void exposes_resolved_alongside_classes_so_coordinates_reach_the_pin_stage() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("scala", new ScalaCompilerModule(Map.of(), Map.of("maven", empty())), "project");

        assertThat(executor.execute()).containsKeys("scala/classes", "scala/artifacts");
    }

    @Test
    public void hands_the_compiler_the_scala_and_java_sources_but_neither_module_info_nor_resources() throws IOException {
        Path compiler = Files.createFile(project.resolve("compiler.jar")),
                library = Files.createFile(project.resolve("library.jar"));
        SequencedProperties dependencies = new SequencedProperties();
        dependencies.setProperty("scalac/runtime/local/compiler", "compiler.jar");
        dependencies.setProperty("main/compile/local/library", "library.jar");
        dependencies.store(project.resolve(BuildStep.DEPENDENCIES));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("Sample.scala"), "package sample\nclass Sample\n");
        Files.writeString(sample.resolve("Greeter.java"), "package sample; public class Greeter {}");
        Files.writeString(sample.resolve("app.properties"), "key=value");
        Files.writeString(project.resolve(BuildStep.SOURCES + "module-info.java"), "module sample {}");

        List<String> commands = compile();

        assertThat(commands).isNotNull();
        assertThat(commands.get(commands.indexOf("-cp") + 1)).isEqualTo(compiler.toString());
        assertThat(commands.get(commands.indexOf("-classpath") + 1).split(File.pathSeparator))
                .as("the compiler's own jars carry the Scala library that the sources compile against")
                .contains(compiler.toString(), library.toString());
        assertThat(commands)
                .contains(sample.resolve("Sample.scala").toString(), sample.resolve("Greeter.java").toString())
                .noneMatch(argument -> argument.endsWith("module-info.java") || argument.endsWith("app.properties"));
        assertThat(root.resolve("scala/classes/output").resolve(BuildStep.CLASSES).resolve("sample/app.properties"))
                .as("the build copies the resources of a module that a language compiles")
                .doesNotExist();
    }

    @Test
    public void runs_no_compiler_for_a_module_without_scala_sources() throws IOException {
        Files.createFile(project.resolve("compiler.jar"));
        SequencedProperties dependencies = new SequencedProperties();
        dependencies.setProperty("scalac/runtime/local/compiler", "compiler.jar");
        dependencies.store(project.resolve(BuildStep.DEPENDENCIES));
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Only.java"),
                "package sample; public class Only {}");

        assertThat(compile())
                .as("Java sources alone are left to javac")
                .isNull();
    }

    private List<String> compile() throws IOException {
        List<List<String>> captured = new ArrayList<>();
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("scala",
                new ScalaCompilerModule(Map.of(), Map.of("module", empty())).factory(commands -> {
                    captured.add(commands);
                    return ProcessHandler.OfTool.of(new ToolProvider() {
                        @Override
                        public String name() {
                            return "scalac";
                        }

                        @Override
                        public int run(PrintWriter out, PrintWriter err, String... args) {
                            return 0;
                        }
                    }).apply(commands);
                }),
                "project");
        executor.execute();
        return captured.isEmpty() ? null : captured.getFirst();
    }

    private Set<String> required(ScalaCompilerModule module) throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("scala", module, "project");
        executor.execute("scala/required");
        return SequencedProperties.ofFiles(root.resolve("scala/required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames();
    }

    private static Resolver empty() {
        return (_, _, _, _, _, _, _, _) -> new Resolver.Resolution(new LinkedHashMap<>(),
                List.of(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>());
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
