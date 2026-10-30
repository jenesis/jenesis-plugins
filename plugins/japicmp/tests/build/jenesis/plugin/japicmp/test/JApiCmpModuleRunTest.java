package build.jenesis.plugin.japicmp.test;

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
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.japicmp.JApiCmpFactory;
import build.jenesis.plugin.japicmp.JApiCmpModule;
import build.jenesis.step.ProcessBuildStep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JApiCmpModuleRunTest {

    private static final String PINS = """
            japicmp/maven/com.github.siom79.japicmp/japicmp 0.26.2 SHA-256/317c4d11ef25469203d6de9c5b804c62392f52e3edc9e42e42161a192335bc3a
            japicmp/maven/com.github.siom79.japicmp/japicmp-base/pom/0.26.2 0.26.2 SHA-256/69db85c998c74286a2ccb626522947303ca48bffe6a159ef2499338d3de3636d
            japicmp/maven/com.github.siom79.japicmp/japicmp/pom/0.26.2 0.26.2 SHA-256/efea5822cfda3cd777442994845e3556842159e397c28071e277bb673927bb09
            japicmp/maven/com.google.errorprone/error_prone_annotations 2.36.0 SHA-256/77440e270b0bc9a249903c5a076c36a722c4886ca4f42675f2903a1c53ed61a5
            japicmp/maven/com.google.errorprone/error_prone_annotations/pom/2.36.0 2.36.0 SHA-256/d79cfd37c85f76d6b754c7501ee1dc8447b7b2642c2382d778252166bd331e9c
            japicmp/maven/com.google.errorprone/error_prone_parent/pom/2.36.0 2.36.0 SHA-256/3a4cfc8a6bed61eb48e969796fc31ea0d270b63e670599946a61883adb7094dc
            japicmp/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            japicmp/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            japicmp/maven/com.google.guava/guava 33.4.8-jre SHA-256/f3d7f57f67fd622f4d468dfdd692b3a5e3909246c28017ac3263405f0fe617ed
            japicmp/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            japicmp/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            japicmp/maven/com.google.guava/guava-parent/pom/33.4.8-jre 33.4.8-jre SHA-256/a03c5199a1be14443df7fd40ba8673b1c6aae04deef6688ce7d573ea489e3a20
            japicmp/maven/com.google.guava/guava/pom/33.4.8-jre 33.4.8-jre SHA-256/04365d4b6ef22c8cf9349fe628069fc3e81a2c838351402ef4e95f9e757beebc
            japicmp/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            japicmp/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            japicmp/maven/com.google.j2objc/j2objc-annotations 3.0.0 SHA-256/88241573467ddca44ffd4d74aa04c2bbfd11bf7c17e0c342c94c9de7a70a7c64
            japicmp/maven/com.google.j2objc/j2objc-annotations/pom/3.0.0 3.0.0 SHA-256/23b3d039e168ad89dd114698e6dd7be383f4a2c577b8877d82c73a6515e74a17
            japicmp/maven/com.sun.activation/all/pom/1.2.1 1.2.1 SHA-256/360883bf64486ecef161b8f282f6503536dd1a670d53a0a871c8fb20170e6795
            japicmp/maven/com.sun.istack/istack-commons-runtime 3.0.8 SHA-256/4ffabb06be454a05e4398e20c77fa2b6308d4b88dfbef7ca30a76b5b7d5505ef
            japicmp/maven/com.sun.istack/istack-commons-runtime/pom/3.0.8 3.0.8 SHA-256/c2e014d34cb84ed287d064986c45c305a4124228a7337eccf6c421d14d708f1c
            japicmp/maven/com.sun.istack/istack-commons/pom/3.0.8 3.0.8 SHA-256/a0f0517e8512f0fbcc7b8295c12f6566a3d0c2d86d655639dc662ef8c0c7ebe5
            japicmp/maven/com.sun.xml.bind.mvn/jaxb-parent/pom/2.3.2 2.3.2 SHA-256/20dd6dc34ab7549ac40da1d82e92222ec4347ad0ec0cb118ef6c5703bed53a18
            japicmp/maven/com.sun.xml.bind.mvn/jaxb-runtime-parent/pom/2.3.2 2.3.2 SHA-256/b24f8d51f184a68bc1b86d48c0e3cfefeb21a44ede1c5f7303c58ae0488533ec
            japicmp/maven/com.sun.xml.bind.mvn/jaxb-txw-parent/pom/2.3.2 2.3.2 SHA-256/b55d3efa9b158f483a30e92c78ccb600f93314733d089eba9b74436f01b314a4
            japicmp/maven/com.sun.xml.bind/jaxb-bom-ext/pom/2.3.2 2.3.2 SHA-256/1a7dec2b27e7e055744cdb8cf1b90def4fd473acd1b804eff098139358959bd7
            japicmp/maven/com.sun.xml.fastinfoset/FastInfoset 1.2.16 SHA-256/056f3a1e144409f21ed16afc26805f58e9a21f3fce1543c42d400719d250c511
            japicmp/maven/com.sun.xml.fastinfoset/FastInfoset/pom/1.2.16 1.2.16 SHA-256/e147d258ab6e6691f70599a952400e6e6c7558f8c9c028dbe1be23178308e830
            japicmp/maven/com.sun.xml.fastinfoset/fastinfoset-project/pom/1.2.16 1.2.16 SHA-256/90582425adc1f40b41362dafb95173931225acaa5e79620d5e6bd52f646292ba
            japicmp/maven/jakarta.activation/jakarta.activation-api 1.2.1 SHA-256/8b0a0f52fa8b05c5431921a063ed866efaa41dadf2e3a7ee3e1961f2b0d9645b
            japicmp/maven/jakarta.activation/jakarta.activation-api/pom/1.2.1 1.2.1 SHA-256/42585cb07dda7f23aa04eb5e0940061944a246a67ad3d16942fbe569ff03cd31
            japicmp/maven/jakarta.xml.bind/jakarta.xml.bind-api 2.3.2 SHA-256/69156304079bdeed9fc0ae3b39389f19b3cc4ba4443bc80508995394ead742ea
            japicmp/maven/jakarta.xml.bind/jakarta.xml.bind-api-parent/pom/2.3.2 2.3.2 SHA-256/15a55b7d537c9f9970aead28d2af97c059f65ff6102f76bbd29f1247dd8a6dfb
            japicmp/maven/jakarta.xml.bind/jakarta.xml.bind-api/pom/2.3.2 2.3.2 SHA-256/b537b388dbab4cc0690b9d2fb0c74124d672531734567acf6e53130eab131ad6
            japicmp/maven/org.apiguardian/apiguardian-api 1.1.2 SHA-256/b509448ac506d607319f182537f0b35d71007582ec741832a1f111e5b5b70b38
            japicmp/maven/org.apiguardian/apiguardian-api/pom/1.1.2 1.1.2 SHA-256/32355081d109095c3d5d374d5a43b4f4c1b75d549e983ef50723e2772e5302a0
            japicmp/maven/org.eclipse.ee4j/project/pom/1.0.2 1.0.2 SHA-256/7495a07a797e88e43c3bc1a87421bd8b1fc55e32291fa18e4e32d8031ddc873f
            japicmp/maven/org.eclipse.ee4j/project/pom/1.0.5 1.0.5 SHA-256/916b4794d8d8220a59a3fdf6a64dbe794aeb23395e888b81ae36a9b5a2c591a6
            japicmp/maven/org.glassfish.jaxb/jaxb-bom/pom/2.3.2 2.3.2 SHA-256/a1018bb54678ed9f5acb2f7a4084e385ff510201f4e9dbf5f75dc6a675f66be7
            japicmp/maven/org.glassfish.jaxb/jaxb-runtime 2.3.2 SHA-256/e6e0a1e89fb6ff786279e6a0082d5cef52dc2ebe67053d041800737652b4fd1b
            japicmp/maven/org.glassfish.jaxb/jaxb-runtime/pom/2.3.2 2.3.2 SHA-256/9448a5ad7fa68a6083dfbe4f42c8c83e082b9202a105401fc68e944c26548b34
            japicmp/maven/org.glassfish.jaxb/txw2 2.3.2 SHA-256/4a6a9f483388d461b81aa9a28c685b8b74c0597993bf1884b04eddbca95f48fe
            japicmp/maven/org.glassfish.jaxb/txw2/pom/2.3.2 2.3.2 SHA-256/a79dd002fb038183ff286a2635be2e68c103b87e0e64717d8d44bfd017fd33ea
            japicmp/maven/org.javassist/javassist 3.30.2-GA SHA-256/eba37290994b5e4868f3af98ff113f6244a6b099385d9ad46881307d3cb01aaf
            japicmp/maven/org.javassist/javassist/pom/3.30.2-GA 3.30.2-GA SHA-256/4227851cb70e43f73aced8bffe692de3ae4412c4a62dcdff055e513ee3d801a3
            japicmp/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            japicmp/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            japicmp/maven/org.jvnet.staxex/stax-ex 1.8.1 SHA-256/20522549056e9e50aa35ef0b445a2e47a53d06be0b0a9467d704e2483ffb049a
            japicmp/maven/org.jvnet.staxex/stax-ex/pom/1.8.1 1.8.1 SHA-256/8fc84f36ce6da6ce8c893b6538199a7f69a69a0706d9b17a3ee6a3a09452eed6
            japicmp/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            """;
    private static final String BASELINE = "org.apiguardian/apiguardian-api/1.1.2";
    private static final String COMPATIBLE = """
            package org.apiguardian.api;
            public @interface API {
                Status status();
                String since() default "";
                String[] consumers() default "*";
                enum Status {
                    INTERNAL, DEPRECATED, EXPERIMENTAL, MAINTAINED, STABLE;
                }
            }
            """;

    private static final String BREAKING = """
            package org.apiguardian.api;
            public @interface API {
                Status status();
                String since() default "";
                enum Status {
                    INTERNAL, DEPRECATED, EXPERIMENTAL, MAINTAINED, STABLE;
                }
            }
            """;

    @TempDir
    private Path root, project, sources;

    @Test
    public void downloads_the_pinned_japicmp_and_reports_on_the_released_baseline() throws IOException {
        writeProject(COMPATIBLE);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("japicmp", module(), "project");
        executor.execute();

        Path report = root.resolve("japicmp").resolve("compare").resolve("output")
                .resolve(BuildStep.REPORTS).resolve("japicmp").resolve("japicmp-report.xml");
        assertThat(report).isNotEmptyFile();
        assertThat(report).content()
                .as("the released baseline is what the built jar is compared against")
                .contains("oldVersion=\"1.1.2\"")
                .contains("org.apiguardian.api.API");
    }

    @Test
    public void a_removed_method_is_reported_without_failing_the_build() throws IOException {
        writeProject(BREAKING);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("japicmp", module(), "project");
        executor.execute();

        Path report = root.resolve("japicmp").resolve("compare").resolve("output")
                .resolve(BuildStep.REPORTS).resolve("japicmp").resolve("japicmp-report.xml");
        assertThat(report).content()
                .as("report-only run records the removal and leaves the build green")
                .contains("consumers");
    }

    @Test
    public void the_binary_incompatibility_gate_fails_the_build_on_a_removed_method() throws IOException {
        writeProject(BREAKING);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("japicmp",
                module().errorOn(EnumSet.of(JApiCmpModule.Failure.BINARY_INCOMPATIBILITY)),
                "project");

        assertThatThrownBy(executor::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
    }

    @Test
    public void hands_the_lines_of_its_process_file_to_japicmp_rather_than_to_its_jvm() throws IOException {
        writeProject(COMPATIBLE);
        SequencedProperties process = new SequencedProperties();
        process.setProperty("--include-synthetic", "");
        process.setProperty("-J-Xmx256m", "");
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "japicmp", process);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("japicmp", module(), "project");
        executor.execute();

        String command = Files.readString(root.resolve("japicmp").resolve("compare").resolve("supplement").resolve("command"));
        assertThat(command.substring(command.indexOf("japicmp.JApiCmp")))
                .as("an option japicmp takes follows its main class, and a -J option is the JVM's")
                .contains(" --include-synthetic")
                .doesNotContain("Xmx256m");
        assertThat(command.substring(0, command.indexOf("japicmp.JApiCmp"))).contains(" -Xmx256m ");
    }

    @Test
    public void hands_japicmp_a_method_pattern_whole_although_its_parameters_are_separated_by_a_comma() throws IOException {
        writeProject(COMPATIBLE);
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("japicmp", new JApiCmpFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("baseline", BASELINE,
                        "exclude", "org.apiguardian.api.API#since(int,java.lang.String), org.apiguardian.api.Other")),
                Permits.of(1),
                "japicmp",
                Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))), "project");
        executor.execute();

        assertThat(Files.readString(root.resolve("japicmp").resolve("compare").resolve("supplement").resolve("command")))
                .contains(" -e 'org.apiguardian.api.API#since(int,java.lang.String);org.apiguardian.api.Other'");
    }

    private JApiCmpModule module() {
        return new JApiCmpModule(
                Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                .pinning(Pinning.STRICT)
                .baseline(BASELINE);
    }

    private void writeProject(String annotation) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));

        Path source = Files.createDirectories(sources.resolve("org").resolve("apiguardian").resolve("api"))
                .resolve("API.java");
        Files.writeString(source, annotation);
        Path classes = Files.createDirectories(sources.resolve("classes"));
        int rc = javax.tools.ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), "--release", "17", source.toString());
        assertThat(rc).as("the vendored annotation compiles").isZero();

        Path artifact = Files.createDirectories(project.resolve(BuildStep.ARTIFACTS)).resolve("classes.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(artifact))) {
            Path folder = classes.resolve("org").resolve("apiguardian").resolve("api");
            try (Stream<Path> files = Files.list(folder)) {
                for (Path file : files.sorted().toList()) {
                    jar.putNextEntry(new JarEntry("org/apiguardian/api/" + file.getFileName()));
                    Files.copy(file, jar);
                    jar.closeEntry();
                }
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
}
