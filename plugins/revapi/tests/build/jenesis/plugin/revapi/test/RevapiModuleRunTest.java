package build.jenesis.plugin.revapi.test;

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
import build.jenesis.plugin.revapi.RevapiModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RevapiModuleRunTest {

    private static final String PINS = """
            revapi/maven/com.fasterxml.jackson.core/jackson-annotations 2.10.4 SHA-256/9d07ea7ce579a678e7aea61249fa82c46469af9d02c5b5f13e84beb7b0827dbc
            revapi/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.10.4 2.10.4 SHA-256/d888fb6d1477489ad7468cb08d96d8948dd0a6922ce0ad41f54a171d19006f30
            revapi/maven/com.fasterxml.jackson.core/jackson-core 2.13.3 SHA-256/ab119a8ea3cc69472ebc0e870b849bfbbe536ad57d613dc38453ccd592ca6a3d
            revapi/maven/com.fasterxml.jackson.core/jackson-core/pom/2.13.3 2.13.3 SHA-256/3d3c8e4439bf2db43760a61207dfc957ad99a59b752cc8476990b49de00cdefc
            revapi/maven/com.fasterxml.jackson.core/jackson-databind 2.10.4 SHA-256/55312662a420c71508e6159c86aa41c1694c52e89a1b90dc94bcf4358134005e
            revapi/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.10.4 2.10.4 SHA-256/effcef26670ed615feb691b19e7971a8de924920216f3cd183993cde29bb5910
            revapi/maven/com.fasterxml.jackson/jackson-base/pom/2.10.4 2.10.4 SHA-256/77645a51baa64eb13595d1a148fbd3847050e34f0e3c162880c0a2134063bdb0
            revapi/maven/com.fasterxml.jackson/jackson-base/pom/2.13.3 2.13.3 SHA-256/72d67291876c63e1896bc7d8ff799033d3a4b9029020110488af9c97315ed939
            revapi/maven/com.fasterxml.jackson/jackson-bom/pom/2.10.4 2.10.4 SHA-256/b4e95b1851dcbdcf0ea90bc7d6d1e00e6635329b3c8ea171eb591f16a5745c83
            revapi/maven/com.fasterxml.jackson/jackson-bom/pom/2.13.3 2.13.3 SHA-256/df675b83b6caba7602fb47d75d4bb513c2af4c0a6155d7e397c0ec0f34112c75
            revapi/maven/com.fasterxml.jackson/jackson-parent/pom/2.10 2.10 SHA-256/a50db80829c4f897c6d0e7e95472cbb43b0ebece135a68e39c27b8a6fe33e481
            revapi/maven/com.fasterxml.jackson/jackson-parent/pom/2.13 2.13 SHA-256/2bba89978172af1effcb4d143d09921a3f3082ca4dcf122b1ded98bf55b2ad57
            revapi/maven/com.fasterxml/oss-parent/pom/38 38 SHA-256/c83f8f45dfdca8d0b6b3661c60b3f84780f671b12e06f91ad5d1c1a1d1f966e8
            revapi/maven/com.fasterxml/oss-parent/pom/43 43 SHA-256/e5585cc1c37079b2e3817a8997945736f158831844d59d0e4d3a45b27611f926
            revapi/maven/com.networknt/json-schema-validator 1.0.43 SHA-256/7c10b089a973fe15d13fbe3205c30eb751d6770354ffe77643c9d6133073a7bf
            revapi/maven/com.networknt/json-schema-validator/pom/1.0.43 1.0.43 SHA-256/4846a58e50e12a07e17feea441e426077213aacb6d14f2cffc3f2967e7cf577c
            revapi/maven/javax.inject/javax.inject/pom/1 1 SHA-256/943e12b100627804638fa285805a0ab788a680266531e650921ebfe4621a8bfa
            revapi/maven/org.antlr/antlr4-master/pom/4.7.2 4.7.2 SHA-256/ba99cb25d2390f38680c7502842a0ee0959d1e6403e013ccff698fd5856eead1
            revapi/maven/org.antlr/antlr4-runtime 4.7.2 SHA-256/4c518b87d4bdff8b44cd8cbc1af816e944b62a3fe5b80b781501cf1f4759bbc4
            revapi/maven/org.antlr/antlr4-runtime/pom/4.7.2 4.7.2 SHA-256/dc09cba98c25d3c06e4aec516885d4c3af03062ba55f4fe6283fc9cf176a60fb
            revapi/maven/org.apache.commons/commons-lang3 3.5 SHA-256/8ac96fc686512d777fca85e144f196cd7cfe0c0aec23127229497d1a38ff651c
            revapi/maven/org.apache.commons/commons-lang3/pom/3.5 3.5 SHA-256/45e7fbb2c231db903a5d5aadafc636a173a4d54560f78a11ff498028ef9e345e
            revapi/maven/org.apache.commons/commons-parent/pom/41 41 SHA-256/b2877c8016f8b28924c8e90dd5ae40cd11f328d02a1de98d80de22574ab36ec7
            revapi/maven/org.apache.logging.log4j/log4j-api 2.17.1 SHA-256/b0d8a4c8ab4fb8b1888d0095822703b0e6d4793c419550203da9e69196161de4
            revapi/maven/org.apache.logging.log4j/log4j-api/pom/2.17.1 2.17.1 SHA-256/1e2acef3220b29be10ae099728b158b18d943f91a193cc4501bb42f929c1e88a
            revapi/maven/org.apache.logging.log4j/log4j-core 2.17.1 SHA-256/c967f223487980b9364e94a7c7f9a8a01fd3ee7c19bdbf0b0f9f8cb8511f3d41
            revapi/maven/org.apache.logging.log4j/log4j-core/pom/2.17.1 2.17.1 SHA-256/0bbb3bf6d4d22a1bfa3c3c0927c29410af14a0fb26e3b02b93726f5c7e98aafd
            revapi/maven/org.apache.logging.log4j/log4j/pom/2.17.1 2.17.1 SHA-256/967abc024443a9cb096935559af5e9ad6f0ff61375f84b27d440d2fa70806b09
            revapi/maven/org.apache.logging/logging-parent/pom/3 3 SHA-256/763a2ec2b8094d4161deb6c264b1262085b9be30bf3a31c2ce137242e57722a7
            revapi/maven/org.apache/apache/pom/18 18 SHA-256/7831307285fd475bbc36b20ae38e7882f11c3153b1d5930f852d44eda8f33c17
            revapi/maven/org.apache/apache/pom/23 23 SHA-256/bc10624e0623f36577fac5639ca2936d3240ed152fb6d8d533ab4d270543491c
            revapi/maven/org.jboss.shrinkwrap/shrinkwrap-bom/pom/1.0.1 1.0.1 SHA-256/e3876f389154317f448fdc33978bcdae397eddcf47fe555019780853a22b24f1
            revapi/maven/org.jboss/jboss-dmr 1.5.0.Final SHA-256/cbbe302464ff99bc0656be2343958f3eb7a4ffc575e03bb7399fccbb327be6c5
            revapi/maven/org.jboss/jboss-dmr/pom/1.5.0.Final 1.5.0.Final SHA-256/f3fc704495a5e6fe9010d8d6ee2d9f5473a24f4773b292535c05aa88aef28f03
            revapi/maven/org.jboss/jboss-parent/pom/25 25 SHA-256/f662a20adbe6fd575f37976b0a7cec81d2f0a0435dd49086badecc0efc901a0b
            revapi/maven/org.jruby.jcodings/jcodings 1.0.46 SHA-256/13e7fa99093b729fe736f1bf6886e6230065b36bd3dc9117ee14942883e3e515
            revapi/maven/org.jruby.jcodings/jcodings/pom/1.0.46 1.0.46 SHA-256/375abc55cfc82cba3dd9252d067e35a13dfd46807b37dba99ed9e0045a4cd701
            revapi/maven/org.jruby.joni/joni 2.1.31 SHA-256/96c8d65e99d16a2ee48046fa352a972feadbc15420ed576a3c13d8339cdbe1f0
            revapi/maven/org.jruby.joni/joni/pom/2.1.31 2.1.31 SHA-256/9181678fff2f1fba274f27ff3c2e96fb68cb7988bf6a059e7cc5a03db0067412
            revapi/maven/org.junit/junit-bom/pom/5.7.0 5.7.0 SHA-256/35fb15f8d0bee2b5900a22832762366552f9a349c56ced60ba123ce47738ff00
            revapi/maven/org.revapi.classif/classif 0.1.1 SHA-256/56b3a356500b2a0acfa964b960d593361acd6c6324bcd13ffd26802b38b44412
            revapi/maven/org.revapi.classif/classif-dsl 0.1.1 SHA-256/eb62873779b25e8b997172b0e88900ac0835489611d7b24d0521861e6ffee081
            revapi/maven/org.revapi.classif/classif-dsl/pom/0.1.1 0.1.1 SHA-256/1d0d5c68a5ed000ba4dc74b766d9dfa6f8c2e94cc2f593f781faf23caf752896
            revapi/maven/org.revapi.classif/classif-parent/pom/0.1.1 0.1.1 SHA-256/fbedb43a98ea92d6c6cf06719170130f55845b766308a3aeb9019089a458f5c9
            revapi/maven/org.revapi.classif/classif/pom/0.1.1 0.1.1 SHA-256/0f7dc3d6300e1ec834e3f22c97f4d7e3f91beaac15c14514e69939c1e5fb4012
            revapi/maven/org.revapi/revapi 0.15.1 SHA-256/c41a393d7216e56f98d66a57ca964ffceb658849b42ef488b71f89a600beae72
            revapi/maven/org.revapi/revapi-basic-features 0.13.1 SHA-256/9eade0d8c45f2d99b5b39b42b2f63cb15904db6f3583c89c370b78e8825f76e2
            revapi/maven/org.revapi/revapi-basic-features/pom/0.13.1 0.13.1 SHA-256/11b60c00c19ac27ec377c9a980e4f49f3d5ff7659256440a8b716bc43eb4cde3
            revapi/maven/org.revapi/revapi-build/pom/37 37 SHA-256/4214af66979ed210bda89287f94a8de8c704f88bd0ee995d4a1e46d3751a0573
            revapi/maven/org.revapi/revapi-build/pom/47 47 SHA-256/bfb10a3999a7b0340bf9f6d519ad4b0adcfe897e88ae3a331a844d54f66e901c
            revapi/maven/org.revapi/revapi-java 0.28.4 SHA-256/95f9edd7082703037fa229a415bc7aaf14b60a85319c893d1a9cd0e665e3a825
            revapi/maven/org.revapi/revapi-java-spi 0.25.1 SHA-256/8fd32703b52df0e206ff55c4e3b97525cc4cd790693972e1746cdd4230d3a42a
            revapi/maven/org.revapi/revapi-java-spi/pom/0.25.1 0.25.1 SHA-256/6ef646d056040e38b0823385f8d32deb4dc308ba701436797c4f71b8d2c82d65
            revapi/maven/org.revapi/revapi-java/pom/0.28.4 0.28.4 SHA-256/465d7bb63d2708b89561e2a6697507bbd13502d8bc685acb62c28b419c3a65ba
            revapi/maven/org.revapi/revapi-parent/pom/11 11 SHA-256/b5ddbf314a649ad9eac8ef383c26c8ce0db33a06587586f8ec6259bdcf9d6e0e
            revapi/maven/org.revapi/revapi-parent/pom/16 16 SHA-256/bbe20625ffc2a33600be241c3c48f42677ce21686a7432b38f85f4aa1b516544
            revapi/maven/org.revapi/revapi-reporter-file-base 0.5.1 SHA-256/eb8e10d0d8c9e3c9945cdb2315fbc351ee0bb884cb86e1498c10179ae4f00495
            revapi/maven/org.revapi/revapi-reporter-file-base/pom/0.5.1 0.5.1 SHA-256/144c410941b2788d4d38416e23461ef8e0edbc309b5f2ea838e9113cc936330e
            revapi/maven/org.revapi/revapi-reporter-json 0.5.1 SHA-256/2b385bb37d92dd48ea0ecb85269194485dfb4fb5757afe278d8a43e6ce169b62
            revapi/maven/org.revapi/revapi-reporter-json/pom/0.5.1 0.5.1 SHA-256/a3c3baf676a95759c8d7ad248853fe5bc585f31e77be0501a34ddfab48385390
            revapi/maven/org.revapi/revapi/pom/0.15.1 0.15.1 SHA-256/63e78d7d62d9a90148815856973bd98fcfed4791ee0b38dd173d5e0753e120d3
            revapi/maven/org.slf4j/slf4j-api 1.7.5 SHA-256/fe30825245d2336c859dc38d60c0fc5f3668dbf29cd586828d2b5667ec355b91
            revapi/maven/javax.inject/javax.inject 1 SHA-256/91c77044a50c481636c32d916fd89c9118a72195390452c81065080f957de7ff
            revapi/maven/org.slf4j/slf4j-api/pom/1.7.5 1.7.5 SHA-256/afaf8e74019b230d3f56fdd7c93fb1070c0dca34f3d2d5ab5dea9fc616bd5ca4
            revapi/maven/org.slf4j/slf4j-parent/pom/1.7.5 1.7.5 SHA-256/c43bc5a022dbfd9de82be232dffe46208cbc7de12c14385b5da824e331e535bb
            revapi/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            revapi/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            """;

    @TempDir
    private Path root, project, sources, classes;

    @Test
    public void reports_the_types_a_jar_removed_from_its_baseline() throws IOException {
        build();

        newExecutor(module()).execute();

        Path report = root.resolve("revapi").resolve("compare").resolve("output")
                .resolve("reports").resolve("revapi").resolve("revapi.json");
        assertThat(report)
                .content()
                .contains("\"code\" : \"java.class.removed\"")
                .contains("interface javax.inject.Provider<T>");
    }

    @Test
    public void ignores_a_difference_that_revapi_s_own_configuration_ignores() throws IOException {
        build();

        Files.writeString(project.resolve("revapi.json"), """
                [{"extension": "revapi.differences", "configuration": {"ignore": true, "differences": [
                    {"code": "java.class.removed", "justification": "The demo removes them."}]}}]
                """);
        newExecutor(module()).execute();

        assertThat(root.resolve("revapi").resolve("compare").resolve("output")
                .resolve("reports").resolve("revapi").resolve("revapi.json"))
                .content()
                .doesNotContain("java.class.removed");
    }

    @Test
    public void fails_the_build_on_a_finding_when_strict() throws IOException {
        build();

        assertThatThrownBy(newExecutor(module().strict(true))::execute)
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("revapi found")
                .hasMessageContaining("jenesis.strict.revapi is true");
    }

    private RevapiModule module() {
        return new RevapiModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                .pinning(Pinning.STRICT)
                .baseline("javax.inject/javax.inject/1");
    }

    private void build() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path source = Files.createDirectories(sources.resolve("javax").resolve("inject")).resolve("Named.java");
        Files.writeString(source, """
                package javax.inject;

                @Qualifier
                @java.lang.annotation.Documented
                @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
                public @interface Named {
                    String value() default "";
                }
                """);
        Path qualifier = sources.resolve("javax").resolve("inject").resolve("Qualifier.java");
        Files.writeString(qualifier, """
                package javax.inject;

                @java.lang.annotation.Target(java.lang.annotation.ElementType.ANNOTATION_TYPE)
                @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
                @java.lang.annotation.Documented
                public @interface Qualifier {
                }
                """);
        int rc = javax.tools.ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), "--release", "8", source.toString(), qualifier.toString());
        assertThat(rc).as("the sample compiles").isZero();
        Path artifacts = Files.createDirectories(project.resolve(BuildStep.ARTIFACTS));
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(artifacts.resolve("javax.inject.jar")));
             Stream<Path> files = Files.walk(classes)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                out.putNextEntry(new JarEntry(classes.relativize(file).toString().replace(File.separatorChar, '/')));
                Files.copy(file, out);
                out.closeEntry();
            }
        }
    }

    private BuildExecutor newExecutor(RevapiModule module) throws IOException {
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
        executor.addModule("revapi", module, "project");
        return executor;
    }
}
