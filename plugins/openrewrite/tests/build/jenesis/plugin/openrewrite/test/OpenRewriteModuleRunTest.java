package build.jenesis.plugin.openrewrite.test;

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
import build.jenesis.plugin.openrewrite.OpenRewriteModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OpenRewriteModuleRunTest {

    private static final String PINS = """
            openrewrite/maven/com.fasterxml.jackson.core/jackson-annotations 2.21 SHA-256/53ca085f4a150f703f49e1aabd935bd03b43e1ea3d55d135438292af22cef56b
            openrewrite/maven/com.fasterxml.jackson.core/jackson-core 2.21.6 SHA-256/36111c3a4372cd5c2be6f4ec44a050382487920f3fc38ca3015c9c1678bd7c56
            openrewrite/maven/com.fasterxml.jackson.core/jackson-databind 2.21.6 SHA-256/62134a98de12d0a421cd2f5b9854c8e63f10f524b0341e66cfd31a4326f6a329
            openrewrite/maven/com.fasterxml.jackson.dataformat/jackson-dataformat-smile 2.21.6 SHA-256/735ea360294ced091cb623cb9370604bfba89187d24b0934267475d2921f2309
            openrewrite/maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310 2.21.6 SHA-256/1d82027afe574c63e24df72eb109f98de02dff3bb5adf81f0174b27088490067
            openrewrite/maven/com.fasterxml.jackson.module/jackson-module-parameter-names 2.21.6 SHA-256/24f5c6f75fad85d7e2e3f600ef4832a839def8a2bf1e6f2fc5a0577cef54e01d
            openrewrite/maven/com.github.ben-manes.caffeine/caffeine 2.9.3 SHA-256/1e0a7bbef1dd791653143f3f05d0e489934bf5481e58a87c9e619cd46b68729b
            openrewrite/maven/com.google.errorprone/error_prone_annotations 2.10.0 SHA-256/a249d4d25dfb86d41e6c82fc335df580189f0c9feeabdc53233fc1e5060724a1
            openrewrite/maven/com.googlecode.javaewah/JavaEWAH 1.1.13 SHA-256/4c0fda2b1d317750d7ea324e36c70b2bc48310c0aaae67b98df0915d696d7111
            openrewrite/maven/com.univocity/univocity-parsers 2.9.1 SHA-256/31685122d5e392e98672ed6009a95a4c1623ca1185567bd44ee94527d454e5c3
            openrewrite/maven/io.github.classgraph/classgraph 4.8.193 SHA-256/e95ab7083d025b7f6a4fb7c0e608a9ff1ddc0ea91b4cd757c9c3a0650e61b34b
            openrewrite/maven/io.micrometer/micrometer-core 1.9.17 SHA-256/c350cfdc76c08356f12a60e840f2dbeaeea7540712d58b54becd6c302a86147a
            openrewrite/maven/io.moderne/jsonrpc 1.0.12 SHA-256/20225ec918f60137496b0596993b04d21ae35db464d67e02f8b93aa4543c4b66
            openrewrite/maven/io.quarkus.gizmo/gizmo 1.0.11.Final SHA-256/a54bfbb6dcce9298134595b3cd1df961bc732468825f26ee5296f2b5f88c0b99
            openrewrite/maven/net.java.dev.jna/jna-platform 5.19.1 SHA-256/3b3864f5b449e9c3c24b16861524b622b086563f44e0cd8384c8efc5a6052f82
            openrewrite/maven/net.java.dev.jna/jna 5.19.1 SHA-256/4fb141dd8ef6b0585ffceea4bc49602fbc6312fa977e2c488794ea3e6aafecae
            openrewrite/maven/org.antlr/antlr4-runtime 4.13.2 SHA-256/dd3e8a13a2d669bf84fb8d834de35ce4875f27157698d206241ec8488aadcaf7
            openrewrite/maven/org.apache.commons/commons-lang3 3.20.0 SHA-256/69e5c9fa35da7a51a5fd2099dfe56a2d8d32cf233e2f6d770e796146440263f4
            openrewrite/maven/org.apache.commons/commons-text 1.15.0 SHA-256/58d2da30f058512a1e7f914e39241deca4dff5c27a085b4ed2faa9e7208067f6
            openrewrite/maven/org.checkerframework/checker-qual 3.19.0 SHA-256/a827c49183f3a632277d27a0a4673686cb341507447b9d570261094bd748aa68
            openrewrite/maven/org.hdrhistogram/HdrHistogram 2.1.12 SHA-256/9b47fbae444feaac4b7e04f0ea294569e4bc282bc69d8c2ce2ac3f23577281e2
            openrewrite/maven/org.jboss/jandex 2.4.2.Final SHA-256/3f2ce55c7d71e744581488dc5105806aa8084c08e6e916a019bab8f8698994f0
            openrewrite/maven/org.jetbrains/annotations 26.1.0 SHA-256/ebc7aec252ed0c7d2d04c039d7f00e69f7b86b1f493c741d67b3ef31b986b054
            openrewrite/maven/org.jspecify/jspecify 1.0.1 SHA-256/070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66
            openrewrite/maven/org.latencyutils/LatencyUtils 2.0.3 SHA-256/a32a9ffa06b2f4e01c5360f8f9df7bc5d9454a5d373cd8f361347fa5a57165ec
            openrewrite/maven/org.objenesis/objenesis 3.6 SHA-256/6ecc0fc776af04e2bfd2bf42d618ab5161440dcb8a0e7dff9e84c065c7cec3dd
            openrewrite/maven/org.openrewrite.tools/java-object-diff 1.0.2 SHA-256/86c5e72942bb12143f2c283bb2d44c9f022e52d44361751ead8c11f3ebde2596
            openrewrite/maven/org.openrewrite.tools/jgit 1.5.0 SHA-256/50c68d603cb8d7f2585d3d98824dc509fdfd95812adb4ac93d6cec4db095b29c
            openrewrite/maven/org.openrewrite/rewrite-core 8.90.4 SHA-256/12168e8045fb62c1b3f3ad3f3ef8895db84e2791d8c811aefda410e262266503
            openrewrite/maven/org.openrewrite/rewrite-java-25 8.90.4 SHA-256/32b05a74bb19e72e4e0241c16ad49c79283e61746ce717066a7563b4947a9cf6
            openrewrite/maven/org.openrewrite/rewrite-java-lombok 8.90.4 SHA-256/db47834696d127a836b4ac51225bbe1a5b2dbab155c9f44f1b0c5cd582a6f741
            openrewrite/maven/org.openrewrite/rewrite-java 8.90.4 SHA-256/c18f5b86a2401f64d82d036460e15937dfd87d41e3b9772757cadee89b89c5bb
            openrewrite/maven/org.openrewrite/rewrite-properties 8.90.4 SHA-256/fd6d276c91cfd69fd34aa52dd67e5896cd86bbbd3c1a38d8206a36d1bdeb2b83
            openrewrite/maven/org.openrewrite/rewrite-xml 8.90.4 SHA-256/6652d97b0ecc8801bbd17158c83f445f60904d41466e3807363f2a87f77d2d2e
            openrewrite/maven/org.openrewrite/rewrite-yaml 8.90.4 SHA-256/4a13a672601e40180c2ddc234edbfcf4dab139ffb0b8ce7ec25f4e7fccf9552d
            openrewrite/maven/org.ow2.asm/asm-analysis 9.10.1 SHA-256/dede75a21306b65974ecd8f87114ff6970f09fb794157a4ca09ab25c888c2bfc
            openrewrite/maven/org.ow2.asm/asm-tree 9.10.1 SHA-256/3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b
            openrewrite/maven/org.ow2.asm/asm-util 9.10.1 SHA-256/1bb99d091fba2597dc6d51193e9bbcf0d8447e7ed96bd8f0198b18152f09655c
            openrewrite/maven/org.ow2.asm/asm 9.10.1 SHA-256/ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb
            openrewrite/maven/org.projectlombok/lombok 1.18.46 SHA-256/01f7b1a015e33e2b62d5f5f37053306357ab1415fd181fcba7794f5d198c1126
            openrewrite/maven/org.slf4j/slf4j-api 2.0.17 SHA-256/7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832
            openrewrite/maven/org.slf4j/slf4j-nop 2.0.17 SHA-256/3716f83649ec66161a2edefd4f49df34d1dd1c51cdcf941996c6987260f0a829
            openrewrite/maven/org.yaml/snakeyaml 2.6 SHA-256/c8f7a98e7394adda02f6317249710e4d1b4c7a25aa8c7eace0c2eea52eb8bf85
            """;
    private static final String UNUSED = """
            package sample;

            import java.util.List;
            import java.util.Map;

            public class Sample {
                public List<String> names() {
                    return List.of("Ada");
                }
            }
            """;

    @TempDir
    private Path root, project;

    @Test
    public void verify_mode_fails_the_build_and_leaves_the_file_untouched() throws IOException {
        Path sample = sample(UNUSED);

        assertThatThrownBy(newExecutor(true, List.of("org.openrewrite.java.RemoveUnusedImports"))::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
        assertThat(Files.readString(sample)).as("verify mode never rewrites the source").isEqualTo(UNUSED);
    }

    @Test
    public void applies_the_recipes_in_place_and_verifies_the_result() throws IOException {
        Path sample = sample(UNUSED);

        newExecutor(false, List.of("org.openrewrite.java.RemoveUnusedImports")).execute();

        assertThat(Files.readString(sample))
                .as("the unused import is removed and the used one kept")
                .contains("import java.util.List;")
                .doesNotContain("import java.util.Map;");

        newExecutor(true, List.of("org.openrewrite.java.RemoveUnusedImports")).execute();
    }

    @Test
    public void runs_a_recipe_that_the_project_declares_in_its_rewrite_yml() throws IOException {
        Path sample = sample(UNUSED);
        Files.writeString(project.resolve(OpenRewriteModule.CONFIGURATION), """
                type: specs.openrewrite.org/v1beta/recipe
                name: sample.Cleanup
                displayName: Sample cleanup
                recipeList:
                  - org.openrewrite.java.RemoveUnusedImports
                """);

        newExecutor(false, List.of("sample.Cleanup")).execute();

        assertThat(Files.readString(sample))
                .as("the declared recipe runs the recipes it lists")
                .doesNotContain("import java.util.Map;");
    }

    @Test
    public void runs_every_recipe_its_rewrite_yml_declares_when_it_names_none() throws IOException {
        Path sample = sample(UNUSED);
        Files.writeString(project.resolve(OpenRewriteModule.CONFIGURATION), """
                type: specs.openrewrite.org/v1beta/recipe
                name: sample.Cleanup
                displayName: Sample cleanup
                recipeList:
                  - org.openrewrite.java.RemoveUnusedImports
                """);

        newExecutor(false, List.of()).execute();

        assertThat(Files.readString(sample))
                .as("a rewrite.yml alone runs the recipes it declares")
                .doesNotContain("import java.util.Map;");
    }

    @Test
    public void refuses_to_run_without_a_recipe_or_a_rewrite_yml() throws IOException {
        sample(UNUSED);

        assertThatThrownBy(newExecutor(false, List.of())::execute)
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("names no recipe and is handed no rewrite.yml");
    }

    private Path sample(String content) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.java");
        Files.writeString(sample, content);
        return sample;
    }

    private BuildExecutor newExecutor(boolean verify, List<String> recipes) throws IOException {
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
                "openrewrite",
                new OpenRewriteModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .pinning(Pinning.STRICT)
                        .recipes(recipes)
                        .verify(verify),
                "project");
        return executor;
    }
}
