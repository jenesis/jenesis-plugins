package build.jenesis.plugin.checkstyle.test;

import module java.base;
import module org.junit.jupiter.api;
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
import build.jenesis.plugin.checkstyle.CheckstyleModule;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckstyleModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_emits_the_checkstyle_maven_coordinate() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("checkstyle", new CheckstyleModule(Map.of(), Map.of()), "project");
        executor.execute("checkstyle/required");

        Path requiredOutput = root.resolve("checkstyle").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("checkstyle/runtime/maven/com.puppycrawl.tools/checkstyle/RELEASE");
    }

    @Test
    public void tool_emits_an_independent_resolution_trail() throws IOException {
        SequencedProperties requires = new SequencedProperties();
        requires.setProperty("main/compile/maven/org.example/library/1.0", "");
        requires.store(project.resolve(BuildStep.REQUIRES));

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "checkstyle",
                new CheckstyleModule(Map.of("maven", files()), Map.of("maven", Resolver.identity()))
                        .tool("custom"),
                "project");
        executor.execute("checkstyle/dependencies");

        Path resolvedOutput = root.resolve("checkstyle").resolve("dependencies").resolve("resolve").resolve("output");
        SequencedProperties resolved = SequencedProperties.ofFiles(resolvedOutput.resolve(BuildStep.DEPENDENCIES));
        assertThat(resolved.stringPropertyNames())
                .as("the module's own closure is resolved by the module, not again by every tool")
                .containsExactly("custom/runtime/maven/com.puppycrawl.tools/checkstyle/RELEASE");
    }

    @Test
    public void puts_the_jars_of_a_checkstyle_plugin_on_the_class_path_of_checkstyle() throws IOException {
        SequencedProperties requires = new SequencedProperties();
        requires.setProperty("checkstyle/plugin/maven/org.example/checks/1.0", "");
        requires.store(project.resolve(BuildStep.REQUIRES));
        Files.writeString(project.resolve("checkstyle.xml"), "<module name=\"Checker\"/>");
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java"),
                "package sample; public class Sample { }");
        Path served = Files.createDirectories(root.resolve("served"));
        ClassDesc main = ClassDesc.of("com.puppycrawl.tools.checkstyle.Main"),
                options = ClassDesc.of("com.puppycrawl.tools.checkstyle.Main$CliOptions"),
                commandLine = ClassDesc.of("picocli.CommandLine"),
                parseResult = ClassDesc.of("picocli.CommandLine$ParseResult");
        jar(served.resolve("checkstyle.jar"), Map.of(
                main, ClassFile.of().build(main, type -> type.withFlags(ClassFile.ACC_PUBLIC).withMethodBody("execute",
                        MethodTypeDesc.of(ConstantDescs.CD_int, parseResult, options),
                        ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC,
                        code -> code.ldc("org.example.Check")
                                .invokestatic(ConstantDescs.CD_Class,
                                        "forName",
                                        MethodTypeDesc.of(ConstantDescs.CD_Class, ConstantDescs.CD_String))
                                .pop()
                                .iconst_0()
                                .ireturn())),
                options, empty(options, MethodTypeDesc.of(ConstantDescs.CD_void)),
                parseResult, empty(parseResult, MethodTypeDesc.of(ConstantDescs.CD_void)),
                commandLine, ClassFile.of().build(commandLine, type -> type.withFlags(ClassFile.ACC_PUBLIC)
                        .withMethodBody(ConstantDescs.INIT_NAME,
                                MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Object),
                                ClassFile.ACC_PUBLIC,
                                code -> code.aload(0)
                                        .invokespecial(ConstantDescs.CD_Object,
                                                ConstantDescs.INIT_NAME,
                                                ConstantDescs.MTD_void)
                                        .return_())
                        .withMethodBody("setUsageHelpWidth",
                                MethodTypeDesc.of(commandLine, ConstantDescs.CD_int),
                                ClassFile.ACC_PUBLIC,
                                code -> code.aload(0).areturn())
                        .withMethodBody("setCaseInsensitiveEnumValuesAllowed",
                                MethodTypeDesc.of(commandLine, ConstantDescs.CD_boolean),
                                ClassFile.ACC_PUBLIC,
                                code -> code.aload(0).areturn())
                        .withMethodBody("parseArgs",
                                MethodTypeDesc.of(parseResult, ConstantDescs.CD_String.arrayType()),
                                ClassFile.ACC_PUBLIC,
                                code -> code.new_(parseResult)
                                        .dup()
                                        .invokespecial(parseResult, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                                        .areturn()))));
        jar(served.resolve("checks.jar"), Map.of(ClassDesc.of("org.example.Check"), ClassFile.of().build(
                ClassDesc.of("org.example.Check"),
                type -> type.withFlags(ClassFile.ACC_PUBLIC))));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("checkstyle",
                new CheckstyleModule(Map.of("maven", (_, coordinate, _) -> Optional.of(RepositoryItem.ofFile(
                        served.resolve(coordinate.contains("checkstyle") ? "checkstyle.jar" : "checks.jar")))),
                        Map.of("maven", Resolver.identity())),
                "project");
        assertThat(executor.execute("checkstyle/check"))
                .as("a custom check that a @jenesis.plugin line names loads beside Checkstyle")
                .containsKey("checkstyle/check");
    }

    private static byte[] empty(ClassDesc type, MethodTypeDesc constructor) {
        return ClassFile.of().build(type, builder -> builder.withFlags(ClassFile.ACC_PUBLIC)
                .withMethodBody(ConstantDescs.INIT_NAME, constructor, ClassFile.ACC_PUBLIC, code -> code.aload(0)
                        .invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                        .return_()));
    }

    private static void jar(Path file, Map<ClassDesc, byte[]> types) throws IOException {
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(file))) {
            for (Map.Entry<ClassDesc, byte[]> type : types.entrySet()) {
                out.putNextEntry(new JarEntry(type.getKey().descriptorString()
                        .substring(1, type.getKey().descriptorString().length() - 1) + ".class"));
                out.write(type.getValue());
                out.closeEntry();
            }
        }
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

    private Repository files() {
        return (_, coordinate, _) -> {
            Path file = Files.write(
                    Files.createDirectories(root.resolve("served")).resolve(coordinate.replace('/', '-') + ".jar"),
                    coordinate.getBytes(StandardCharsets.UTF_8));
            return Optional.of(RepositoryItem.ofFile(file));
        };
    }
}
