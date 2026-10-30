package build.jenesis.plugin.ktlint.test;

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
import build.jenesis.plugin.ktlint.KtlintFormatModule;

import static org.assertj.core.api.Assertions.assertThat;

public class KtlintFormatModuleRunTest {

    private static final String PINS = """
            ktlint-format/maven/ch.qos.logback/logback-classic 1.3.14 SHA-256/f9b07a6dba4df3899381df7e597df450134e1879b1f3a757456b3cd1c8d94e2f
            ktlint-format/maven/ch.qos.logback/logback-classic/pom/1.3.14 1.3.14 SHA-256/232ae1c31f05e90ae4e4bba1846492318de1519d60d2b873b43a18f6c261a3a2
            ktlint-format/maven/ch.qos.logback/logback-core 1.3.14 SHA-256/9f53159af18a9d438bc398c970db3bb7e17ddb07b04bbb3b01dfe3454dd18862
            ktlint-format/maven/ch.qos.logback/logback-core/pom/1.3.14 1.3.14 SHA-256/f5f0e306475d0b9b6a055e130b1a0c4fb9d222034c93f0d1dd35461e3bc2e1da
            ktlint-format/maven/ch.qos.logback/logback-parent/pom/1.3.14 1.3.14 SHA-256/796dcc4cbfd42aeb7f72432445f5364619678928242b9f3e4749d4c4e0dd9dc4
            ktlint-format/maven/com.github.ajalt.clikt/clikt-core-jvm 5.0.2 SHA-256/18b2df30a3395a37823ec432375a15b485d292b6575e30ef184cc9e7c07d8b87
            ktlint-format/maven/com.github.ajalt.clikt/clikt-core-jvm/pom/5.0.2 5.0.2 SHA-256/394338ffe9b64c1e1a0f3376e16e135e397c1f543ce89a7c1b820847a69db1f4
            ktlint-format/maven/com.github.ajalt.clikt/clikt-jvm 5.0.2 SHA-256/0835107b51d57214d5a2e15d4b214fdd66b3eee5bb3acf96ff35b23bd4a36cde
            ktlint-format/maven/com.github.ajalt.clikt/clikt-jvm/pom/5.0.2 5.0.2 SHA-256/4aba9eb477cbf4abee42903bdab793ce8e9858a56915d327df02b3b31c248c57
            ktlint-format/maven/com.github.ajalt.colormath/colormath-jvm 3.6.0 SHA-256/59f741adfe62053066782d8b1a45afd06685a4bc64b33277e54876b993ed885c
            ktlint-format/maven/com.github.ajalt.colormath/colormath-jvm/pom/3.6.0 3.6.0 SHA-256/f03c35d505110d0673345f4740e55bcd976a9a9fa5a1b13caa2ad398f3bc1e5d
            ktlint-format/maven/com.github.ajalt.mordant/mordant-core-jvm 3.0.1 SHA-256/9cf9b46d1f49f2d6cf2635462b29dc59e0c29b1fb2f085b3312888bbe9c7cd31
            ktlint-format/maven/com.github.ajalt.mordant/mordant-core-jvm/pom/3.0.1 3.0.1 SHA-256/d5896de5e34a9d56ab2786398b260724b181f39cc0508cbb2a1f7e886cd25d87
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm 3.0.1 SHA-256/9ed3b976fcccc78da746d49866fa8ebb8f10530a93c544ea0420259a607dd95e
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-ffm-jvm 3.0.1 SHA-256/2041c2f5f7b87095b115cb19155ee9257451854d08e5b8671165b43bc7e114cf
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-ffm-jvm/pom/3.0.1 3.0.1 SHA-256/6b36e7661ad828de03a28992d8ae96256ceadf3fda128f8ec489a8d65bbbac53
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-graal-ffi-jvm 3.0.1 SHA-256/6dd4bebc164aeacddacc8f98e8f871e00fd21ce7bc2eb0d18230ea83bddfc86a
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-graal-ffi-jvm/pom/3.0.1 3.0.1 SHA-256/0a6f792f1c936095f8746991d64da8b3ff8409acde3948abe5de2fd56888783a
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-jna-jvm 3.0.1 SHA-256/41063442c8891b2774536a9b87a5062a7fd20e6f1949974c6da72f49472d6f4d
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm-jna-jvm/pom/3.0.1 3.0.1 SHA-256/fe15a8c64b47e07f2f99d883286e4efb14746245658746b2550f6f9688645f80
            ktlint-format/maven/com.github.ajalt.mordant/mordant-jvm/pom/3.0.1 3.0.1 SHA-256/b009da4c37db8e1736b8482b44f209d82771ff1c8d3bdf946eb684d3b9ce9965
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli 1.5.0 SHA-256/df984c24eecebc2c0bc5f20c4676dbbb341795dccf0e1820052157022da0e6df
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-baseline 1.5.0 SHA-256/e1cb88427d52cbb398b555ae90854d4a56581daf149abdf81e318b165448aae1
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-baseline/pom/1.5.0 1.5.0 SHA-256/9e78e2347765719b13b1474b1286a2cf755e1c9f3cfec7c4b7e84dab672464b1
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-checkstyle 1.5.0 SHA-256/be3359300acb8c12bea8e971388d28c2c9d08cc2d4f972bf6532de1bf4a1f024
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-checkstyle/pom/1.5.0 1.5.0 SHA-256/93182b2723f919d524dc390ef45ec6284165b785f58a0cd24eec2c3df8206723
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-core 1.5.0 SHA-256/cc3f47e736051f36ad6525931c057971a158bd4c39b6d34fc03cd328ae0c5746
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-core/pom/1.5.0 1.5.0 SHA-256/4d7c081a0910b1249318e2545b43a89bc87f4ee3eb3625c1d280f4e5f2a18c8e
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-format 1.5.0 SHA-256/0ce520848775801dba2413e58903ca5efe0084698228e4e57c6dfa7350093543
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-format/pom/1.5.0 1.5.0 SHA-256/ab04cfe0aa815142a14e5958af954eada4a91511b82af43c51f94c66e0e16bbe
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-html 1.5.0 SHA-256/5b666af72b28e9a3ad043049fb2158c83a39e2224f0674491b899b0ead174331
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-html/pom/1.5.0 1.5.0 SHA-256/9e7b8393851e403f3f1bae10a9f112f40698ab95089846d40a9428427b9e4c17
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-json 1.5.0 SHA-256/dcb8a9109b452f0934cf04d56b0e98031595be6cad062ff61ed4c0c6c703c519
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-json/pom/1.5.0 1.5.0 SHA-256/a788592955efc88eeca8b6094a64c5f27746785aaabbb76eb2544d977b16b969
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain 1.5.0 SHA-256/bd664d42947e1006ff402364ba53a20d2221b83cd666c74da7969d09858af265
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain-summary 1.5.0 SHA-256/a2375aa50052093c40a0fce8d094f27e36a11d04526163035aec49d149a733d3
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain-summary/pom/1.5.0 1.5.0 SHA-256/13dbcdaf31e1288bb67ec96c70bf1b394ebf78e7b8e34b265792a6ee1881a4cc
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain/pom/1.5.0 1.5.0 SHA-256/9f631ad188dc6bbcb154325c9eba62a7b773f343034ff6eab7e1cb5cd281e1b2
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-sarif 1.5.0 SHA-256/a6e2721d202363a8c4b609255842e615844bfe42d45febba9a4d5bb980331742
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-reporter-sarif/pom/1.5.0 1.5.0 SHA-256/f2f4d6c2552269c8f761a1db46f42a71704435255a2ecbc1f210f8ae12f6bb22
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-ruleset-core 1.5.0 SHA-256/64d39075bdeda74782fc5c1ba95284c116f336e5606f4d1c645012d003224e1b
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli-ruleset-core/pom/1.5.0 1.5.0 SHA-256/bc2d4a69097a8afe8b709cbdff59dfde01f186caea1d8904e845bf0b52b6e633
            ktlint-format/maven/com.pinterest.ktlint/ktlint-cli/pom/1.5.0 1.5.0 SHA-256/204848df4971e9d8fac62e00f84405438383f7acbe3f0836ae641abd25d9c6dd
            ktlint-format/maven/com.pinterest.ktlint/ktlint-logger 1.5.0 SHA-256/c067c999baa41a68041ba04f9cbfa4c53ba5320eaaddc8dd3d29e2401463c202
            ktlint-format/maven/com.pinterest.ktlint/ktlint-logger/pom/1.5.0 1.5.0 SHA-256/5e8f1f93dc77193b0b938cd7d6c351d1544cc11a9b48942367edb817c6627b0d
            ktlint-format/maven/com.pinterest.ktlint/ktlint-rule-engine 1.5.0 SHA-256/7cdaee5290f8c24eff02aa7e831d98d9cb584176854376bca2c6977ab0f466be
            ktlint-format/maven/com.pinterest.ktlint/ktlint-rule-engine-core 1.5.0 SHA-256/750cf41707927f59290d07191dacad418e101f468dbfb3b0a9f29cae2af75903
            ktlint-format/maven/com.pinterest.ktlint/ktlint-rule-engine-core/pom/1.5.0 1.5.0 SHA-256/ca17592160c390f870a3da24da20eb1461675f8f5753d134907faa6b725f1d22
            ktlint-format/maven/com.pinterest.ktlint/ktlint-rule-engine/pom/1.5.0 1.5.0 SHA-256/fb8f01854192bf38dc41072233a8cb6816c693c0b274edcdb39ae92d2c2c4bc4
            ktlint-format/maven/com.pinterest.ktlint/ktlint-ruleset-standard 1.5.0 SHA-256/ecae0c3356612244a8e0d901fe431c4a25a1edf5d7c674783b947511ebd167a8
            ktlint-format/maven/com.pinterest.ktlint/ktlint-ruleset-standard/pom/1.5.0 1.5.0 SHA-256/f87b9b9b206471f92fa0fbeeed83b989fc7913ff7b9bb409103fc75602ac9d9a
            ktlint-format/maven/dev.drewhamilton.poko/poko-annotations-jvm 0.18.0 SHA-256/ae01637db8e38af0d1848595aa64c8cda77e980cdab737e45d3838e218c540a6
            ktlint-format/maven/dev.drewhamilton.poko/poko-annotations-jvm/pom/0.18.0 0.18.0 SHA-256/410a3f0fe60173352521ec62ee4167af01115adcd05ecdb4f44349643669563c
            ktlint-format/maven/io.github.detekt.sarif4k/sarif4k-jvm 0.6.0 SHA-256/b3ac96dd97acba8318dbe26f6a432d6c6db91c46c780805e8928b8103e5763dc
            ktlint-format/maven/io.github.detekt.sarif4k/sarif4k-jvm/pom/0.6.0 0.6.0 SHA-256/8b90fc3745f008f510886ce707539f6bb71bbdd1b7c9db8fd1db0ae566a1c0af
            ktlint-format/maven/io.github.oshai/kotlin-logging-jvm 7.0.3 SHA-256/241daa21665dd0ca55576a4bc4d8e9ace8891ae3c698cc77f13bb4bdac372e94
            ktlint-format/maven/io.github.oshai/kotlin-logging-jvm/pom/7.0.3 7.0.3 SHA-256/ecdc9980188e12d5f994b3222bb9e6629ddeee3ef6c6c7dc4b6e2ffa69c674c9
            ktlint-format/maven/net.java.dev.jna/jna 5.14.0 SHA-256/34ed1e1f27fa896bca50dbc4e99cf3732967cec387a7a0d5e3486c09673fe8c6
            ktlint-format/maven/net.java.dev.jna/jna/pom/5.14.0 5.14.0 SHA-256/e04e25951501df25adc7b1dcdb6c53373c94897b84d3e14921292763ee07ae3d
            ktlint-format/maven/org.ec4j.core/ec4j-core 1.1.0 SHA-256/3424a9c2632a8433c00ab0b7cc10489eb732675c97fe333aff5b5272c5e861f6
            ktlint-format/maven/org.ec4j.core/ec4j-core-parent/pom/1.1.0 1.1.0 SHA-256/df80286269cdc5c3e552976a21537740fe682917611f10707b02c6df16c2dfa1
            ktlint-format/maven/org.ec4j.core/ec4j-core/pom/1.1.0 1.1.0 SHA-256/40c775667a61725ae06934e3f8c10d16d1f6d0fe92ec5b741e39b2fc09484e20
            ktlint-format/maven/org.jetbrains.intellij.deps/trove4j 1.0.20200330 SHA-256/c5fd725bffab51846bf3c77db1383c60aaaebfe1b7fe2f00d23fe1b7df0a439d
            ktlint-format/maven/org.jetbrains.intellij.deps/trove4j/pom/1.0.20200330 1.0.20200330 SHA-256/87721cbaa65a3c97d8b1ba9d207840f164c9fe38759fc9ea10ffe26565f8d3e9
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable 2.1.0 SHA-256/c1b139a6f251c3b99e92befa326cb75d93a001d74c3ac601155a8cdb0d253783
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom/2.1.0 2.1.0 SHA-256/0044fa45046fba7ca808d98f9895f25b16510edb53c1e282107db006d126d67f
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable 2.1.0 SHA-256/6aa581bd53c3500e380e4bb6b2407f6d233910012f425349c2ed5a8ddbe29eac
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom/2.1.0 2.1.0 SHA-256/c85b7aec38f0a3e53cd221265d08233d9ab1f0600902b7d3d74754def84abf18
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-reflect/pom/1.6.10 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-script-runtime 2.1.0 SHA-256/15a2b82119e9f145ea028029bd31166584648a419157c20948c124fa33d40e50
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-script-runtime/pom/2.1.0 2.1.0 SHA-256/f3332fc0f5e962f6aefe0a2c08d3c4d7ab35b13a05dac84c4bc6914e26ed27cd
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib 2.1.0 SHA-256/d6f91b7b0f306cca299fec74fb7c34e4874d6f5ec5b925a0b4de21901e119c3f
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-common 1.7.20 SHA-256/e0e91962bc0007338bf5b1739f62927ac32d14ba3d827fa608ab4e5351729d5d
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-common/pom/1.7.20 1.7.20 SHA-256/1862fe818a7e7bb8d0a7c031aea238b59d549453b10859ce07638c9f96ff4885
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.7.20 SHA-256/524da3c1a2ad56fd52c4ae2272ef3de421de8d2047ab1c51fc306d351243f2f5
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7/pom/1.7.20 1.7.20 SHA-256/3b91f670de0b0f4c78549a2f5e60b57548a33ab6416b1054ef63865d6c62ae95
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.7.20 SHA-256/1da0d306c995945e1f807240ef64b5cd2dd5ac58612afb1a8596143d10b7ded5
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8/pom/1.7.20 1.7.20 SHA-256/491ffbc948a3477491a7fbd43a368c5731cfafe6432af80783b2bbfe67e09fa7
            ktlint-format/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.1.0 2.1.0 SHA-256/134e48c17796c1c1027ecbe6c9f1c75c792f535987ab89e1e1dda43f8c366f5e
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-coroutines-bom/pom/1.6.4 1.6.4 SHA-256/ab2614855fba66aa8a42514dbe3d5a884315ffe1ed63f5932e710a8006245ce1
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm 1.6.4 SHA-256/c24c8bb27bb320c4a93871501a7e5e0c61607638907b197aef675513d4c820be
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/pom/1.6.4 1.6.4 SHA-256/09d960f85922903c2e52e126b17e9fa5020b425c469ec61944b3c01835547224
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm 1.4.1 SHA-256/eba7f1c854296e4ce1418fb01360f8f10c5683e7c45aa3472018417a067636f3
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm/pom/1.4.1 1.4.1 SHA-256/6e522e0a863ead95d7f2732e00f6f45bf302ea32e09be88c64d060c07655f832
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm 1.4.1 SHA-256/af604c46737121d4225fdb60ef0e17766a3c94b7c1c9ef76b4e3a5c7733d557e
            ktlint-format/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm/pom/1.4.1 1.4.1 SHA-256/32d9347c710135fbe70a70192857bc73b06959962edecfff31926039be24b861
            ktlint-format/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            ktlint-format/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            ktlint-format/maven/org.slf4j/slf4j-api 2.0.7 SHA-256/5d6298b93a1905c32cda6478808ac14c2d4a47e91535e53c41f7feeb85d946f4
            ktlint-format/maven/org.slf4j/slf4j-api/pom/2.0.7 2.0.7 SHA-256/2d403ccf0e0a02d5c1a8667b0e2a33c8dfc6038ab287b9671dd681c205267981
            ktlint-format/maven/org.slf4j/slf4j-parent/pom/2.0.7 2.0.7 SHA-256/c182bb36cd3af1c93c1603cdfefe7889157db30ba8b584f4a44535fcd22e45e7
            """;
    private static final String VERSION = "1.5.0";

