package build.jenesis.plugin.codenarc.test;

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
import build.jenesis.plugin.codenarc.CodeNarcModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CodeNarcModuleRunTest {

    private static final String PINS = """
            codenarc/maven/com.github.javaparser/javaparser-core 3.23.0 SHA-256/1d732f1b057b9e2ea69935180b5a6abf2c776f5e311a96651c3c03b0616032cc
            codenarc/maven/com.github.javaparser/javaparser-core/pom/3.23.0 3.23.0 SHA-256/2e0749dfa06045e7a33aa99497e521095c22a6ec44ab96101711ecff8cb0b036
            codenarc/maven/com.github.javaparser/javaparser-parent/pom/3.23.0 3.23.0 SHA-256/a232a00e8a38efcff83af0148a2a992375a45c9811288a4e80baab890525b0ca
            codenarc/maven/com.thoughtworks.qdox/qdox 1.12.1 SHA-256/21fba22f830e9268f07cf4ab2d99e8181abbdcb0cb91ee0228eb3cb918dcdd1d
            codenarc/maven/com.thoughtworks.qdox/qdox/pom/1.12.1 1.12.1 SHA-256/c52a6616d04efb30c9edeab9ee82b0591e4d04d38e030bf81e250776552d215d
            codenarc/maven/org.apache.ant/ant 1.10.11 SHA-256/88c0b89bbbaae01e0d9fcae93be792f5abbe3409106f8eee858fdf365dbc0754
            codenarc/maven/org.apache.ant/ant-antlr 1.10.11 SHA-256/ff253b3699f859115378c0c4322c94d591499eac97285942722f53a7e85182f1
            codenarc/maven/org.apache.ant/ant-antlr/pom/1.10.11 1.10.11 SHA-256/bdaf1fdc91f6c504e4067ddbe021b2e5780612e7f9395b1676bb8fee7c3ef251
            codenarc/maven/org.apache.ant/ant-junit 1.10.11 SHA-256/02267c1233cde1bc196f5aed40b6b8b61b79c47c2f6729ae1acf24ebfe89e815
            codenarc/maven/org.apache.ant/ant-junit/pom/1.10.11 1.10.11 SHA-256/01e5b3e8f12c1cd40a5426b5a5166affbebec789c841f7a1e8f1b81e5dd7e38b
            codenarc/maven/org.apache.ant/ant-launcher 1.10.11 SHA-256/dab530df7a980b5ac8fd7e8d208243ae0d3ebd6de09b1aa2ce756360cc2ed256
            codenarc/maven/org.apache.ant/ant-launcher/pom/1.10.11 1.10.11 SHA-256/ed2a0688261beb6e08ec54b38312f1d482ccf1a3b863c47d28d5b90a446d1c1e
            codenarc/maven/org.apache.ant/ant-parent/pom/1.10.11 1.10.11 SHA-256/57a0532682f30fa307416a225929d5710acda72d7b25ee08caf98dc82cd35db6
            codenarc/maven/org.apache.ant/ant/pom/1.10.11 1.10.11 SHA-256/c22894d9cb46abf5cebf6eeb2bccfe4d78e18eeea311a0ea6addd59dfb4b1fe3
            codenarc/maven/org.apache.groovy/groovy 4.0.23 SHA-256/b26ee90507fecda8c6da6d3fdbeb8b2c99979ac8b8aa2459a4813e6bee7ae6e6
            codenarc/maven/org.apache.groovy/groovy-bom/pom/4.0.23 4.0.23 SHA-256/f87690727ad21a30c7409791a29c3b5a5d5bea961a87d139d899d30ab42521a8
            codenarc/maven/org.apache.groovy/groovy/pom/4.0.23 4.0.23 SHA-256/e906565a50f99e9b65061dfc0f6ccb6242ee427f1222fbc8d383b6ac28dd835d
            codenarc/maven/org.codehaus.groovy/groovy 3.0.9 SHA-256/77bf86897f295f8cae2e1f46b1eca109f487ba81b66ef24a2b6dcba1eb7d6ce7
            codenarc/maven/org.codehaus.groovy/groovy-ant 3.0.9 SHA-256/b22c2f153a67af2abbbbccf768cfb333181c7d089aee113b36d8ef04e5ae8581
            codenarc/maven/org.codehaus.groovy/groovy-ant/pom/3.0.9 3.0.9 SHA-256/ed21d8d3e724fd1ba7ba8407bcd8f901c2cde80c19877c6a3898280dc8ef8fca
            codenarc/maven/org.codehaus.groovy/groovy-docgenerator 3.0.9 SHA-256/861647dfbd80077090c0b9eb14a695f25eaf02ed422e0b12daf5473ac2cb9609
            codenarc/maven/org.codehaus.groovy/groovy-docgenerator/pom/3.0.9 3.0.9 SHA-256/578d2e273f4ae5663e4b0ed9675c66ac75f40f75e517ecb20271b80789b0c613
            codenarc/maven/org.codehaus.groovy/groovy-groovydoc 3.0.9 SHA-256/73710df42537355e9831add7cdc4393e036f040b1ae5f1927c25635e8f8baeee
            codenarc/maven/org.codehaus.groovy/groovy-groovydoc/pom/3.0.9 3.0.9 SHA-256/6125b22961eb90248df2bfb5d66168d053f1074dd196485df3e511d735f02d0f
            codenarc/maven/org.codehaus.groovy/groovy-json 3.0.9 SHA-256/fb8edd9f367e0a5debc03f31523755a95e9eedae8bd1c0e9df1493188e350263
            codenarc/maven/org.codehaus.groovy/groovy-json/pom/3.0.9 3.0.9 SHA-256/b793fc6dec9700cd85f6c4335429db178f8643b7769f511db713a81a21c8cabf
            codenarc/maven/org.codehaus.groovy/groovy-templates 3.0.9 SHA-256/6188c3be1f2db4c562976339464847345c38ae43136c9334c9e2c027c6f38059
            codenarc/maven/org.codehaus.groovy/groovy-templates/pom/3.0.9 3.0.9 SHA-256/0a3c8363af61d5e55beb9aa93b27cbabe27ca67fce3eee6b4465888c2c89f482
            codenarc/maven/org.codehaus.groovy/groovy-xml 3.0.9 SHA-256/2bcecd848caed3975358a5c556aa49154b2d66f0c8a5c17043dd7b7d8985b6fe
            codenarc/maven/org.codehaus.groovy/groovy-xml/pom/3.0.9 3.0.9 SHA-256/148cf4b57b6aa3feb5259a3bf25b2884a6c06b313fc58838d76acd0ba2d3da2d
            codenarc/maven/org.codehaus.groovy/groovy/pom/3.0.9 3.0.9 SHA-256/3e26864c233e00441d0eadfd82fb95abbcbec516b8e9512bc5c5e94be846dcff
            codenarc/maven/org.codehaus/codehaus-parent/pom/4 4 SHA-256/6b87237de8c2e1740cf80627c7f3ce3e15de1930bb250c55a1eca94fa3e014df
            codenarc/maven/org.codenarc/CodeNarc 3.5.0 SHA-256/444e61901aa646d9a980e4c12197e2d1c859e3b7a0c6737dfe80f67725282a27
            codenarc/maven/org.codenarc/CodeNarc/pom/3.5.0 3.5.0 SHA-256/4e098e5fc21bfa1bf927a7482b7c66191a77c5f78f9f20ab3cce5f4da7aed6f8
            codenarc/maven/org.gmetrics/GMetrics 2.1.0 SHA-256/2aa46be0478c46e921b2d9e699ae408ec82b5fc2516abecf56c42682ee2914b3
            codenarc/maven/org.gmetrics/GMetrics/pom/2.1.0 2.1.0 SHA-256/a3eb7616fa5a29fafb1ccd7ebaa32252e7dd92819873212157e8dc726ae13dd7
            codenarc/maven/org.slf4j/slf4j-api 1.7.35 SHA-256/84cbd60deaf9e18db8cb181e43db4e63f7de353cfcaf654a76d85b22da4d2762
            codenarc/maven/org.slf4j/slf4j-api/pom/1.7.35 1.7.35 SHA-256/2fb6434ed1c82e4ac78cd80043800b2bdb80a5c73aa65fc6b7de5fd5e42fd409
            codenarc/maven/org.slf4j/slf4j-bom/pom/2.0.16 2.0.16 SHA-256/0566048ec825cdf28758620af64d9e14ae38a9cd8748dd6fafa6df4a4194c279
            codenarc/maven/org.slf4j/slf4j-parent/pom/1.7.35 1.7.35 SHA-256/0912e868d5183a2de49dd5f53942531d294b2c05c24e838ca5ecda8bf0182389
            codenarc/maven/org.slf4j/slf4j-parent/pom/2.0.16 2.0.16 SHA-256/09a0b4cc814d7274616c9b16d4c0fd9aaf1ecc813334de5131e547271b1982e5
            codenarc/maven/org.slf4j/slf4j-simple 2.0.16 SHA-256/effc32018658bea09d1e08c7d1060ccad46c086960f583d07dd7ffe9c1172a47
            codenarc/maven/org.slf4j/slf4j-simple/pom/2.0.16 2.0.16 SHA-256/cf6c553148e8a77951cfda131d209481981fd6e2c02a7b48dc6d6605523cdf1c
            """;
    private static final String CONFIG = """
            <ruleset xmlns="http://codenarc.org/ruleset/1.0">
                <rule class="org.codenarc.rule.basic.EmptyIfStatementRule"/>
            </ruleset>
            """;

    @TempDir
    private Path root, project;

    @Test
    public void downloads_the_pinned_codenarc_and_writes_a_report() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("codenarc.xml"), CONFIG);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.groovy"), """
                package sample
                class Sample {
                    void run() {
                        if (true) { }
                    }
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "codenarc",
                new CodeNarcModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path resolved = root.resolve("codenarc").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned CodeNarc version resolves")
                    .anyMatch(name -> name.contains("CodeNarc") && name.contains("3.5.0"));
        }
        Path report = root.resolve("codenarc").resolve("check").resolve("output").resolve("reports").resolve("codenarc").resolve("codenarc-report.xml");
        assertThat(report).isNotEmptyFile();
        assertThat(report).content().contains("EmptyIfStatement");
    }

    @Test
    public void fails_a_report_only_run_when_codenarc_cannot_load_its_configuration() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("codenarc.xml"), """
                <ruleset xmlns="http://codenarc.org/ruleset/1.0">
                    <rule class="org.codenarc.rule.basic.NoSuchRule"/>
                </ruleset>
                """);
        Path sampleDir = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sampleDir.resolve("Sample.groovy"), """
                package sample
                class Sample {
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "codenarc",
                new CodeNarcModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");

        assertThatThrownBy(executor::execute)
                .rootCause()
                .as("report-only covers findings, not a CodeNarc that never ran")
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
