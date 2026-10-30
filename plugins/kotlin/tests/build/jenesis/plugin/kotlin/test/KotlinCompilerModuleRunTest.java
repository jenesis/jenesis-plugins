package build.jenesis.plugin.kotlin.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Pinning;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.kotlin.KotlinCompilerModule;
import build.jenesis.step.Dependencies;
import build.jenesis.step.ProcessBuildStep;
import java.lang.classfile.Attributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KotlinCompilerModuleRunTest {

    private static final String KOTLIN_VERSION = "2.3.21",
            PINS = """
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-api 2.3.21 SHA-256/b2e5cc2666c100079ed343618528eb820dcfa5095095e30f2dd19c60dff7e462
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-api/pom/2.3.21 2.3.21 SHA-256/71f8a5ff3e40e719104f5330e283320e19d858834ef92d12bfa0d066dbc371d7
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-cri-impl 2.3.21 SHA-256/cff0a9b665851a325c2195857e2e1a5a747954c9437d00de0ba15519a4294c46
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-cri-impl/pom/2.3.21 2.3.21 SHA-256/67553bcb21e9c1167700ec3277ac7fab972e6b2b368f95bf6d467e0464dbda33
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-impl 2.3.21 SHA-256/2cd70396404a0e43c05aaf79a8b35ff3a8ff2e296aedf770f6e5923fbfacae3b
            kotlin/maven/org.jetbrains.kotlin/kotlin-build-tools-impl/pom/2.3.21 2.3.21 SHA-256/d05f6323c17f961868956831f9d5afad311035b18c3f543ffd2f3c846693d952
            kotlin/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable 2.3.21 SHA-256/d3e70fb011675e77ef00f1a68918d3cd91eed26058977d1e3ea2a37a293233af
            kotlin/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom/2.3.21 2.3.21 SHA-256/9523c8feadcb17f42d04ffa97fe2a9c08e5db18f090f6db903dca5c36c7ae5c4
            kotlin/maven/org.jetbrains.kotlin/kotlin-compiler-runner 2.3.21 SHA-256/6db7086229db89d07183a9b476e0992f86120dbc21f66092cd124c7ed6d74434
            kotlin/maven/org.jetbrains.kotlin/kotlin-compiler-runner/pom/2.3.21 2.3.21 SHA-256/1fa5485ee9cccc8e93399534b1c52a075fcd463d69a229c9caa601cc091dc288
            kotlin/maven/org.jetbrains.kotlin/kotlin-daemon-client 2.3.21 SHA-256/fa767c9b40d069f164c617a6e6bd302643c7ba8c0792e8ed9e412c18f84b50be
            kotlin/maven/org.jetbrains.kotlin/kotlin-daemon-client/pom/2.3.21 2.3.21 SHA-256/d70aa131e10d9c40d44ffb3d88c85cac54a64a30542179f847f803ec71ae1bad
            kotlin/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable 2.3.21 SHA-256/222fc10a3c22e74d30369bf8a0ca6c4f9372eac7d4a0081e290c256fa4d30fb2
            kotlin/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom/2.3.21 2.3.21 SHA-256/f5c57776c5ec3504a416364164600b0cbe85ebea8f16ebef607eadaf0fd6bf43
            kotlin/maven/org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
            kotlin/maven/org.jetbrains.kotlin/kotlin-reflect/pom/1.6.10 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
            kotlin/maven/org.jetbrains.kotlin/kotlin-script-runtime 2.3.21 SHA-256/ec61bf1229c837fa9f4255f34ca69816138b73158e7ae9b8d964cf92ca6dfd2e
            kotlin/maven/org.jetbrains.kotlin/kotlin-script-runtime/pom/2.3.21 2.3.21 SHA-256/2cf5d1123d8a628bcc5ee74f5b07b08c55a87ad4e9c91e13db83bb5a2ff6aa2d
            kotlin/maven/org.jetbrains.kotlin/kotlin-stdlib 2.3.21 SHA-256/6f64eac736db9434dd6925b4a518b9d1d17177652320c37916cf9ba3ce7d7d7a
            kotlin/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.3.21 2.3.21 SHA-256/3e0b48daa14dc73f8029d8d9e15ed32940377caf7b915510f88d33219b350186
            kotlin/maven/org.jetbrains.kotlinx/kotlinx-coroutines-bom/pom/1.8.0 1.8.0 SHA-256/1239e9dbe1397cd5971342956b2511bc3ace7b641842e4372a088dcfa8b9ad55
            kotlin/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm 1.8.0 SHA-256/9860906a1937490bf5f3b06d2f0e10ef451e65b95b269f22daf68a3d1f5065c5
            kotlin/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/pom/1.8.0 1.8.0 SHA-256/a5633abd53467ceb91622d81f2e982080877145e0b76e1b75788715434885d0b
            kotlin/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            kotlin/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            main/maven/org.jetbrains.kotlin/kotlin-stdlib 2.3.21 SHA-256/6f64eac736db9434dd6925b4a518b9d1d17177652320c37916cf9ba3ce7d7d7a
            main/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.3.21 2.3.21 SHA-256/3e0b48daa14dc73f8029d8d9e15ed32940377caf7b915510f88d33219b350186
            main/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            main/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            """;

    @TempDir
    private Path root, project, tests, testRoot;

    @Test
    public void compiles_a_kotlin_source_against_a_downloaded_compiler() throws IOException, ReflectiveOperationException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample {
                    fun greet(): String = "Hello world!"
                }
                """);

        Path classes = compile(UnaryOperator.identity());

        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
        List<URL> urls = new ArrayList<>(List.of(classes.toUri().toURL()));
        for (Path jar : Dependencies.all(root.resolve("kotlin/artifacts/resolve/output"))) {
            urls.add(jar.toUri().toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), null)) {
            Class<?> sample = loader.loadClass("sample.Sample");
            assertThat(sample.getMethod("greet").invoke(sample.getConstructor().newInstance())).isEqualTo("Hello world!");
        }
    }

    @Test
    public void leaves_out_files_other_than_sources_when_resources_are_not_included() throws IOException {
        Path sample = sources(project);
        Files.writeString(sample.resolve("Sample.kt"), """
                package sample
                class Sample
                """);
        Files.writeString(sample.resolve("app.properties"), "key=value");

        Path classes = compile(kotlin -> kotlin.includeResources(false));

        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
        assertThat(classes.resolve("sample/app.properties"))
                .as("resources are excluded when includeResources(false)")
                .doesNotExist();
    }

    @Test
    public void skips_module_info_so_kotlinc_does_not_parse_it() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample
                """);
        Files.writeString(project.resolve(BuildStep.SOURCES + "module-info.java"), """
                module sample {
                    exports sample;
                }
                """);

        Path classes = compile(UnaryOperator.identity());

        assertThat(classes.resolve("sample/Sample.class"))
                .as("kotlinc compiled the Kotlin source while ignoring module-info.java")
                .isNotEmptyFile();
        assertThat(classes.resolve("module-info.class"))
                .as("kotlinc must not parse or emit module-info.java; javac owns it")
                .doesNotExist();
    }

    @Test
    public void compiles_a_kotlin_source_below_meta_inf_versions_for_its_release_into_the_versioned_classes() throws IOException {
        Path sample = sources(project);
        Files.writeString(sample.resolve("Sample.kt"), """
                package sample
                class Sample { fun name(): String = "base" }
                """);
        Files.writeString(sample.resolve("Greeting.kt"), """
                package sample
                internal fun greeting(): String = "hello"
                """);
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "META-INF/versions/17/sample"))
                .resolve("Sample.kt"), """
                package sample
                class Sample { fun name(): String = greeting() + " 17" }
                """);
        release("11");

        Path classes = compile(UnaryOperator.identity());

        assertThat(majorVersion(classes.resolve("sample/Sample.class")))
                .as("the base is compiled for the module's release")
                .isEqualTo(55);
        assertThat(majorVersion(classes.resolve("META-INF/versions/17/sample/Sample.class")))
                .as("the overlay is compiled for its own release")
                .isEqualTo(61);
        assertThat(classes.resolveSibling("manifest.mf")).content().contains("Multi-Release: true");
        assertThat(classes.resolve("META-INF/versions/17/META-INF"))
                .as("Kotlin reads a module's mapping from the root of the jar alone, so an overlay carries none")
                .doesNotExist();
    }

    @Test
    public void the_tests_of_a_module_see_its_internal_declarations() throws IOException {
        Files.writeString(sources(project).resolve("Shout.kt"), """
                package sample
                internal fun shout(text: String): String = text.uppercase()
                """);
        Path classes = compile(UnaryOperator.identity());
        Path jar = Files.createDirectories(tests.resolve("resolved")).resolve("sample.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar));
             Stream<Path> files = Files.walk(classes)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                out.putNextEntry(new JarEntry(classes.relativize(file).toString().replace(File.separatorChar, '/')));
                Files.copy(file, out);
                out.closeEntry();
            }
        }
        Files.writeString(sources(tests).resolve("ShoutTest.kt"), """
                package sample
                class ShoutTest { fun shouts(): String = shout("hi") }
                """);
        Files.writeString(tests.resolve(BuildStep.MODULE), "test=sample\n");
        Files.writeString(tests.resolve(BuildStep.METADATA), "project=demo\nartifact=sample\nversion=1.0.0\n");
        Files.writeString(tests.resolve(BuildStep.DEPENDENCIES), "main/compile/maven/demo/sample/1.0.0=resolved/sample.jar\n");
        Files.writeString(tests.resolve(Dependencies.GRAPH),
                "vertex/main/compile/maven/demo/sample=1.0.0\\t\\tfalse\\ttrue\n");

        BuildExecutor executor = executor(testRoot);
        executor.addSource("tests", tests);
        executor.addModule("kotlin", kotlin(), "tests", standardLibrary(executor, tests, "tests"));
        executor.execute();

        assertThat(testRoot.resolve("kotlin").resolve(KotlinCompilerModule.OUTPUT).resolve("output")
                .resolve(BuildStep.CLASSES + "sample/ShoutTest.class"))
                .as("the tested jar is a friend of the test half, so its internal function is visible")
                .isNotEmptyFile();
    }

    @Test
    public void compiles_against_the_declared_dependencies_and_not_the_compiler_runtime() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                import kotlinx.coroutines.runBlocking
                fun answer(): Int = runBlocking { 42 }
                """);

        assertThatThrownBy(() -> compile(UnaryOperator.identity()))
                .as("the coroutines library kotlinc runs on is no dependency the module declared")
                .hasStackTraceContaining("kotlinx");
    }

    @Test
    public void picks_up_release_from_upstream_javac_properties() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample
                """);
        release("11");

        assertThat(majorVersion(compile(UnaryOperator.identity()).resolve("sample/Sample.class")))
                .as("--release=11 propagated to -jvm-target so the .class file carries major version 55 (Java 11)")
                .isEqualTo(55);
    }

    @Test
    public void release_8_reaches_kotlin_as_the_jvm_target_1_8_it_names_java_8_by() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample
                """);
        release("8");

        assertThat(majorVersion(compile(UnaryOperator.identity()).resolve("sample/Sample.class")))
                .as("kotlinc refuses the target 8 and takes Java 8 as 1.8, which emits major version 52")
                .isEqualTo(52);
    }

    @Test
    public void recompiles_incrementally_only_what_a_change_reaches() throws IOException {
        Path sample = sources(project);
        Files.writeString(sample.resolve("Untouched.kt"), """
                package sample
                class Untouched { fun name(): String = "untouched" }
                """);
        Files.writeString(sample.resolve("Edited.kt"), """
                package sample
                class Edited { fun value(): String = "first" }
                """);
        Files.writeString(sample.resolve("User.kt"), """
                package sample
                class User { fun use(): String = "used " + Edited().value() }
                """);
        Files.writeString(sample.resolve("Removed.kt"), """
                package sample
                class Removed
                """);
        Files.writeString(sample.resolve("data.txt"), "data");
        Path classes = compile(kotlin -> kotlin.incremental(true));
        FileTime untouched = Files.getLastModifiedTime(classes.resolve("sample/Untouched.class")),
                user = Files.getLastModifiedTime(classes.resolve("sample/User.class"));
        assertThat(classes.resolve("sample/Removed.class")).isNotEmptyFile();

        Files.writeString(sample.resolve("Edited.kt"), """
                package sample
                class Edited { fun value(): String = "second" }
                """);
        Files.delete(sample.resolve("Removed.kt"));
        compile(kotlin -> kotlin.incremental(true));
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/Untouched.class")))
                .as("a source the change does not reach keeps the class the previous run compiled")
                .isEqualTo(untouched);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/User.class")))
                .as("a body changed behind an unchanged signature leaves the class that calls it as it was")
                .isEqualTo(user);
        assertThat(classes.resolve("sample/Removed.class"))
                .as("the class of a removed source is removed with it")
                .doesNotExist();
        assertThat(classes.resolve("sample/data.txt"))
                .as("the resources are placed beside the classes the compiler wrote")
                .hasContent("data");
        assertThat(new String(Files.readAllBytes(classes.resolve("sample/Edited.class")), StandardCharsets.ISO_8859_1))
                .as("the edited source is compiled again")
                .contains("second");

        Files.writeString(sample.resolve("Edited.kt"), """
                package sample
                class Edited { fun value(): Int = 2 }
                """);
        compile(kotlin -> kotlin.incremental(true));
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/User.class")))
                .as("a changed signature recompiles the source that uses it")
                .isNotEqualTo(user);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/Untouched.class"))).isEqualTo(untouched);
    }

    @Test
    public void hands_a_jvm_option_to_the_jvm_that_compiles_incrementally() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample
                """);
        SequencedProperties kotlinc = new SequencedProperties();
        kotlinc.setProperty("-J-Xss4m", "");
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "kotlinc", kotlinc);

        Path classes = compile(kotlin -> kotlin.incremental(true));

        List<String> command = Arrays.asList(Files.readString(root.resolve("kotlin/compiled/supplement/command"))
                .split(" "));
        assertThat(command.indexOf("-Xss4m"))
                .as("a -J line is an option of the JVM that compiles the module, ahead of its class path")
                .isNotNegative()
                .isLessThan(command.indexOf("-cp"));
        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
    }

    @Test
    public void compiles_a_kotlin_source_below_meta_inf_versions_incrementally_as_well() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample { fun name(): String = "base" }
                """);
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "META-INF/versions/17/sample"))
                .resolve("Sample.kt"), """
                package sample
                class Sample { fun name(): String = "17" }
                """);
        release("11");

        Path classes = compile(kotlin -> kotlin.incremental(true));

        assertThat(majorVersion(classes.resolve("sample/Sample.class"))).isEqualTo(55);
        assertThat(majorVersion(classes.resolve("META-INF/versions/17/sample/Sample.class")))
                .as("the overlay is compiled for its own release")
                .isEqualTo(61);
        assertThat(classes.resolveSibling("manifest.mf")).content().contains("Multi-Release: true");
        assertThat(classes.resolve("META-INF/versions/17/META-INF"))
                .as("Kotlin reads a module's mapping from the root of the jar alone, so an overlay carries none")
                .doesNotExist();
        assertThat(root.resolve("kotlin/compiled/supplement/command-17"))
                .as("the overlay is compiled through the build tools API, as the module is, rather than by kotlinc's own main class")
                .content()
                .contains("--tool")
                .doesNotContain("K2JVMCompiler");
    }

    @Test
    public void names_the_kotlin_module_after_the_artifact_as_the_kotlin_maven_plugin_does() throws IOException {
        assertModuleNamedAfterArtifact(false);
    }

    @Test
    public void names_the_kotlin_module_after_the_artifact_when_compiling_incrementally() throws IOException {
        assertModuleNamedAfterArtifact(true);
    }

    private void assertModuleNamedAfterArtifact(boolean incremental) throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample {
                    internal fun hidden(): String = "hidden"
                }
                """);
        SequencedProperties metadata = new SequencedProperties();
        metadata.setProperty("project", "group");
        metadata.setProperty("artifact", "sample-artifact");
        metadata.store(project.resolve(BuildStep.METADATA));

        Path classes = compile(kotlin -> kotlin.incremental(incremental));

        assertThat(classes.resolve("META-INF/sample-artifact.kotlin_module"))
                .as("each module carries a module file of its own name, never kotlinc's default main")
                .isNotEmptyFile();
        assertThat(ClassFile.of().parse(classes.resolve("sample/Sample.class")).methods().stream()
                .map(method -> method.methodName().stringValue()))
                .as("an internal member is mangled with the module's name")
                .contains("hidden$sample_artifact");
    }

    @Test
    public void hands_the_lines_of_process_kotlinc_properties_to_kotlinc_and_its_j_lines_to_the_jvm() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), """
                package sample
                class Sample {
                    fun greet(name: String): String = "Hello " + name
                }
                """);
        SequencedProperties kotlinc = new SequencedProperties();
        kotlinc.setProperty("-module-name", "custom-name");
        kotlinc.setProperty("-java-parameters", "");
        kotlinc.setProperty("-api-version", "2.0");
        kotlinc.setProperty("-language-version", "2.0");
        kotlinc.setProperty("-J-Xss4m", "");
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "kotlinc", kotlinc);

        Path classes = compile(UnaryOperator.identity());

        assertThat(classes.resolve("META-INF/custom-name.kotlin_module"))
                .as("-module-name reached kotlinc, which names the module file after it")
                .isNotEmptyFile();
        ClassModel sample = ClassFile.of().parse(classes.resolve("sample/Sample.class"));
        assertThat(sample.methods().stream()
                .filter(method -> method.methodName().equalsString("greet"))
                .flatMap(method -> method.findAttribute(Attributes.methodParameters()).stream()))
                .as("-java-parameters reached kotlinc, which then writes the MethodParameters attribute")
                .isNotEmpty();
        List<String> command = Arrays.asList(Files.readString(root.resolve("kotlin/compiled/supplement/command"))
                .split(" "));
        assertThat(command.indexOf("-Xss4m"))
                .as("a -J line goes to the JVM that runs kotlinc, ahead of its main class")
                .isNotNegative()
                .isLessThan(command.indexOf("build.jenesis.plugin.kotlin.KotlincDriver"));
        assertThat(command.indexOf("-api-version"))
                .as("a compiler argument follows kotlinc's main class")
                .isGreaterThan(command.indexOf("build.jenesis.plugin.kotlin.KotlincDriver"));
    }

    @Test
    public void kotlin_can_reference_java_sources_supplied_to_the_same_step() throws IOException {
        Path sample = sources(project);
        Files.writeString(sample.resolve("Greeter.java"), """
                package sample;
                public class Greeter {
                    public String hello() { return "Hello"; }
                }
                """);
        Files.writeString(sample.resolve("Sample.kt"), """
                package sample
                class Sample {
                    fun greet(): String = Greeter().hello() + " from Kotlin"
                }
                """);

        assertThat(compile(UnaryOperator.identity()).resolve("sample/Sample.class"))
                .as("Sample.kt resolved the Greeter type from the .java source companion and compiled")
                .isNotEmptyFile();
    }

    @Test
    public void downloads_the_full_compiler_dependency_set_from_the_kotlin_pom() throws IOException {
        Files.writeString(sources(project).resolve("Sample.kt"), "package sample\nclass Sample\n");

        compile(UnaryOperator.identity());

        assertThat(SequencedProperties.ofFiles(root.resolve("kotlin/artifacts/resolve/output")
                .resolve(BuildStep.DEPENDENCIES)).stringPropertyNames())
                .anyMatch(name -> name.contains("kotlin-compiler-embeddable"))
                .anyMatch(name -> name.contains("kotlin-stdlib"))
                .anyMatch(name -> name.contains("kotlinx-coroutines-core"))
                .anyMatch(name -> name.contains("annotations"));
    }

    private Path sources(Path folder) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(folder.resolve(BuildStep.VERSIONS));
        return Files.createDirectories(folder.resolve(BuildStep.SOURCES + "sample"));
    }

    private void release(String release) throws IOException {
        SequencedProperties javac = new SequencedProperties();
        javac.setProperty("--release", release);
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "javac", javac);
    }

    private Path compile(UnaryOperator<KotlinCompilerModule> configuration) throws IOException {
        BuildExecutor executor = executor(root);
        executor.addSource("project", project);
        executor.addModule("kotlin", configuration.apply(kotlin()), "project", standardLibrary(executor, project, "project"));
        executor.execute();
        return root.resolve("kotlin").resolve(KotlinCompilerModule.OUTPUT).resolve("output").resolve(BuildStep.CLASSES);
    }

    private static KotlinCompilerModule kotlin() {
        return new KotlinCompilerModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT);
    }

    private static String standardLibrary(BuildExecutor executor, Path folder, String source) throws IOException {
        Path file = folder.resolve(BuildStep.REQUIRES);
        SequencedProperties requires = Files.exists(file) ? SequencedProperties.ofFiles(file) : new SequencedProperties();
        requires.setProperty("main/compile/maven/org.jetbrains.kotlin/kotlin-stdlib/" + KOTLIN_VERSION, "");
        requires.store(file);
        executor.addModule(source + "-libraries", new Dependencies(
                Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT).group("main"), source);
        return source + "-libraries";
    }

    private static int majorVersion(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
    }

    private static BuildExecutor executor(Path root) throws IOException {
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
