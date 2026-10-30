package build.jenesis.plugin.spotbugs.test;

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
import build.jenesis.plugin.spotbugs.SpotBugsModule;

import static org.assertj.core.api.Assertions.assertThat;

public class SpotBugsModuleRunTest {

    private static final String PINS = """
            spotbugs/maven/com.fasterxml.jackson/jackson-bom/pom/2.19.1 2.19.1 SHA-256/ba6d68eeab3a1cc13a77a8ade2197ff9a32aa1cffeac72847d49badd82e1b9ce
            spotbugs/maven/com.fasterxml.jackson/jackson-parent/pom/2.19.2 2.19.2 SHA-256/639a2b63dd05da4e381081303985caadfbb7ad9f85b1d2320638f6b11f208365
            spotbugs/maven/com.fasterxml/oss-parent/pom/68 68 SHA-256/25eafd96dae242b6b5a7108f55b2c14015b828daa5abe234289fd6e774a1ce57
            spotbugs/maven/com.github.spotbugs/spotbugs 4.9.6 SHA-256/62a0def31899338200fc9013b4db8a8aedfc3536ca7d70d59038b092dfaa6819
            spotbugs/maven/com.github.spotbugs/spotbugs-annotations 4.9.6 SHA-256/523d394a6b36174ad0a22f0c1c75b105ccff42869a8b7ce86e7fd339ca6f86ce
            spotbugs/maven/com.github.spotbugs/spotbugs-annotations/pom/4.9.6 4.9.6 SHA-256/c4ee9728478d5bc2028f5349451195b1710da1536d96b341aa04414b787dac77
            spotbugs/maven/com.github.spotbugs/spotbugs/pom/4.9.6 4.9.6 SHA-256/10c9bd0d0a557727744ff7f261613023a1321ea7d9d3900ca6acd17150bbd9b4
            spotbugs/maven/com.github.stephenc.jcip/jcip-annotations 1.0-1 SHA-256/4fccff8382aafc589962c4edb262f6aa595e34f1e11e61057d1c6a96e8fc7323
            spotbugs/maven/com.github.stephenc.jcip/jcip-annotations/pom/1.0-1 1.0-1 SHA-256/68c2a5aa6e8f345743093e52b5b9e0190ba4d5a5215c0a59b4d7d33647208cbb
            spotbugs/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            spotbugs/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            spotbugs/maven/com.google.code.gson/gson 2.13.2 SHA-256/dd0ce1b55a3ed2080cb70f9c655850cda86c206862310009dcb5e5c95265a5e0
            spotbugs/maven/com.google.code.gson/gson-parent/pom/2.13.2 2.13.2 SHA-256/83ab528a9d50fd76aeb8ad6f727b3ee9cb766586255774ed16ca8c4c76d9dacd
            spotbugs/maven/com.google.code.gson/gson/pom/2.13.2 2.13.2 SHA-256/3aa06aa7c0f9af092961a42d09578e4324be146348a0ee6ed47857f7c2677b76
            spotbugs/maven/com.google.errorprone/error_prone_annotations 2.41.0 SHA-256/a56e782b5b50811ac204073a355a21d915a2107fce13ec711331ad036f660fcc
            spotbugs/maven/com.google.errorprone/error_prone_annotations/pom/2.41.0 2.41.0 SHA-256/a151df1e2e0b48618d8b06a180748a29b3abb39b1b2396f6a1c879a727488c6e
            spotbugs/maven/com.google.errorprone/error_prone_parent/pom/2.41.0 2.41.0 SHA-256/c538388d760a5c1c98dcf06f6ed3cfe5f11a651827db5cbd2ed8288c795cad42
            spotbugs/maven/jakarta.platform/jakarta.jakartaee-bom/pom/9.1.0 9.1.0 SHA-256/df98e0266219fdbb82562826d79a3a20776a8ba02aa787f0f0765a538654c8a4
            spotbugs/maven/jakarta.platform/jakartaee-api-parent/pom/9.1.0 9.1.0 SHA-256/a7702c487026802784b5797b62330a8b8d6592bf0f4737b25c67a5eac82659c0
            spotbugs/maven/jaxen/jaxen 2.0.0 SHA-256/9499e487a66268f47b8307d130cd1e13a58392105e98a51f6a525db79c615cc5
            spotbugs/maven/jaxen/jaxen-parent/pom/2.0.0 2.0.0 SHA-256/263387fed91cc35813bffcd51efc60d5122d2043124ff49f94e35118169857e2
            spotbugs/maven/jaxen/jaxen/pom/2.0.0 2.0.0 SHA-256/3c507f5b485842374cefa5148b46f99850f692900e16adc22fa04e75340c6dff
            spotbugs/maven/net.sf.saxon/Saxon-HE 12.9 SHA-256/8f3a9216a537367132293eacbba9df062eace8f8b16a184af59e2e4839d4cd41
            spotbugs/maven/net.sf.saxon/Saxon-HE/pom/12.9 12.9 SHA-256/f880e9d1d400c99c1892f5964e5fc920dd1def05622359b9b058f32b5b426975
            spotbugs/maven/org.apache.bcel/bcel 6.10.0 SHA-256/afd26d78e921d5f843f5745c44a6edede5b1f607179d8ac76797a57bcbd430e2
            spotbugs/maven/org.apache.bcel/bcel/pom/6.10.0 6.10.0 SHA-256/88e6f1dbf75f6f551be1fb8633d916b49cd3126fd078338db482dd7519c530d8
            spotbugs/maven/org.apache.commons/commons-lang3 3.18.0 SHA-256/4eeeae8d20c078abb64b015ec158add383ac581571cddc45c68f0c9ae0230720
            spotbugs/maven/org.apache.commons/commons-lang3/pom/3.18.0 3.18.0 SHA-256/aa254b373b6f6d46bc9dca86331b072a8ab86eb25ea9921fd439618392e98a16
            spotbugs/maven/org.apache.commons/commons-parent/pom/71 71 SHA-256/95b7be70f316ae4ca22f6fbdd08de2182e87cd874a650de7c3d3386a747a82a3
            spotbugs/maven/org.apache.commons/commons-parent/pom/85 85 SHA-256/d189ff2c0027e96bb65d31e6f227ed2af966169b36af1e973dd5ba08926dc7b5
            spotbugs/maven/org.apache.commons/commons-text 1.14.0 SHA-256/121fce2282910c8f0c3ba793a5436b31beb710423cbe2d574a3fb7a73c508e92
            spotbugs/maven/org.apache.commons/commons-text/pom/1.14.0 1.14.0 SHA-256/5c80e8144f22bb47dacb642f64460418eb253f0b1ba03eb1413c2f36f0a18805
            spotbugs/maven/org.apache.groovy/groovy-bom/pom/4.0.27 4.0.27 SHA-256/aa44eb52bfdfe61d27b3e450d2b348d88deaa3437ab4d9d49a84095348f9f6fb
            spotbugs/maven/org.apache.logging.log4j/log4j-api 2.25.1 SHA-256/20b9c77c0a9e54d1063a39e551dcaf98c7d8e7a4994648f84d0b9e14c71f7215
            spotbugs/maven/org.apache.logging.log4j/log4j-api/pom/2.25.1 2.25.1 SHA-256/ba9e895291814a1879d3a60c2d4111799324eb8479b1e012a47b351a378831c3
            spotbugs/maven/org.apache.logging.log4j/log4j-bom/pom/2.25.1 2.25.1 SHA-256/b8ac2332849711b2f7474415937bdbb967d2b9300ef4cffe3f35be3b1aa18a4e
            spotbugs/maven/org.apache.logging.log4j/log4j-core 2.25.1 SHA-256/78c232747855464b182f0abf78a99a22c88d4d270ff585343dab55576d7420e2
            spotbugs/maven/org.apache.logging.log4j/log4j-core/pom/2.25.1 2.25.1 SHA-256/9e9b3670e7f0c309971d2dd3437ab111d3c0a6683d3204d60b6aa5e4a8442cb8
            spotbugs/maven/org.apache.logging.log4j/log4j/pom/2.25.1 2.25.1 SHA-256/0ae665f709e13106e8230336f86cd832b81bcad5c05909725c3ff7743dd4ff5d
            spotbugs/maven/org.apache/apache/pom/32 32 SHA-256/cfd872c0ec27f53ae68f43dbc0fecded8add773079a53afbd390e407b42ce72f
            spotbugs/maven/org.apache/apache/pom/35 35 SHA-256/ea297dcd114136e8b8e8b630230d52a76c2fc69f6c5db25d672b1857000728b8
            spotbugs/maven/org.dom4j/dom4j 2.2.0 SHA-256/3fae79e081096e1410645eb3557c63b79ca266d510ab479889511109becd1690
            spotbugs/maven/org.dom4j/dom4j/pom/2.2.0 2.2.0 SHA-256/e7fdd028dc3dc995d7ea7b9b4c4e23c467cc53114f3a322610c4ca46035c49dc
            spotbugs/maven/org.eclipse.ee4j/project/pom/1.0.7 1.0.7 SHA-256/205c039a42cbae3556efbeb04a483eb3a3cf9550bd75bf84260dc8f28218f105
            spotbugs/maven/org.junit/junit-bom/pom/5.11.0-M2 5.11.0-M2 SHA-256/4a3ffc4a4edcfec2cb5d619922706a9407161798571939f854dfda73e4e17cc8
            spotbugs/maven/org.junit/junit-bom/pom/5.13.1 5.13.1 SHA-256/fa68451ea830572ed43ffe51d75b6a05f7a5e665a602a51f49d6be02063a65f3
            spotbugs/maven/org.junit/junit-bom/pom/5.13.2 5.13.2 SHA-256/43b1104fb3fd4ef4b72a9751d41e09c29f001c8be00ff3972238dc169a734b49
            spotbugs/maven/org.junit/junit-bom/pom/5.13.4 5.13.4 SHA-256/d7a08a99b2502f0bb68cd4e1f984f0bf69324aaa208bd0f73366c03fc3548a42
            spotbugs/maven/org.mockito/mockito-bom/pom/4.11.0 4.11.0 SHA-256/d8531a746c988f7f68ed5f188cdea945006aea993ec5df9e524e0d27d61491da
            spotbugs/maven/org.ow2.asm/asm 9.8 SHA-256/876eab6a83daecad5ca67eb9fcabb063c97b5aeb8cf1fca7a989ecde17522051
            spotbugs/maven/org.ow2.asm/asm-analysis 9.8 SHA-256/e640732fbcd3c6271925a504f125e38384688f4dfbbf92c8622dfcee0d09edb9
            spotbugs/maven/org.ow2.asm/asm-analysis/pom/9.8 9.8 SHA-256/c5747e25c72e1b07d5263c75c78ad61a626dd2458fafcafc23f81d3253ee42ed
            spotbugs/maven/org.ow2.asm/asm-commons 9.8 SHA-256/3301a1c1cb4c59fcc5292648dac1d7c5aed4c0f067dfbe88873b8cdfe77404f4
            spotbugs/maven/org.ow2.asm/asm-commons/pom/9.8 9.8 SHA-256/f793e78f01f703717d09472e56cdf212fe298970c882e2116e8e549fb6d140c2
            spotbugs/maven/org.ow2.asm/asm-tree 9.8 SHA-256/14b7880cb7c85eed101e2710432fc3ffb83275532a6a894dc4c4095d49ad59f1
            spotbugs/maven/org.ow2.asm/asm-tree/pom/9.8 9.8 SHA-256/7149e7faa0e191296f879aeed9209c8942d398f0698d2cca1a9c628f2e2a8f77
            spotbugs/maven/org.ow2.asm/asm-util 9.8 SHA-256/8ba0460ecb28fd0e2980e5f3ef3433a513a457bc077f81a53bdc75b587a08d15
            spotbugs/maven/org.ow2.asm/asm-util/pom/9.8 9.8 SHA-256/24d0970e171e2917b83a8f0f05d50a1cdb5c82e508555b52dbcc9d3361c4f0ec
            spotbugs/maven/org.ow2.asm/asm/pom/9.8 9.8 SHA-256/c1367c3bb383d7619e7f797e38df7513885f2eef04ae7b5908f68222657b5baa
            spotbugs/maven/org.ow2/ow2/pom/1.5.1 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
            spotbugs/maven/org.slf4j/slf4j-api 2.0.17 SHA-256/7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832
            spotbugs/maven/org.slf4j/slf4j-api/pom/2.0.17 2.0.17 SHA-256/150c40287f7cecdc21b9380caac98e928c4f33c023db6b348df1c5ac977026bf
            spotbugs/maven/org.slf4j/slf4j-bom/pom/2.0.17 2.0.17 SHA-256/f78d27b642b4b886eb839fc102b5cd9fe7f30f37599ffe68185be4e1609031e9
            spotbugs/maven/org.slf4j/slf4j-parent/pom/2.0.17 2.0.17 SHA-256/95cd71e852dfdb29126e58b7b939d57ec2b2e6024391852e0b545dee082bbf3b
            spotbugs/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            spotbugs/maven/org.springframework/spring-framework-bom/pom/5.3.39 5.3.39 SHA-256/f6d481093e75767cba1ac7e1db38f8f692cbe3e38744693371a75aeb21c6148b
            spotbugs/maven/org.xmlresolver/xmlresolver 5.3.3 SHA-256/1fe4d5b92f708dcdb82dbce12919e0171e6b5ca62c6dca6220483625098feb5f
            spotbugs/maven/org.xmlresolver/xmlresolver/jar/data 5.3.3 SHA-256/b0c487ad2f3e558be8d829c916d2458d10aca6a5bafa7a4d0524b70845e48a5c
            spotbugs/maven/org.xmlresolver/xmlresolver/pom/5.3.3 5.3.3 SHA-256/261ada76d9e6676008dcac377a4d4589749166b606559f59f2e45ea62d8b75ec
            """;
    private static final String VERSION = "4.9.6";

    @TempDir
    private Path root, project, sources;

    @Test
    public void downloads_the_pinned_spotbugs_and_writes_a_report() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve("spotbugs-exclude.xml"), "<FindBugsFilter/>");

        Path source = Files.createDirectories(sources.resolve("sample")).resolve("Sample.java");
        Files.writeString(source, """
                package sample;
                public class Sample {
                    public boolean check(String value) {
                        return value == "expected";
                    }
                }
                """);
        Path classes = Files.createDirectories(project.resolve(BuildStep.CLASSES));
        int rc = javax.tools.ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), "--release", "17", source.toString());
        assertThat(rc).as("the sample compiles").isZero();

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule(
                "spotbugs",
                new SpotBugsModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path resolved = root.resolve("spotbugs").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned SpotBugs version resolves")
                    .anyMatch(name -> name.contains("spotbugs") && name.contains(VERSION));
        }
        Path report = root.resolve("spotbugs").resolve("check").resolve("output").resolve("reports").resolve("spotbugs").resolve("spotbugs-report.xml");
        assertThat(report).isNotEmptyFile();
        assertThat(report).content().contains("ES_COMPARING");
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