    @TempDir
    private Path root, project;

    @Test
    public void formats_in_place_then_skips_an_unchanged_file_on_a_second_run() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve(".editorconfig"), """
                root = true
                [*.kt]
                indent_size = 4
                """);
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.kt");
        Files.writeString(sample, """
                package sample

                fun main() {
                println("hello")
                }
                """);

        newExecutor().execute();

        Path resolved = root.resolve("ktlint-format").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned ktlint version resolves")
                    .anyMatch(name -> name.contains("ktlint-cli") && name.contains(VERSION));
        }
        String formatted = Files.readString(sample);
        assertThat(formatted)
                .as("the source is reformatted in place")
                .contains("    println(\"hello\")");
        Path command = root.resolve("ktlint-format").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();
        Path hashes = root.resolve("ktlint-format").resolve("format").resolve("output").resolve("formatted.properties");
        assertThat(hashes).isNotEmptyFile();

        newExecutor().execute();

        assertThat(Files.readString(sample)).as("the already-formatted file is left byte-identical").isEqualTo(formatted);
        assertThat(command).as("the formatter is not forked again when nothing changed").doesNotExist();
    }

    private BuildExecutor newExecutor() throws IOException {
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
                "ktlint-format",
                new KtlintFormatModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT).verify(false),
                "project");
        return executor;
    }
}
