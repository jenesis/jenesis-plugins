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
import build.jenesis.plugin.scala.ScalaDocumentationModule;
import build.jenesis.step.ProcessHandler;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaDocumentationModuleTest {

    @TempDir
    private Path input, root;

    @Test
    public void classpath_carries_compile_dependencies_while_positional_inputs_are_class_roots() throws IOException {
        Path toolJar = Files.createFile(input.resolve("scaladoc.jar"));
        Path compileJar = Files.createFile(input.resolve("compile.jar"));
        Path classes = Files.createDirectory(input.resolve(BuildStep.CLASSES));
        Files.createFile(classes.resolve("Sample.tasty"));
        Files.writeString(Files.createDirectories(input.resolve(BuildStep.SOURCES)).resolve("Sample.scala"),
                "class Sample\n");
        SequencedProperties dependencies = new SequencedProperties();
        dependencies.setProperty("scaladoc/runtime/local/scaladoc", "scaladoc.jar");
        dependencies.setProperty("main/compile/local/compile", "compile.jar");
        dependencies.store(input.resolve(BuildStep.DEPENDENCIES));

        List<String> commands = document();

        assertThat(commands).isNotNull();
        assertThat(commands.get(commands.indexOf("-classpath") + 1).split(File.pathSeparator))
                .contains(classes.toString(), compileJar.toString())
                .doesNotContain(toolJar.toString());
        assertThat(commands.get(commands.indexOf("-cp") + 1)).isEqualTo(toolJar.toString());
        assertThat(commands.subList(commands.indexOf("-project") + 2, commands.size()))
                .containsExactly(classes.toString());
        assertThat(Path.of(commands.get(commands.indexOf("-d") + 1)).getFileName())
                .as("the documentation is written at the root of its folder, which the build places beside others")
                .hasToString("javadoc");
    }

    @Test
    public void documents_nothing_for_a_module_without_scala_sources() throws IOException {
        Files.createFile(input.resolve("scaladoc.jar"));
        Files.createFile(Files.createDirectory(input.resolve(BuildStep.CLASSES)).resolve("Only.class"));
        Files.writeString(Files.createDirectories(input.resolve(BuildStep.SOURCES)).resolve("Only.java"),
                "public class Only {}");
        SequencedProperties dependencies = new SequencedProperties();
        dependencies.setProperty("scaladoc/runtime/local/scaladoc", "scaladoc.jar");
        dependencies.store(input.resolve(BuildStep.DEPENDENCIES));

        assertThat(document())
                .as("Java sources alone are left to javadoc")
                .isNull();
    }

    private List<String> document() throws IOException {
        List<List<String>> captured = new ArrayList<>();
        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        executor.addSource("input", input);
        executor.addModule("scaladoc",
                new ScalaDocumentationModule(Map.of(),
                        Map.of("module", (_, _, _, _, _, _) -> new Resolver.Resolution(new LinkedHashMap<>(),
                                List.of(),
                                new LinkedHashMap<>())))
                        .factory(commands -> {
                            captured.add(commands);
                            return ProcessHandler.OfTool.of(new ToolProvider() {
                                @Override
                                public String name() {
                                    return "scaladoc";
                                }

                                @Override
                                public int run(PrintWriter out, PrintWriter err, String... args) {
                                    return 0;
                                }
                            }).apply(commands);
                        }),
                "input");
        executor.execute();
        return captured.isEmpty() ? null : captured.getFirst();
    }
}
