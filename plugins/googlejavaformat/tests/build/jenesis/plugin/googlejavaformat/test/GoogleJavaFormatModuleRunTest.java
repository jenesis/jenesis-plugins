package build.jenesis.plugin.googlejavaformat.test;

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
import build.jenesis.plugin.googlejavaformat.GoogleJavaFormatModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GoogleJavaFormatModuleRunTest {

    private static final String PINS = """
            google-java-format/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            google-java-format/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            google-java-format/maven/com.google.errorprone/error_prone_annotations 2.21.1 SHA-256/d1f3c66aa91ac52549e00ae3b208ba4b9af7d72d68f230643553beb38e6118ac
            google-java-format/maven/com.google.errorprone/error_prone_annotations/pom/2.21.1 2.21.1 SHA-256/f598880feefaea9d674dc41db13ab37004bf03776b5bb21c04dede8e920c1f12
            google-java-format/maven/com.google.errorprone/error_prone_parent/pom/2.21.1 2.21.1 SHA-256/32bb0b5ff241fd6ba1feea448aebb9cedef1699be73cb6f319365387b82bf92c
            google-java-format/maven/com.google.googlejavaformat/google-java-format 1.35.0 SHA-256/b2a52e363d51dd86b3b38d1a8dd51538541b42ff1048769684681c676550f043
            google-java-format/maven/com.google.googlejavaformat/google-java-format-parent/pom/1.35.0 1.35.0 SHA-256/9aa329c60659bf429cb1d99341401d6f92088afc69be5f4bc09c6ef8c154f5e3
            google-java-format/maven/com.google.googlejavaformat/google-java-format/pom/1.35.0 1.35.0 SHA-256/2d7c391affb930d6263d3b18a139a530bb9bbaf367211013aeb2f806d106d61c
            google-java-format/maven/com.google.guava/failureaccess 1.0.1 SHA-256/a171ee4c734dd2da837e4b16be9df4661afab72a41adaf31eb84dfdaf936ca26
            google-java-format/maven/com.google.guava/failureaccess/pom/1.0.1 1.0.1 SHA-256/e96042ce78fecba0da2be964522947c87b40a291b5fd3cd672a434924103c4b9
            google-java-format/maven/com.google.guava/guava 32.1.3-jre SHA-256/6d4e2b5a118aab62e6e5e29d185a0224eed82c85c40ac3d33cf04a270c3b3744
            google-java-format/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            google-java-format/maven/com.google.guava/guava-parent/pom/32.1.3-jre 32.1.3-jre SHA-256/f283c1f04897a9a88a3fa4ff46804e65e82114809a09cd04094bf7de01b1857b
            google-java-format/maven/com.google.guava/guava/pom/32.1.3-jre 32.1.3-jre SHA-256/700e6d46e75b5939a22a41c25ec2bb122f3cbef4efed45e3312fddcbe993db33
            google-java-format/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            google-java-format/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            google-java-format/maven/com.google.j2objc/j2objc-annotations 2.8 SHA-256/f02a95fa1a5e95edb3ed859fd0fb7df709d121a35290eff8b74dce2ab7f4d6ed
            google-java-format/maven/com.google.j2objc/j2objc-annotations/pom/2.8 2.8 SHA-256/37f87798b18385113c918bfa9e1276fe50735ef8fa849b5800c519d54dbf11f8
            google-java-format/maven/org.checkerframework/checker-qual 3.37.0 SHA-256/e4ce1376cc2735e1dde220b62ad0913f51297704daad155a33f386bc5db0d9f7
            google-java-format/maven/org.checkerframework/checker-qual/pom/3.37.0 3.37.0 SHA-256/02392fbd4ce21901f945615472b1d4d4d3591b3aabdf013105f5c92ec32cb4f0
            google-java-format/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            google-java-format/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            """;
    private static final String VERSION = "1.35.0";

    @TempDir
    private Path root, project;

    @Test
    public void formats_in_place_then_skips_an_unchanged_file_on_a_second_run() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, "package sample;class Sample{int f(){return 42;}}\n");

        newExecutor(false).execute();

        Path resolved = root.resolve("google-java-format").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned google-java-format version resolves")
                    .anyMatch(name -> name.contains("google-java-format") && name.contains(VERSION));
        }
        String formatted = Files.readString(sample);
        assertThat(formatted)
                .as("the source is reformatted in place")
                .contains("class Sample {")
                .contains("int f() {");
        Path command = root.resolve("google-java-format").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();
        Path hashes = root.resolve("google-java-format").resolve("format").resolve("output").resolve("formatted.properties");
        assertThat(hashes).isNotEmptyFile();

        newExecutor(false).execute();

        assertThat(Files.readString(sample)).as("the already-formatted file is left byte-identical").isEqualTo(formatted);
        assertThat(command).as("the formatter is not forked again when nothing changed").doesNotExist();
    }

    @Test
    public void verify_mode_fails_the_build_and_leaves_the_file_untouched() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        String unformatted = "package sample;class Sample{int f(){return 42;}}\n";
        Files.writeString(sample, unformatted);

        assertThatThrownBy(newExecutor(true)::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
        assertThat(Files.readString(sample)).as("verify mode never rewrites the source").isEqualTo(unformatted);
        assertThat(root.resolve("google-java-format").resolve("format").resolve("output").resolve("formatted.properties"))
                .as("verify mode does not persist the incremental hash state")
                .doesNotExist();
    }

    @Test
    public void formats_in_aosp_style_and_leaves_long_strings_javadoc_and_imports_as_told() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        String literal = "\"" + "a long string literal ".repeat(6) + "\"";
        Files.writeString(sample, "package sample;\nimport java.util.List;\nclass Sample{\n"
                + "/** Kept   as   written. */\nString f(){return " + literal + ";}}\n");

        newExecutor(false, module -> module.aosp(true)
                .reflow(false)
                .javadoc(false)
                .removeUnusedImports(false)).execute();

        assertThat(Files.readString(sample))
                .as("the AOSP style indents by four, and the options leave the rest as written")
                .contains("    String f() {")
                .contains("import java.util.List;")
                .contains(literal)
                .contains("Kept   as   written.");
    }

    @Test
    public void verifies_again_when_an_option_changes() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, "package sample;\n\nclass Sample {\n  int f() {\n    return 42;\n  }\n}\n");

        newExecutor(true).execute();

        assertThatThrownBy(newExecutor(true, module -> module.aosp(true))::execute)
                .as("the unchanged source is verified again once the style changes")
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
    }

    private BuildExecutor newExecutor(boolean verify) throws IOException {
        return newExecutor(verify, module -> module);
    }

    private BuildExecutor newExecutor(boolean verify, UnaryOperator<GoogleJavaFormatModule> configuration) throws IOException {
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
                "google-java-format",
                configuration.apply(new GoogleJavaFormatModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .pinning(Pinning.STRICT)
                        .verify(verify)),
                "project");
        return executor;
    }
}
