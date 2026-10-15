package build.jenesis.plugin.ksp.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.ksp.KspFactory;
import build.jenesis.plugin.ksp.KspModule;
import build.jenesis.step.Dependencies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KspModuleRunTest {

    private static final Map<String, Repository> REPOSITORIES = Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE));
    private static final Map<String, Resolver> RESOLVERS = Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE));
    private static final String PINS = """
            ksp\\:tool/maven/com.google.devtools.ksp/symbol-processing-aa-embeddable 2.3.12 SHA-256/862f0042fc2467cdcf7752e15880f97396a29e2469741dc6453f37584f1b4c69
            ksp\\:tool/maven/com.google.devtools.ksp/symbol-processing-api 2.3.12 SHA-256/fc7d11cea3bcd873589c8914ad6320bdc877bce14cc7c2f717394646af56bd37
            ksp\\:tool/maven/com.google.devtools.ksp/symbol-processing-common-deps 2.3.12 SHA-256/30510a4364e02880ce2fbf0c4f0ef564dead3818eef4fc0500cbcee23fc362af
            ksp\\:tool/maven/org.jetbrains.kotlin/kotlin-stdlib 2.3.20 SHA-256/0ae12504a5040ebaf37703908483420d1a5624dd1d93f357665f8c77c848a01e
            ksp\\:tool/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            ksp\\:processor/maven/com.google.auto.service/auto-service-annotations 1.0 SHA-256/44752893119fdaf01b4c5ee74e46e5dab86f2dcda18114c562f877355c6ed26e
            ksp\\:processor/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            ksp\\:processor/maven/com.google.errorprone/error_prone_annotations 2.5.1 SHA-256/ff80626baaf12a09342befd4e84cba9d50662f5fcd7f7a9b3490a6b7cf87e66c
            ksp\\:processor/maven/com.google.guava/failureaccess 1.0.1 SHA-256/a171ee4c734dd2da837e4b16be9df4661afab72a41adaf31eb84dfdaf936ca26
            ksp\\:processor/maven/com.google.guava/guava 30.1.1-jre SHA-256/44ce229ce26d880bf3afc362bbfcec34d7e6903d195bbb1db9f3b6e0d9834f06
            ksp\\:processor/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            ksp\\:processor/maven/com.google.j2objc/j2objc-annotations 1.3 SHA-256/21af30c92267bd6122c0e0b4d20cccb6641a37eaf956c6540ec471d584e64a7b
            ksp\\:processor/maven/com.squareup.moshi/moshi 1.15.2 SHA-256/767f7592c6c61eb693549a26d333c121f5383826bd287b899867aa326dae55e4
            ksp\\:processor/maven/com.squareup.moshi/moshi-kotlin-codegen 1.15.2 SHA-256/949ce943e0ee135bd41ba4abf08b3a1608d4378671de12388690b5432b4e8a3b
            ksp\\:processor/maven/com.squareup.okio/okio-jvm 3.7.0 SHA-256/d8b35adc28768f43ae5afe6a7d1aa2a878ba51e0b96a4f308811f3b1f5b13e55
            ksp\\:processor/maven/com.squareup/kotlinpoet 1.13.2 SHA-256/7cbf7f4330170f9459d15c105fae6e13784cb37855fd68cf2826dfa0b5deb676
            ksp\\:processor/maven/com.squareup/kotlinpoet-ksp 1.13.2 SHA-256/d1de5e3d6f7405dc10cb14a43f39400ba75604e1cf86da1fe08e9007a8a17d08
            ksp\\:processor/maven/org.checkerframework/checker-qual 3.8.0 SHA-256/c88c2e6a5fdaeb9f26fcf879264042de8a9ee9d376e2477838feaabcfa44dda6
            ksp\\:processor/maven/org.jetbrains.kotlin/kotlin-reflect 1.8.21 SHA-256/8a6cd5a3cf092acee274ce2c444dc36eefdb631579859dd4d857b3309a529c91
            ksp\\:processor/maven/org.jetbrains.kotlin/kotlin-stdlib 1.8.21 SHA-256/042a1cd1ac976cdcfe5eb63f1d8e0b0b892c9248e15a69c8cfba495d546ea52a
            ksp\\:processor/maven/org.jetbrains.kotlin/kotlin-stdlib-common 1.8.21 SHA-256/6a44c9ecc9d7754d9e943fb1e3588c74d4a3f1785be51074f49d6c5723682a73
            ksp\\:processor/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.8.21 SHA-256/33d148db0e11debd0d90677d28242bced907f9c77730000fd597867089039d86
            ksp\\:processor/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.8.21 SHA-256/3db752a30074f06ee6c57984aa6f27da44f4d2bbc7f5442651f6988f1cb2b7d7
            ksp\\:processor/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            ksp\\:processor/maven/org.ow2.asm/asm 9.3 SHA-256/1263369b59e29c943918de11d6d6152e2ec6085ce63e5710516f8c67d368e4bc
            main/maven/com.squareup.moshi/moshi 1.15.2 SHA-256/767f7592c6c61eb693549a26d333c121f5383826bd287b899867aa326dae55e4
            main/maven/com.squareup.okio/okio-jvm 3.7.0 SHA-256/d8b35adc28768f43ae5afe6a7d1aa2a878ba51e0b96a4f308811f3b1f5b13e55
            main/maven/org.jetbrains.kotlin/kotlin-stdlib 1.8.21 SHA-256/042a1cd1ac976cdcfe5eb63f1d8e0b0b892c9248e15a69c8cfba495d546ea52a
            main/maven/org.jetbrains.kotlin/kotlin-stdlib-common 1.8.21 SHA-256/6a44c9ecc9d7754d9e943fb1e3588c74d4a3f1785be51074f49d6c5723682a73
            main/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.8.21 SHA-256/33d148db0e11debd0d90677d28242bced907f9c77730000fd597867089039d86
            main/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.8.21 SHA-256/3db752a30074f06ee6c57984aa6f27da44f4d2bbc7f5442651f6988f1cb2b7d7
            main/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            """;

    @TempDir
    private Path root, project;

    @BeforeEach
    public void writeProject() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Point.kt"), """
                package sample

                import com.squareup.moshi.JsonClass

                @JsonClass(generateAdapter = true)
                data class Point(val x: Int, val y: Int)
                """);
    }

    @Test
    public void generates_the_sources_and_resources_of_the_declared_processors() throws IOException {
        Files.writeString(project.resolve(BuildStep.REQUIRES), """
                main/compile/maven/com.squareup.moshi/moshi/1.15.2=
                ksp\\:processor/plugin/maven/com.squareup.moshi/moshi-kotlin-codegen/1.15.2=
                """);
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("libraries", libraries(), "project");
        executor.addModule("ksp", newModule(), "project", "libraries");
        executor.execute();

        assertThat(generated().resolve(BuildStep.SOURCES + "sample/PointJsonAdapter.kt"))
                .as("the processor resolved the annotation against the module's libraries")
                .content()
                .contains("class PointJsonAdapter");
        assertThat(generated().resolve(BuildStep.RESOURCES + "META-INF/proguard/moshi-sample.Point.pro")).isNotEmptyFile();
    }

    @Test
    public void hands_its_values_to_the_processors_as_options() throws IOException {
        Files.writeString(project.resolve(BuildStep.REQUIRES), """
                main/compile/maven/com.squareup.moshi/moshi/1.15.2=
                ksp\\:processor/plugin/maven/com.squareup.moshi/moshi-kotlin-codegen/1.15.2=
                """);
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("moshi.generated", "javax.annotation.processing.Generated");
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("libraries", libraries(), "project");
        executor.addModule("ksp",
                new KspFactory().create(new Environment(Map.of("dependency.pin", "strict")),
                        values,
                        Permits.of(1),
                        "ksp",
                        REPOSITORIES,
                        RESOLVERS),
                "project",
                "libraries");
        executor.execute();

        assertThat(generated().resolve(BuildStep.SOURCES + "sample/PointJsonAdapter.kt"))
                .as("the value reached the processor as the option moshi.generated")
                .content()
                .contains("@Generated(");
    }

    @Test
    public void processes_only_what_a_change_reaches_in_a_warm_jvm_and_removes_what_a_removed_source_generated() throws IOException {
        Files.writeString(project.resolve(BuildStep.REQUIRES), """
                main/compile/maven/com.squareup.moshi/moshi/1.15.2=
                ksp\\:processor/plugin/maven/com.squareup.moshi/moshi-kotlin-codegen/1.15.2=
                """);
        Path sample = project.resolve(BuildStep.SOURCES + "sample");
        generate();
        Path point = generated().resolve(BuildStep.SOURCES + "sample/PointJsonAdapter.kt");
        FileTime generatedAt = Files.getLastModifiedTime(point);

        Files.writeString(sample.resolve("Line.kt"), """
                package sample

                import com.squareup.moshi.JsonClass

                @JsonClass(generateAdapter = true)
                data class Line(val from: Point, val to: Point)
                """);
        generate();
        assertThat(generated().resolve(BuildStep.SOURCES + "sample/LineJsonAdapter.kt"))
                .as("the added source is processed")
                .content()
                .contains("class LineJsonAdapter");
        assertThat(Files.getLastModifiedTime(point))
                .as("a source the change does not reach keeps what it generated before")
                .isEqualTo(generatedAt);

        Files.delete(sample.resolve("Line.kt"));
        generate();
        assertThat(generated().resolve(BuildStep.SOURCES + "sample/LineJsonAdapter.kt"))
                .as("what a removed source generated is removed with it")
                .doesNotExist();
        assertThat(point).isNotEmptyFile();
    }

    @Test
    public void refuses_to_run_without_a_declared_processor() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("ksp", newModule(), "project");
        assertThatThrownBy(executor::execute)
                .hasStackTraceContaining("no symbol processor is declared")
                .hasStackTraceContaining("@jenesis.plugin ksp:processor <coordinate>");
    }

    private void generate() throws IOException {
        Environment warm = new Environment(Map.of("process.daemon", "true"));
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("libraries", libraries(), "project");
        executor.addModule("ksp", KspModule.ofEnvironment(warm, REPOSITORIES, RESOLVERS).pinning(Pinning.STRICT), "project", "libraries");
        executor.execute();
    }

    private Path generated() {
        return root.resolve("ksp").resolve(KspModule.GENERATE).resolve("output");
    }

    private Dependencies libraries() {
        return new Dependencies(REPOSITORIES, RESOLVERS).pinning(Pinning.STRICT).group("main");
    }

    private KspModule newModule() {
        return new KspModule(REPOSITORIES, RESOLVERS).pinning(Pinning.STRICT);
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
