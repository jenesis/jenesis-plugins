package build.jenesis.plugin.palantir.test;

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
import build.jenesis.plugin.palantir.PalantirJavaFormatModule;

import static org.assertj.core.api.Assertions.assertThat;

public class PalantirJavaFormatModuleRunTest {

    private static final String PINS = """
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-annotations 2.21 SHA-256/53ca085f4a150f703f49e1aabd935bd03b43e1ea3d55d135438292af22cef56b
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.21 2.21 SHA-256/71cac5392151e2a528cc9a09179f0a334179f05c52f8e596cf58ddf12bb27f2b
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-core 2.21.1 SHA-256/1edd5f2e49dca5f8e4519957c24b7b3050bd1c7ee883920da33cff031ff1f7c0
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-core/pom/2.21.1 2.21.1 SHA-256/0f3fb5eb9e8970449ac0cb98e2f8e7b513d2d4ec5b1a4f02e22778e4f7bab8d7
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-databind 2.21.1 SHA-256/b011eb5202d9ec889e27f1dcbdf6c63f06a76e7a16c0a1b30c6048d556c9a28e
            palantir-java-format/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.21.1 2.21.1 SHA-256/bd4817d0b91794aff6f4b116b576bfedc9277be2d6c651c9566f552222c6a550
            palantir-java-format/maven/com.fasterxml.jackson.datatype/jackson-datatype-guava 2.21.1 SHA-256/4c1b5f7034a3179c6ade358627c5007631188ac807ebb2b0c7fcb06287527b75
            palantir-java-format/maven/com.fasterxml.jackson.datatype/jackson-datatype-guava/pom/2.21.1 2.21.1 SHA-256/da2225c15d60fac9262c8b46baf70d5449b4715296f3a773cdca2b0bcc20d085
            palantir-java-format/maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8 2.21.1 SHA-256/e39839e058fa982d3899a5698107d4ed1425c08ebade43e9a1cd308472a80587
            palantir-java-format/maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/pom/2.21.1 2.21.1 SHA-256/c29515390632721641629c0edfc620f483f35076cfb3f2374324786da33f5d65
            palantir-java-format/maven/com.fasterxml.jackson.datatype/jackson-datatypes-collections/pom/2.21.1 2.21.1 SHA-256/88dcef481a4aaee465e128eb21c98e0790f8b049d6300dd32c547c20cda85699
            palantir-java-format/maven/com.fasterxml.jackson.module/jackson-module-parameter-names 2.21.1 SHA-256/1eb87d27c27fade5a21414c57fe9fcd2b11f54b8c43549ae5e25731b5c304234
            palantir-java-format/maven/com.fasterxml.jackson.module/jackson-module-parameter-names/pom/2.21.1 2.21.1 SHA-256/53c96bfebcdbdf9aec420f47bdb0483ffa2c61f8720f71eec434c32bdcc51ecb
            palantir-java-format/maven/com.fasterxml.jackson.module/jackson-modules-java8/pom/2.21.1 2.21.1 SHA-256/80dfdc6f2f76fa4c065e11dc74531a02447245f1415b3a1270281ffcd8c92b5a
            palantir-java-format/maven/com.fasterxml.jackson/jackson-base/pom/2.21.1 2.21.1 SHA-256/60d4eb66a000a33fa37e7cb51e2550dabe7467a0b8ac60806d7d3ba9728ccd64
            palantir-java-format/maven/com.fasterxml.jackson/jackson-bom/pom/2.21.1 2.21.1 SHA-256/8d7c78dde10f4d08e7296cef084cf30b77adcea7a36e23d1520793fdec63ff28
            palantir-java-format/maven/com.fasterxml.jackson/jackson-parent/pom/2.21 2.21 SHA-256/3851df627faeb46887b956104630b74a8bbb5f7de22e974cf15982e20e0373de
            palantir-java-format/maven/com.fasterxml/oss-parent/pom/75 75 SHA-256/fcbbf1c18c9047e6917d384f2064e21b42a58fc85fb0523a9e2e015eff7c619d
            palantir-java-format/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            palantir-java-format/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            palantir-java-format/maven/com.google.errorprone/error_prone_annotations 2.47.0 SHA-256/5364bc6f22e72e98195e406a58d3ba1c09ffa11dea0729592cb870dc2de4056d
            palantir-java-format/maven/com.google.errorprone/error_prone_annotations/pom/2.47.0 2.47.0 SHA-256/d80c889a4a6f711f6945fbee79e05ec247b178a567e9d5abf58eb26ebf0a0752
            palantir-java-format/maven/com.google.errorprone/error_prone_parent/pom/2.47.0 2.47.0 SHA-256/2368a990c7a63095e1d0d44459d5a4092f0eb31f8562bd12cdf0e1c877b6a685
            palantir-java-format/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            palantir-java-format/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            palantir-java-format/maven/com.google.guava/guava 33.6.0-jre SHA-256/dc573e1fca4fd5454f4a5fd3d7da2df03002876a4175bafc14a95980dd7713b3
            palantir-java-format/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            palantir-java-format/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            palantir-java-format/maven/com.google.guava/guava-parent/pom/33.6.0-jre 33.6.0-jre SHA-256/374bd31f61b1cf612bee9ab2e4d70bbdf77dd85a49b431f809d4fbdc901f2dd4
            palantir-java-format/maven/com.google.guava/guava/pom/33.6.0-jre 33.6.0-jre SHA-256/b45b78944280054311c53da9270809fb40dcabc408ce66ce55730099b4f57d32
            palantir-java-format/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            palantir-java-format/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            palantir-java-format/maven/com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
            palantir-java-format/maven/com.google.j2objc/j2objc-annotations/pom/3.1 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
            palantir-java-format/maven/com.palantir.javaformat/palantir-java-format 2.91.0 SHA-256/488dfce17065c9204103ee216028389b2551c00d0bab660a78dd4a736cd20f91
            palantir-java-format/maven/com.palantir.javaformat/palantir-java-format-spi 2.91.0 SHA-256/eb30a72f5e123887bf0fd1ebc8a222fe0a844dbd4b258a5ac75df575da888f13
            palantir-java-format/maven/com.palantir.javaformat/palantir-java-format-spi/pom/2.91.0 2.91.0 SHA-256/d5c609be0e38772b39d2579507af80fcd7bccce04b9292c183fed5b71cd9a076
            palantir-java-format/maven/com.palantir.javaformat/palantir-java-format/pom/2.91.0 2.91.0 SHA-256/e9e0c1a71756969497ed80000935753b8b32de4cc3a83023a87e2d34cea94ba2
            palantir-java-format/maven/org.functionaljava/functionaljava 5.0 SHA-256/377ad140e7d26ba04fadf219b09d7e1c74bc0232fa4010b20c1c79db11f9670e
            palantir-java-format/maven/org.functionaljava/functionaljava/pom/5.0 5.0 SHA-256/47d380d98553daf742739b510bfdc4948a741877edcb2a5396fc29199689ce09
            palantir-java-format/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            palantir-java-format/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            palantir-java-format/maven/org.junit/junit-bom/pom/5.14.1 5.14.1 SHA-256/01b01dfa366550b40ac5760548a7d728b6109d17c451e83864d1e5e0ce862c94
            palantir-java-format/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            palantir-java-format/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            """;
    private static final String VERSION = "2.91.0";

