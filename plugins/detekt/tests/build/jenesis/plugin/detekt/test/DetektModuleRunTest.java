package build.jenesis.plugin.detekt.test;

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
import build.jenesis.plugin.detekt.DetektModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DetektModuleRunTest {

    private static final String PINS = """
            detekt/maven/dev.drewhamilton.poko/poko-annotations-jvm 0.16.0 SHA-256/940e6d50445bc6b0ae26ad414ec7b953a3e4e802dc7756cc14d56958bc97cc31
            detekt/maven/dev.drewhamilton.poko/poko-annotations-jvm/pom/0.16.0 0.16.0 SHA-256/9ab43c0fae15b5c62c32a7a1fecda9c784b395c1fd8b890f34d2328d2f3e074e
            detekt/maven/io.github.davidburstrom.contester/contester-breakpoint 0.2.0 SHA-256/672cbebb5d45a72b35dd81fd6127e187451bb6fb7fba35315bbdf2f57cfce835
            detekt/maven/io.github.davidburstrom.contester/contester-breakpoint/pom/0.2.0 0.2.0 SHA-256/be2d22c5019386656299e8f16342c68c52645b747ffb86192908d17b16fec0ae
            detekt/maven/io.github.detekt.sarif4k/sarif4k-jvm 0.6.0 SHA-256/b3ac96dd97acba8318dbe26f6a432d6c6db91c46c780805e8928b8103e5763dc
            detekt/maven/io.github.detekt.sarif4k/sarif4k-jvm/pom/0.6.0 0.6.0 SHA-256/8b90fc3745f008f510886ce707539f6bb71bbdd1b7c9db8fd1db0ae566a1c0af
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-api 1.23.7 SHA-256/93fd0c9cdb22dd97ecfc843d55abfc52c9d9c6a0db32abdf9c85735e1bc8047d
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-api/pom/1.23.7 1.23.7 SHA-256/3828d33951c958b71fcd58bcbffeb6671ce888aebac7d5f1aec306f7819acb80
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-cli 1.23.7 SHA-256/d7ddded00163f57ffc81d740ed6e194d0660eb8c60008af3acc3b9561d907875
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-cli/pom/1.23.7 1.23.7 SHA-256/ca0ca67442cee08d7acd990a671d1cf3ccf76d998d6999cc03d8da076bde1320
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-core 1.23.7 SHA-256/a100da3ff7cc41c9a4ee1cfb6baf875da04714359a518e369673f1896ccd1e01
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-core/pom/1.23.7 1.23.7 SHA-256/aca643706ef7173e2d1090f74852a9b94785f8efb858ca99e718729635f96a72
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-metrics 1.23.7 SHA-256/a3592ec4f927f4101b85782a47b94a19aec9c1c75a9054430d8898190d6d0065
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-metrics/pom/1.23.7 1.23.7 SHA-256/c9887ea965a4be74bdc168301ae0bd570523e5a4c269cd24f8be2544c783ebdf
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-parser 1.23.7 SHA-256/e217ab883e2e0f0e8a69b1fce7a131ff93a8bf2e06bd5fd395b38cde6b9c9504
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-parser/pom/1.23.7 1.23.7 SHA-256/2599cf2988bfa4d00164967126988ddc3b1fd7819ffd598065caccd0ca1af55a
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-psi-utils 1.23.7 SHA-256/6a054c91da27aac77db3ed509ff9f408c45410e1a5e989dc380aae3c09d7fb6e
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-psi-utils/pom/1.23.7 1.23.7 SHA-256/13779877c6c0d4ee1d90c9dc66b047d496b656b90058f1701d190db0ad59dc9c
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-html 1.23.7 SHA-256/bd0dcd0cccb72fe86f665c700d9c95dbfae33db35c09fc331b71c1896c8737fc
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-html/pom/1.23.7 1.23.7 SHA-256/33795541d32becf15457f0c4d3eb8fc2983e95c582beb1c4a310ee373d793b2d
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-md 1.23.7 SHA-256/7c3ad77113fb8821b5adde900d318bb39d4d2361eae30f0de2adbe8fea2ea648
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-md/pom/1.23.7 1.23.7 SHA-256/d60a4d235a51377651063d133465afb4fc81681203b85c11b37c932e5e74995d
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-sarif 1.23.7 SHA-256/0bb1ecf6dd43758d193d99dd02333da3afc2bfc21238139fe319f586714b9b1d
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-sarif/pom/1.23.7 1.23.7 SHA-256/c23aa72586b1a3b834304f5710c7e2286300e1c7966da644d34a9a98173fb174
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-txt 1.23.7 SHA-256/34a7bee9da3c414bb69547b72ceaeab799b98a63b082b2c826460b5421c64c70
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-txt/pom/1.23.7 1.23.7 SHA-256/11bd70b50affdf7d48c0fd75b90319153e781008038dee6e5e340459d4ce9a4c
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-xml 1.23.7 SHA-256/c4b78909c4ef03f0dc1bbd807207aa4d75524f4a29e747ffeb4997d0f7c8e09b
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-report-xml/pom/1.23.7 1.23.7 SHA-256/b9a93e2d3347be41d4e0c4c36fc7dc74d67f6190301e811eceff274c9f7aa989
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules 1.23.7 SHA-256/b94ae25b9bc5c99bb1b9b289ea9b8138e220428f92d74d2216bc278b6cfa5e46
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-complexity 1.23.7 SHA-256/d8f9098f42548785ea26bd08fd23551104fe4357be1e00ad567b626f86766d66
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-complexity/pom/1.23.7 1.23.7 SHA-256/bd87103d3bf208a897116b6c641061aef72582a50ad6ff1508255ca09468518f
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-coroutines 1.23.7 SHA-256/c5123c9fbcbe4ed494ea8826ec3102285a4e479e1c0ddbb8071bbe4984524402
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-coroutines/pom/1.23.7 1.23.7 SHA-256/db62343db56193f9f98ac94dde16b3f5a94802d95fd57679c04006b2af31aefe
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-documentation 1.23.7 SHA-256/864f1e4dc8c605bfbb894ed67402ded9a23b1ac17c73c6aaf8e8f0514c3c3962
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-documentation/pom/1.23.7 1.23.7 SHA-256/0bce2157177af12d3a87e7c6b60d89980419cd12b9fa9492baef6ea50ba625c9
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-empty 1.23.7 SHA-256/abe9a00622e63d6c9123a57b79281e3e951d3a6498ddc758564bbdfeb988f0ff
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-empty/pom/1.23.7 1.23.7 SHA-256/89db28aba6c1c9b68eb75b8ac9186e9b628467349025d478d9ea94be3e9f455b
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-errorprone 1.23.7 SHA-256/431f016bbf3275a5ba6c7b9c6090f63b878df3e801b05190939cc2863402bf04
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-errorprone/pom/1.23.7 1.23.7 SHA-256/c8d75b8ee9d3384ce56ff924c4d827ceaaa790b70ab3411f157e8518642b908f
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-exceptions 1.23.7 SHA-256/02c715b2107a7223f203405bb8e4b5b418c3643e264029c750e7a3e504c1be78
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-exceptions/pom/1.23.7 1.23.7 SHA-256/dc8b3837893b35460586666030ac90a4ce22097bb07d0014f2d901c4841cb7df
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-naming 1.23.7 SHA-256/c99c026cc45578703353b9eb2108b8bc1cd445170cabb3057163f45e53926886
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-naming/pom/1.23.7 1.23.7 SHA-256/c6d70422b31eaaef5d8f84d650111686b01b8057c7b158f6a7df5ad7ddcd1e8a
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-performance 1.23.7 SHA-256/c00fd0ae45b4c35fed4c3b180666eda9b5eeb1912b6d27efead0d2926c93920a
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-performance/pom/1.23.7 1.23.7 SHA-256/aae28f4a2cb21134041f6ad88d98cceedcc15e63ab433793c86987c1cee26d48
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-style 1.23.7 SHA-256/9effb12bece270a623c6719f57c0e93547d3c5bf00b221e77dd461aab9981169
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules-style/pom/1.23.7 1.23.7 SHA-256/dac243f632f4d8a868bab33990c4b465db1a081d9c193ae96afd52a2a6d4350e
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-rules/pom/1.23.7 1.23.7 SHA-256/4d9e0551cd5bc9a81e3563bf2b44b577041a41b2f1688e7655c38b56406192d4
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-tooling 1.23.7 SHA-256/7a148ecfd983ee258a67eb8bb194bda7d628a3b85fa9283a29c5b1d4a7e9d02d
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-tooling/pom/1.23.7 1.23.7 SHA-256/6d28aa9f02228bb5441ff4f2159a249ea984afccc2a3fc908b04df0913b333a9
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-utils 1.23.7 SHA-256/0b5c211c72f63985a3e2e0f12f9a7733c7b3411b983a1c7f3175f3bd68d72194
            detekt/maven/io.gitlab.arturbosch.detekt/detekt-utils/pom/1.23.7 1.23.7 SHA-256/b332aad78e73315a8c3abaf9690b0e033e1e1f062cc541c8725dfde55a2ee1fb
            detekt/maven/org.jcommander/jcommander 1.85 SHA-256/fa7552d2831a2b20778d86851d093edca68fbc0a77f792b6223110e4fae67a70
            detekt/maven/org.jcommander/jcommander/pom/1.85 1.85 SHA-256/e61401cfd1e4f20e2a4048872e45eeee7716a1d6f99f668a4777122dd2882c48
            detekt/maven/org.jetbrains.intellij.deps/trove4j 1.0.20200330 SHA-256/c5fd725bffab51846bf3c77db1383c60aaaebfe1b7fe2f00d23fe1b7df0a439d
            detekt/maven/org.jetbrains.intellij.deps/trove4j/pom/1.0.20200330 1.0.20200330 SHA-256/87721cbaa65a3c97d8b1ba9d207840f164c9fe38759fc9ea10ffe26565f8d3e9
            detekt/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable 2.0.10 SHA-256/ea76850ffe3b84c92654a73f8e366964857f7e95687ca158882241cebef591e8
            detekt/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom/2.0.10 2.0.10 SHA-256/c453b7cc1f761671108743c5685b5e1cd51aebbfca955c40c06071ea6042135b
            detekt/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable 2.0.10 SHA-256/a500d6f7bb2152a8387a9e24b886d0654fdbdb6662ec264c544ce9346f186141
            detekt/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom/2.0.10 2.0.10 SHA-256/0c50d6b3ead788ce77a765cf0c96bc970ac3b7d57a6d6636769fcdfedbb392b3
            detekt/maven/org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
            detekt/maven/org.jetbrains.kotlin/kotlin-reflect/pom/1.6.10 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
            detekt/maven/org.jetbrains.kotlin/kotlin-script-runtime 2.0.10 SHA-256/58f4f7ad99a4a045964b44fe55f0b2604d2c5f51ff4d97c7e6817983fdf92ea7
            detekt/maven/org.jetbrains.kotlin/kotlin-script-runtime/pom/2.0.10 2.0.10 SHA-256/916ea02b4d530a530284e493d1df4d6fbd780bff91e92489fdfd872a14319b92
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib 2.0.10 SHA-256/60f1cefbf1c101676abc9f413f569dad464929d7dc63d5e70e15ae26986e08ac
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-common 1.7.20 SHA-256/e0e91962bc0007338bf5b1739f62927ac32d14ba3d827fa608ab4e5351729d5d
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-common/pom/1.7.20 1.7.20 SHA-256/1862fe818a7e7bb8d0a7c031aea238b59d549453b10859ce07638c9f96ff4885
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.7.20 SHA-256/524da3c1a2ad56fd52c4ae2272ef3de421de8d2047ab1c51fc306d351243f2f5
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7/pom/1.7.20 1.7.20 SHA-256/3b91f670de0b0f4c78549a2f5e60b57548a33ab6416b1054ef63865d6c62ae95
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.7.20 SHA-256/1da0d306c995945e1f807240ef64b5cd2dd5ac58612afb1a8596143d10b7ded5
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8/pom/1.7.20 1.7.20 SHA-256/491ffbc948a3477491a7fbd43a368c5731cfafe6432af80783b2bbfe67e09fa7
            detekt/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.0.10 2.0.10 SHA-256/b8e465f28c2458135b2a31ed4dfffea1b04351c5bd8adb3e3213a9b89855e0c4
            detekt/maven/org.jetbrains.kotlinx/kotlinx-html-jvm 0.8.1 SHA-256/98bda1c78a5028a134ceb25b63f5c130c89349730d35fd47ef7490b6bf0b63b3
            detekt/maven/org.jetbrains.kotlinx/kotlinx-html-jvm/pom/0.8.1 0.8.1 SHA-256/a4a8317010c319a6c777176bef6319b32cc96e16b484394e26a2cbf8571ffd04
            detekt/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm 1.4.1 SHA-256/eba7f1c854296e4ce1418fb01360f8f10c5683e7c45aa3472018417a067636f3
            detekt/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm/pom/1.4.1 1.4.1 SHA-256/6e522e0a863ead95d7f2732e00f6f45bf302ea32e09be88c64d060c07655f832
            detekt/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm 1.4.1 SHA-256/af604c46737121d4225fdb60ef0e17766a3c94b7c1c9ef76b4e3a5c7733d557e
            detekt/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm/pom/1.4.1 1.4.1 SHA-256/32d9347c710135fbe70a70192857bc73b06959962edecfff31926039be24b861
            detekt/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            detekt/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            detekt/maven/org.snakeyaml/snakeyaml-engine 2.7 SHA-256/4053f878c171692aab8782f53a3974f43e55e2b6ed12c3682b36a46968c5ded1
            detekt/maven/org.snakeyaml/snakeyaml-engine/pom/2.7 2.7 SHA-256/a52a6243f8615856791d1297f97374076bf81348061af714ad0963d375ae5cb7
            """;
    private static final String VERSION = "1.23.7";
    private static final String CONFIG = """
            style:
              active: true
              MaxLineLength:
                active: true
                maxLineLength: 10
            """;

    @TempDir
    private Path root, project;

    @Test
    public void downloads_the_pinned_detekt_and_writes_a_report() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("detekt.yml"), CONFIG);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.kt"), """
                package sample
                fun greetTheEntireWorld(): Int = 42
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "detekt",
                new DetektModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path resolved = root.resolve("detekt").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned detekt version resolves")
                    .anyMatch(name -> name.contains("detekt-cli") && name.contains(VERSION));
        }
        Path report = root.resolve("detekt").resolve("check").resolve("output").resolve("reports").resolve("detekt").resolve("detekt-report.xml");
        assertThat(report).isNotEmptyFile();
        assertThat(report).content().contains("MaxLineLength");
    }

    @Test
    public void fails_a_report_only_run_when_detekt_cannot_load_its_configuration() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("detekt.yml"), """
                style:
                  active: [
                """);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.kt"), """
                package sample
                fun greet(): Int = 42
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "detekt",
                new DetektModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");

        assertThatThrownBy(executor::execute)
                .rootCause()
                .as("report-only covers findings, not a Detekt that never ran")
                .hasMessageContaining("Unexpected exit code");
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
