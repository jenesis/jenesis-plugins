package build.jenesis.plugin.asciidoctor.test;

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
import build.jenesis.plugin.asciidoctor.AsciidoctorModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsciidoctorModuleRunTest {

    private static final String PINS = """
            asciidoctor/maven/com.github.jnr/jffi 1.3.13 SHA-256/78df5fb05d7e2541b867bedc538b18840245a601bb2160fa26824bb67ed93878
            asciidoctor/maven/com.github.jnr/jffi/jar/native 1.3.13 SHA-256/a38dd049951a27f7a423389fecf0195a5bd414f418e90f5ac9992652c22a5b8e
            asciidoctor/maven/com.github.jnr/jnr-a64asm 1.0.0 SHA-256/53ae5ea7fa5c284e8279aa348e7b9de4548b0cae10bfd058fa217c791875e4cf
            asciidoctor/maven/com.github.jnr/jnr-constants 0.10.4 SHA-256/9a5b8cf9798d9d0331b8d8966c5235a22c4307676e35803a24659e6d76096f78
            asciidoctor/maven/com.github.jnr/jnr-enxio 0.32.18 SHA-256/0dfde0b64e92429b1a20e54ee77fca606c4f458a5efe54b461884dd0a1e823a3
            asciidoctor/maven/com.github.jnr/jnr-ffi 2.2.17 SHA-256/37b494600b956a7932e799d4a7284bbf1ad0b0b23b4df62d0c01f55f1d3ee681
            asciidoctor/maven/com.github.jnr/jnr-netdb 1.2.0 SHA-256/24f54abd859979b6caabf4918b022c57fc0d3247def4bfe68e5a30172409fc3c
            asciidoctor/maven/com.github.jnr/jnr-posix 3.1.20 SHA-256/e4f28d47be66a8710950425c037318442af2b8021708de0b7871ff65fab8cd86
            asciidoctor/maven/com.github.jnr/jnr-unixsocket 0.38.23 SHA-256/50d273039f9920f567c17a5881fde6a6f7d4b637fb9961b3b7c96dd542e6adc2
            asciidoctor/maven/com.github.jnr/jnr-x86asm 1.0.2 SHA-256/39f3675b910e6e9b93825f8284bec9f4ad3044cd20a6f7c8ff9e2f8695ebf21e
            asciidoctor/maven/com.headius/backport9 1.13 SHA-256/89dccb0f38901d9d15280c55d3a435bb7867d93756b17a2bf99f505daf9f9a1f
            asciidoctor/maven/com.headius/invokebinder 1.13 SHA-256/c6a4406f7aae8d016b2cc3206adba08a6d5b845868c556d6e5b3e43fe205741a
            asciidoctor/maven/com.headius/options 1.6 SHA-256/c4c52c7931c945792d48c6010596195359a35a82d6ac36c645f14705b9a277db
            asciidoctor/maven/joda-time/joda-time 2.12.7 SHA-256/385282b005818cfaccdbe8bd2429811e7e641782f2b88932a6b8ff51d668f616
            asciidoctor/maven/me.qmx.jitescript/jitescript 0.4.1 SHA-256/035d4afc5a27202c3e3f38c32506e3657697a4d6d97463eb25affe1cbf45efd5
            asciidoctor/maven/org.asciidoctor/asciidoctorj-api 3.0.1 SHA-256/6b5d6dc7fc43c5620751dfb880f70822db313067dfa1005f80abd3a29686d8af
            asciidoctor/maven/org.asciidoctor/asciidoctorj 3.0.1 SHA-256/dca289ed528a5bf2de2429211d831199e205c423e2f31e54394e11a7cecb579c
            asciidoctor/maven/org.crac/crac 1.5.0 SHA-256/f4426e1641c8f0fa2f025a4cd7c40c285abaf265930e6717adfcaef03d034850
            asciidoctor/maven/org.jruby.jcodings/jcodings 1.0.63 SHA-256/ef11e1741a65160c5619cd2c3c79c40ee0affda847062f012616034b9d10e719
            asciidoctor/maven/org.jruby.joni/joni 2.2.5 SHA-256/c2cdc754e9fb428c96e510cf55c0cc2a80c0574596de3423dcd33133fb0cca1b
            asciidoctor/maven/org.jruby/dirgra 0.3 SHA-256/9ee2b48dcdfe0f6261200b81852a3f8c02af702269457c82f485d36d6e078360
            asciidoctor/maven/org.jruby/jruby-base 9.4.14.0 SHA-256/02a0998fb9c3c9dc2c2c0f8a4bd15f8c20bf3709f25813f19dd90d04c8e837fb
            asciidoctor/maven/org.jruby/jruby-stdlib 9.4.14.0 SHA-256/870e93d0bf6b9cc8bb066ffa8a8e060e7c4757961c2116c00c8a133c70723110
            asciidoctor/maven/org.jruby/jruby 9.4.14.0 SHA-256/1a2f867a2d3973989204936cb8fd8db09a9e9c539fc64300b733464478ab6dd7
            asciidoctor/maven/org.jruby/jzlib 1.1.5 SHA-256/97e2d8359168c484113c38837fdd74453577e6ed8575bb35eb65bc117a90b97f
            asciidoctor/maven/org.ow2.asm/asm-analysis 9.7.1 SHA-256/85b29371884ba31bb76edf22323c2c24e172c3267a67152eba3d1ccc2e041ef2
            asciidoctor/maven/org.ow2.asm/asm-commons 9.7.1 SHA-256/9a579b54d292ad9be171d4313fd4739c635592c2b5ac3a459bbd1049cddec6a0
            asciidoctor/maven/org.ow2.asm/asm-tree 9.7.1 SHA-256/9929881f59eb6b840e86d54570c77b59ce721d104e6dfd7a40978991c2d3b41f
            asciidoctor/maven/org.ow2.asm/asm-util 9.7.1 SHA-256/f885be71b5c90556f5f1ad1c4f9276b29b96057c497d46666fe4ddbec3cb43c6
            asciidoctor/maven/org.ow2.asm/asm 9.7.1 SHA-256/8cadd43ac5eb6d09de05faecca38b917a040bb9139c7edeb4cc81c740b713281
            """;

    @TempDir
    private Path root, project, documents;

    @Test
    public void converts_every_document_keeping_its_folder_and_copies_what_it_shows() throws IOException {
        Files.writeString(documents.resolve("index.adoc"), """
                = Example

                See xref:guide/start.adoc[the guide].

                include::_partial.adoc[]
                """);
        Files.writeString(documents.resolve("_partial.adoc"), "Included from a partial.\n");
        Files.writeString(Files.createDirectories(documents.resolve("guide")).resolve("start.adoc"), "= Start\n\nBegin here.\n");
        Files.writeString(Files.createDirectories(documents.resolve("images")).resolve("logo.svg"), "<svg/>\n");

        newExecutor(module -> module).execute();

        Path output = root.resolve("asciidoctor").resolve(AsciidoctorModule.RENDER).resolve("output")
                .resolve(AsciidoctorModule.PROJECT).resolve("docs");
        assertThat(output.resolve("index.html"))
                .as("the document is converted with what it includes")
                .content()
                .contains("Included from a partial.")
                .contains("guide/start.html");
        assertThat(output.resolve("guide/start.html")).as("a document in a folder keeps its folder").exists();
        assertThat(output.resolve("_partial.html")).as("a partial is only included, never converted").doesNotExist();
        assertThat(output.resolve("images/logo.svg")).as("what a document shows is copied beside it").exists();
        assertThat(output.resolve("index.html"))
                .as("the output names neither the time nor the version it was converted with")
                .content()
                .doesNotContain("Last updated");
    }

    @Test
    public void converts_into_the_output_folder_with_the_attributes_it_is_handed() throws IOException {
        Files.writeString(documents.resolve("index.adoc"), "= Example\n\nVersion {product-version}.\n");

        newExecutor(module -> module.output("manual").attributes(new LinkedHashMap<>(Map.of("product-version", "1.2.3"))))
                .execute();

        assertThat(root.resolve("asciidoctor").resolve(AsciidoctorModule.RENDER).resolve("output")
                .resolve(AsciidoctorModule.PROJECT).resolve("manual").resolve("index.html"))
                .content()
                .contains("Version 1.2.3.");
    }

    @Test
    public void fails_on_a_document_that_includes_what_does_not_exist() throws IOException {
        Files.writeString(documents.resolve("index.adoc"), "= Example\n\ninclude::missing.adoc[]\n");

        assertThatThrownBy(newExecutor(module -> module)::execute)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("Unexpected exit code");
    }

    private BuildExecutor newExecutor(UnaryOperator<AsciidoctorModule> configuration) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
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
        executor.addModule("inputs", (inputs, _) -> inputs.addSource("docs", documents));
        executor.addModule(
                "asciidoctor",
                configuration.apply(new AsciidoctorModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .pinning(Pinning.STRICT)),
                "project",
                "inputs");
        return executor;
    }
}