    @TempDir
    private Path root, project;

    @Test
    public void formats_in_place_then_skips_an_unchanged_file_on_a_second_run() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, "package sample;class Sample{int f(){return 42;}}\n");

        newExecutor().execute();

        Path resolved = root.resolve("palantir-java-format").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned palantir-java-format version resolves")
                    .anyMatch(name -> name.contains("palantir-java-format") && name.contains(VERSION));
        }
        String formatted = Files.readString(sample);
        assertThat(formatted)
                .as("the source is reformatted in place")
                .contains("class Sample {")
                .contains("int f() {");
        assertThat(formatted)
                .as("the source is formatted in Palantir style, indented by four spaces rather than Google's two")
                .contains("\n    int f() {");
        Path command = root.resolve("palantir-java-format").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();
        Path hashes = root.resolve("palantir-java-format").resolve("format").resolve("output").resolve("formatted.properties");
        assertThat(hashes).isNotEmptyFile();

        newExecutor().execute();

        assertThat(Files.readString(sample)).as("the already-formatted file is left byte-identical").isEqualTo(formatted);
        assertThat(command).as("the formatter is not forked again when nothing changed").doesNotExist();
    }

    @Test
    public void keeps_long_strings_and_imports_as_told() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        String literal = "\"" + "a long string literal ".repeat(8) + "\"";
        Files.writeString(sample, "package sample;\nimport java.util.Map;\nimport java.util.List;\nclass Sample{\n"
                + "String f(){return " + literal + ";}}\n");

        newExecutor(module -> module.reflow(false).sortImports(false).removeUnusedImports(false)).execute();

        assertThat(Files.readString(sample))
                .as("the imports stay in their order, unused, and the long string on one line")
                .contains("import java.util.Map;\nimport java.util.List;")
                .contains(literal);
    }

    private BuildExecutor newExecutor() throws IOException {
        return newExecutor(module -> module);
    }

    private BuildExecutor newExecutor(UnaryOperator<PalantirJavaFormatModule> configuration) throws IOException {
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
        executor.addModule(
                "palantir-java-format",
                configuration.apply(new PalantirJavaFormatModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .pinning(Pinning.STRICT)
                        .verify(false)),
                "project");
        return executor;
    }
}
