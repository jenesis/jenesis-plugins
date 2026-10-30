package build.jenesis.plugin.scalafmt.test;

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
import build.jenesis.plugin.scalafmt.ScalafmtFormatModule;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalafmtFormatModuleRunTest {

    private static final String PINS = """
            scalafmt-format/maven/com.geirsson/metaconfig-core_2.13 0.12.0 SHA-256/2c91199ae5b206afdd52538f8c4da67c1794bcce0b5b06cf25679db00cf32c19
            scalafmt-format/maven/com.geirsson/metaconfig-core_2.13/pom/0.12.0 0.12.0 SHA-256/78a7c18dfdedf9e76b54acebf3e3233e778a3fddc94322cf961928c3503ebdf2
            scalafmt-format/maven/com.geirsson/metaconfig-pprint_2.13 0.12.0 SHA-256/6d8b0b4279116c11d4f29443bd2a9411bedb3d86ccaf964599a9420f530ed062
            scalafmt-format/maven/com.geirsson/metaconfig-pprint_2.13/pom/0.12.0 0.12.0 SHA-256/f8c523eea1c425253bcbd10466edd299bc1372985319907f37416402d5a58b3f
            scalafmt-format/maven/com.geirsson/metaconfig-typesafe-config_2.13 0.12.0 SHA-256/b4c5dbf863dadde363d8bd24333ce1091fec94fc5b88efd04607a26f3eab61b8
            scalafmt-format/maven/com.geirsson/metaconfig-typesafe-config_2.13/pom/0.12.0 0.12.0 SHA-256/903474db5893bf2e03926821791a466f0377f167c4e8ab4a2570b89450661ecd
            scalafmt-format/maven/com.github.scopt/scopt_2.13 4.1.0 SHA-256/2e5037bda974630b046794274e344273919abf4727acfcd86352617dce68f82b
            scalafmt-format/maven/com.github.scopt/scopt_2.13/pom/4.1.0 4.1.0 SHA-256/cfb5878cf5d988f0482346fd376a9122ef173a535e4ab33636dd96714c6f94e3
            scalafmt-format/maven/com.googlecode.java-diff-utils/diffutils 1.3.0 SHA-256/61ba4dc49adca95243beaa0569adc2a23aedb5292ae78aa01186fa782ebdc5c2
            scalafmt-format/maven/com.googlecode.java-diff-utils/diffutils/pom/1.3.0 1.3.0 SHA-256/2fe31dd6309b0f5f195bbdc4749cfc0af065d61f06cfe183dfd2f2092ab847b6
            scalafmt-format/maven/com.lihaoyi/fansi_2.13 0.4.0 SHA-256/0eb11a2a905d608033ec1642b0a9f0d8444daa4ad465f684b50bdc7e7a41bf53
            scalafmt-format/maven/com.lihaoyi/fansi_2.13/pom/0.4.0 0.4.0 SHA-256/1049e12338f1b42b9a8c87cedec06f307a8cce54e1c766c68d41bda9dbf80e71
            scalafmt-format/maven/com.lihaoyi/sourcecode_2.13 0.3.0 SHA-256/6e5b2d55e942b450a222bfd3ebc23e99ca03716e42da25af1b2c8cde038100f5
            scalafmt-format/maven/com.lihaoyi/sourcecode_2.13/pom/0.3.0 0.3.0 SHA-256/749778086a4b559f1791d68b4d58a1e80c60496f32f53b6c1f75e8f21b1bfdd0
            scalafmt-format/maven/com.martiansoftware/nailgun-all/pom/0.9.1 0.9.1 SHA-256/3d7b4fccdd6c8062c7037f74f14e59aeaa80c835b783b49d72eed4a9da874d8d
            scalafmt-format/maven/com.martiansoftware/nailgun-server 0.9.1 SHA-256/4518faa6bf4bd26fccdc4d85e1625dc679381a08d56872d8ad12151dda9cef25
            scalafmt-format/maven/com.martiansoftware/nailgun-server/pom/0.9.1 0.9.1 SHA-256/e1e164a7e12f35d2d940cd4a52ab0cba37da07179eb3dfa70b8989dbeb305d5d
            scalafmt-format/maven/com.typesafe/config 1.4.3 SHA-256/8ada4c185ce72416712d63e0b5afdc5f009c0cdf405e5f26efecdf156aa5dfb6
            scalafmt-format/maven/com.typesafe/config/pom/1.4.3 1.4.3 SHA-256/b67eafa9dd220ff87d00dba6880083a5296a5e0c6c03103f5d4b8e1da1030ff3
            scalafmt-format/maven/io.get-coursier/interface 0.0.17 SHA-256/b3987e8c02441e82d88ab8727acd64eabf3a35217ffedba904b125e06a722a77
            scalafmt-format/maven/io.get-coursier/interface/pom/0.0.17 0.0.17 SHA-256/6e23602d09bc9eaa98af430db24a2a18a9dd8834ff3820b58d90ca52e561643a
            scalafmt-format/maven/io.github.java-diff-utils/java-diff-utils 4.12 SHA-256/9990a2039778f6b4cc94790141c2868864eacee0620c6c459451121a901cd5b5
            scalafmt-format/maven/io.github.java-diff-utils/java-diff-utils-parent/pom/4.12 4.12 SHA-256/d811cf9f118cc2cad130c9427ad55c174d4c0a6f1a00ac1ae1c9bcbec5c44b19
            scalafmt-format/maven/io.github.java-diff-utils/java-diff-utils/pom/4.12 4.12 SHA-256/c26e097edc8ec6805d1319817d23d4e496cc1015cc55dc5202112d8f6a9165fc
            scalafmt-format/maven/net.java.dev.jna/jna 5.13.0 SHA-256/66d4f819a062a51a1d5627bffc23fac55d1677f0e0a1feba144aabdd670a64bb
            scalafmt-format/maven/net.java.dev.jna/jna/pom/5.13.0 5.13.0 SHA-256/f515c2578178f45247ecca7a9e1db109531b1c42f2424e253ceeb0f6b8d42374
            scalafmt-format/maven/org.jline/jline 3.22.0 SHA-256/7c3ec8d2c5815188bbaefa4c7c42bc9b8ec172382ca026a4b4f3d113c0b5c3e3
            scalafmt-format/maven/org.jline/jline-parent/pom/3.22.0 3.22.0 SHA-256/ff69472bd34dda302f4e24de29438e5de690f3cf45828e9884af3cc512e5859f
            scalafmt-format/maven/org.jline/jline/pom/3.22.0 3.22.0 SHA-256/d1c8aae3ed14ae8e1e0ef751ee7a2671c899ec535a8a86409554addbc3d9dcf7
            scalafmt-format/maven/org.scala-lang.modules/scala-collection-compat_2.13 2.11.0 SHA-256/0c1108883b7b97851750e8932f9584346ccb23f1260c197f97295ac2e6c56cec
            scalafmt-format/maven/org.scala-lang.modules/scala-collection-compat_2.13/pom/2.11.0 2.11.0 SHA-256/20cece3425535ba0dd11991aa8e88dd67265ba39960899fc0b4f31d39d10d414
            scalafmt-format/maven/org.scala-lang.modules/scala-parallel-collections_2.13 1.0.4 SHA-256/68f266c4fa37cb20a76e905ad940e241190ce288b7e4a9877f1dd1261cd1a9a7
            scalafmt-format/maven/org.scala-lang.modules/scala-parallel-collections_2.13/pom/1.0.4 1.0.4 SHA-256/fa9a9cb2f0d7914d7817ae96e05b84cb1ce115a40e23c0d25bf0321035d02b38
            scalafmt-format/maven/org.scala-lang/scala-compiler 2.13.11 SHA-256/c5a14770370e73a69367b131da1533890200b1e2aa70643b73f9ff31ef2e69ec
            scalafmt-format/maven/org.scala-lang/scala-compiler/pom/2.13.11 2.13.11 SHA-256/86c2d5e817489e1b478bd713c5cd8ad980eb9045fa831ef3a0d72952a20d4395
            scalafmt-format/maven/org.scala-lang/scala-library 2.13.13 SHA-256/b58b223f9b59f88d04c9dc9ec26f4b463e93e6f296de5663dbd710a7dfce2743
            scalafmt-format/maven/org.scala-lang/scala-library/pom/2.13.13 2.13.13 SHA-256/fdc1a8e8a231d73a6f2258e2a48cf0f7fd7113366ee81c026b184dbc3021efe2
            scalafmt-format/maven/org.scala-lang/scala-reflect 2.13.13 SHA-256/8e7fa6fd2f9682035bfeae45160afa38ca67aec7dfc614146a758efdeb2ffafc
            scalafmt-format/maven/org.scala-lang/scala-reflect/pom/2.13.13 2.13.13 SHA-256/0d9273a398b31c09e44dd70e7f7f60a7327f42af0b7299f5580882a98abc505f
            scalafmt-format/maven/org.scala-lang/scalap 2.13.11 SHA-256/ac358699f40002fb4f32ad77531765fce23425d0e83c51854d1635118ab285ea
            scalafmt-format/maven/org.scala-lang/scalap/pom/2.13.11 2.13.11 SHA-256/e9de7460557210b8fd2b4fadd94d107b9941ff6e6dccb1bf021f041e93df062f
            scalafmt-format/maven/org.scalameta/common_2.13 4.9.9 SHA-256/be66ba789863c65abfc9c1e448339ce915f2bc778daf348d884a967e8eb473ee
            scalafmt-format/maven/org.scalameta/common_2.13/pom/4.9.9 4.9.9 SHA-256/efe6ed0fd32ed1f227c07e9c853a1dfc6cea61932b9ce83ef487c9b25a5eafa3
            scalafmt-format/maven/org.scalameta/mdoc-parser_2.13 2.5.4 SHA-256/a36fc6125666047b653f8acb1aad16db4aefaaaffdc3f53d2b9eeec83dc580bf
            scalafmt-format/maven/org.scalameta/mdoc-parser_2.13/pom/2.5.4 2.5.4 SHA-256/4a5b391930d212a404de3005c7d26c573727b5b8ce7124a557b04a249b791bd8
            scalafmt-format/maven/org.scalameta/parsers_2.13 4.9.9 SHA-256/ab4198d993b4214d9b98277f96c4ac76a72b87a1fea8df96e9be8e3e98176d7a
            scalafmt-format/maven/org.scalameta/parsers_2.13/pom/4.9.9 4.9.9 SHA-256/63eb277c8ed8c5a4105c95ddefeb6d268574ac5d5f07f13e9babe7d3272b147b
            scalafmt-format/maven/org.scalameta/scalafmt-cli_2.13 3.8.3 SHA-256/1d1862d8dfb1d65c788cb366b542ac9934d147b55d0516bf81dd58806b055af8
            scalafmt-format/maven/org.scalameta/scalafmt-cli_2.13/pom/3.8.3 3.8.3 SHA-256/80915b9e47662705b61cb8ec9072b969ac1a265abc13e645a6d9bcf1c17dc8a6
            scalafmt-format/maven/org.scalameta/scalafmt-config_2.13 3.8.3 SHA-256/175c7e345baccb75e0f79aa9d8c821834b4b232d3917039c44ca2f0265f2110a
            scalafmt-format/maven/org.scalameta/scalafmt-config_2.13/pom/3.8.3 3.8.3 SHA-256/c654b4b7a162f23fb8669d27d21031d7e0f65f84b927d086e98db43dd787cd4a
            scalafmt-format/maven/org.scalameta/scalafmt-core_2.13 3.8.3 SHA-256/c214d16a746ceab8ac47b97c18d2817f726174dd58da75d43472d045ddc25009
            scalafmt-format/maven/org.scalameta/scalafmt-core_2.13/pom/3.8.3 3.8.3 SHA-256/32acaaffb66151ad9126f2e4ce8d6f696062a6923cc9b4eb48f6bb1002ea2ca6
            scalafmt-format/maven/org.scalameta/scalafmt-dynamic_2.13 3.8.3 SHA-256/a3b79331a8f8d57ba57820de3ef0dd0f5608f3d0547403ed724bc09e1a17ea2a
            scalafmt-format/maven/org.scalameta/scalafmt-dynamic_2.13/pom/3.8.3 3.8.3 SHA-256/5df41a50c9a35773ee246da906a4961f5676cff6d5ebde640143a901c7fb55fb
            scalafmt-format/maven/org.scalameta/scalafmt-interfaces 3.8.3 SHA-256/37510a20442b73f68e6112c2bee6dcd8d60760e0a1ed5758c9949e587e61e954
            scalafmt-format/maven/org.scalameta/scalafmt-interfaces/pom/3.8.3 3.8.3 SHA-256/f24449f4b17cbdc317aef052af6fb908537f898011babee2b1c08e0a0a092aa4
            scalafmt-format/maven/org.scalameta/scalafmt-sysops_2.13 3.8.3 SHA-256/981b5455b956ece0e7f2c0825241c6f99b2d70cc2352700a2fcffa5c01ed6633
            scalafmt-format/maven/org.scalameta/scalafmt-sysops_2.13/pom/3.8.3 3.8.3 SHA-256/ee41bb53ba78fa9108e035ca1181832f32980e2f29c325fd7ed0ed6c4f7307ad
            scalafmt-format/maven/org.scalameta/scalameta_2.13 4.9.9 SHA-256/01a3c1130202400dbcf4ea0f42374c8e392b9199716ddf605217f4bf1f61cb1d
            scalafmt-format/maven/org.scalameta/scalameta_2.13/pom/4.9.9 4.9.9 SHA-256/60d6a5a3ee439058c49d1d1020346a2a85564db5b8bbc2d430880c4ffe04ea1d
            scalafmt-format/maven/org.scalameta/svm-subs 101.0.0 SHA-256/b31eb8ef90bec4c22a8ec858f5bd007bd46ce80c3dcef9dce238c6f9dd15c1a4
            scalafmt-format/maven/org.scalameta/svm-subs/pom/101.0.0 101.0.0 SHA-256/3685479a8534fd4e11001f765ca076e850b5c77959aef755cd4419c202f6818e
            scalafmt-format/maven/org.scalameta/trees_2.13 4.9.9 SHA-256/d016cde916b19d6c814ac296544a1882b96664ac03e5ef27019a518482c3db49
            scalafmt-format/maven/org.scalameta/trees_2.13/pom/4.9.9 4.9.9 SHA-256/ab3e69c71e959aefc4d06b2e5fd41827926b301a7941e6367e4dc712ddb8efbd
            scalafmt-format/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            scalafmt-format/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            scalafmt-format/maven/org.typelevel/paiges-core_2.13 0.4.3 SHA-256/4daa8b180b466634b66be040e1097c107981c0ba0b7c605e2f7c0b66ae1b99b5
            scalafmt-format/maven/org.typelevel/paiges-core_2.13/pom/0.4.3 0.4.3 SHA-256/c40162ac9b52b576a6a34dde0e4c509b981ac49702a5fd4ee3042b75ad43e185
            """;
    private static final String VERSION = "3.8.3";

    @TempDir
    private Path root, project;

    @Test
    public void formats_in_place_then_skips_an_unchanged_file_on_a_second_run() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Files.writeString(project.resolve(".scalafmt.conf"), """
                version = "3.8.3"
                runner.dialect = scala213
                """);
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.scala");
        Files.writeString(sample, "package sample\nclass Sample {   def  f( ) :Int=42 }\n");

        newExecutor().execute();

        Path resolved = root.resolve("scalafmt-format").resolve("dependencies").resolve("resolve").resolve("output").resolve("resolved");
        try (Stream<Path> jars = Files.list(resolved)) {
            assertThat(jars.map(jar -> jar.getFileName().toString()))
                    .as("the pinned scalafmt version resolves")
                    .anyMatch(name -> name.contains("scalafmt-cli") && name.contains(VERSION));
        }
        String formatted = Files.readString(sample);
        assertThat(formatted)
                .as("the source is reformatted in place")
                .contains("def f(): Int = 42");
        Path command = root.resolve("scalafmt-format").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();
        Path hashes = root.resolve("scalafmt-format").resolve("format").resolve("output").resolve("formatted.properties");
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
                "scalafmt-format",
                new ScalafmtFormatModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT).verify(false),
                "project");
        return executor;
    }
}
