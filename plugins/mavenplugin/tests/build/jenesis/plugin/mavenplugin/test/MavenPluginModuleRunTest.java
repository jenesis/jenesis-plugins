package build.jenesis.plugin.mavenplugin.test;

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
import build.jenesis.plugin.mavenplugin.MavenPluginModule;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenPluginModuleRunTest {

    private static final String PINS = """
            mavenplugin/maven/aopalliance/aopalliance 1.0 SHA-256/0addec670fedcd3f113c5c8091d783280d23f75e3acb841b61a9cdb079376a08
            mavenplugin/maven/com.github.luben/zstd-jni 1.5.6-3 SHA-256/f72ede1b39258faf81277dc58de30c71cbae4253732558d2ce10b53d8b5763d5
            mavenplugin/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            mavenplugin/maven/com.google.guava/guava 33.4.8-jre SHA-256/f3d7f57f67fd622f4d468dfdd692b3a5e3909246c28017ac3263405f0fe617ed
            mavenplugin/maven/com.google.inject/guice/jar/classes 5.1.0 SHA-256/142ad4475e19524d2fe3ac995b3f7cbc962fc726f2edb9dbdccc61feab9b2bf9
            mavenplugin/maven/com.thoughtworks.qdox/qdox 2.1.0 SHA-256/804d51f9bae9e0a84889f3d1b79f5b6586aa91baaa33a8f2175a7b327bd9910e
            mavenplugin/maven/commons-codec/commons-codec 1.11 SHA-256/e599d5318e97aa48f42136a2927e6dfa4e8881dff0e6c8e3109ddbbff51d7b7d
            mavenplugin/maven/commons-io/commons-io 2.16.1 SHA-256/f41f7baacd716896447ace9758621f62c1c6b0a91d89acee488da26fc477c84f
            mavenplugin/maven/commons-logging/commons-logging 1.2 SHA-256/daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636
            mavenplugin/maven/io.airlift/aircompressor 0.27 SHA-256/fdbef3137a28f63bb0cb93487803080ede746a4ec3d421e36c6f0c305c35e5e4
            mavenplugin/maven/javax.inject/javax.inject 1 SHA-256/91c77044a50c481636c32d916fd89c9118a72195390452c81065080f957de7ff
            mavenplugin/maven/net.sf.jtidy/jtidy r938 SHA-256/6fc03e51e73fa884f06e7eae0761e045e56fdeb4e146a4d952e3023cc9e3fb43
            mavenplugin/maven/org.apache.commons/commons-compress 1.26.2 SHA-256/9168a03141d8fc7eda21a2360d83cc0412bcbb1d6204d992bd48c2573cb3c6b8
            mavenplugin/maven/org.apache.commons/commons-lang3 3.17.0 SHA-256/6ee731df5c8e5a2976a1ca023b6bb320ea8d3539fbe64c8a1d5cb765127c33b4
            mavenplugin/maven/org.apache.httpcomponents/httpclient 4.5.14 SHA-256/c8bc7e1c51a6d4ce72f40d2ebbabf1c4b68bfe76e732104b04381b493478e9d6
            mavenplugin/maven/org.apache.httpcomponents/httpcore 4.4.16 SHA-256/6c9b3dd142a09dc468e23ad39aad6f75a0f2b85125104469f026e52a474e464f
            mavenplugin/maven/org.apache.maven.doxia/doxia-sink-api 2.0.0 SHA-256/fba33eaee3b01547bcd14b05ebc37f7dacef1819ad9ee7a5b27899afd3472cf4
            mavenplugin/maven/org.apache.maven.plugin-tools/maven-plugin-annotations 3.15.1 SHA-256/b58bcb3a1f362f6e1efa2772064026bb3d4ad92e6f43a1812d8d2886489912f5
            mavenplugin/maven/org.apache.maven.plugin-tools/maven-plugin-tools-annotations 3.15.1 SHA-256/cb812be374f19da55406714c648534c91b234d73fdaa386afda9ad51a025f208
            mavenplugin/maven/org.apache.maven.plugin-tools/maven-plugin-tools-api 3.15.1 SHA-256/38556a754b8c947c674d1678270aa36cb6a98d9443235229c6d09f8154837098
            mavenplugin/maven/org.apache.maven.plugin-tools/maven-plugin-tools-generators 3.15.1 SHA-256/c9e76fc2501852d55d258e1210ad5eb306686b2f31cfa55b185991a79b1cb392
            mavenplugin/maven/org.apache.maven.reporting/maven-reporting-api 4.0.0 SHA-256/cb2cbde3c9c7288f7398a250dcf3c90cf92714cff301f22b298e1091b5def33c
            mavenplugin/maven/org.apache.maven.resolver/maven-resolver-api 1.9.24 SHA-256/c0ce14849298720dcf3b09fe9e263b3c576d2d6cd43c61f8aa9d6117e04e5e47
            mavenplugin/maven/org.apache.maven.resolver/maven-resolver-impl 1.9.24 SHA-256/3cd88edda00c46242cdbe57aefb47703519d176d9e8a500333de15faf41b0e1d
            mavenplugin/maven/org.apache.maven.resolver/maven-resolver-named-locks 1.9.24 SHA-256/a222b0a5ca4c145e8e0f45207a23bcce18bdbe1f1c980e4e9fb332591ee8c91a
            mavenplugin/maven/org.apache.maven.resolver/maven-resolver-spi 1.9.24 SHA-256/e3153906e93333c106ee18babb72ba7984ccb15e9116ea08f88c71b8d9387e01
            mavenplugin/maven/org.apache.maven.resolver/maven-resolver-util 1.9.24 SHA-256/aeb7e776a8c47b1c317a2581f299e2525b044cb8abde31164e970bbbefab12b1
            mavenplugin/maven/org.apache.maven.shared/maven-shared-utils 3.4.2 SHA-256/b613357e1bad4dfc1dead801691c9460f9585fe7c6b466bc25186212d7d18487
            mavenplugin/maven/org.apache.maven.wagon/wagon-provider-api 3.5.3 SHA-256/5e72000338945ed3e96f8e4f578d1d0672e1af7e19c0e9014197ae5b31af3ef4
            mavenplugin/maven/org.apache.maven/maven-artifact 3.9.11 SHA-256/0bfd1c121f907642344311d81e4dfd93eba6c24dc712cc0aeae273eb22833022
            mavenplugin/maven/org.apache.maven/maven-builder-support 3.9.11 SHA-256/5b1de1560ec19c4c69a655f986455888f3a0bcc3c160cac1f1edb87de3825f77
            mavenplugin/maven/org.apache.maven/maven-core 3.9.11 SHA-256/93d3c0523dda6c86c6620d9e692e7df39d0e2e4e4cd860061a8c0d274c2ad185
            mavenplugin/maven/org.apache.maven/maven-model 3.9.11 SHA-256/163aa596f976264384efeb50b4cb8a806086fd64a9b755ad1f69195fed3998ad
            mavenplugin/maven/org.apache.maven/maven-model-builder 3.9.11 SHA-256/c4c72accdcd848332c1a37995610a6a369d49fdaabc43e85f0022a5b77de3ba7
            mavenplugin/maven/org.apache.maven/maven-plugin-api 3.9.11 SHA-256/8e985a56412564cf687040efed8bcd3a63eac2202a824bb01e1d5ece2b87458f
            mavenplugin/maven/org.apache.maven/maven-repository-metadata 3.9.11 SHA-256/f8655f5f0060e0032944f2a23688d186e140d2184749bcfa1927f83cd6a8b1a2
            mavenplugin/maven/org.apache.maven/maven-resolver-provider 3.9.11 SHA-256/b5d6e52ae1ecf1cb62ede2234234ba3093149524066488e84415187ce478c582
            mavenplugin/maven/org.apache.maven/maven-settings 3.9.11 SHA-256/026718d47d19e360d978857bc63d3dc065884fc2c801563d8a2a77b5ab74ccf8
            mavenplugin/maven/org.apache.maven/maven-settings-builder 3.9.11 SHA-256/33c96e4ca5a15b7c93180f6b2f8deb15f511dfc15dda0d2b671c968b22108972
            mavenplugin/maven/org.apache.velocity/velocity-engine-core 2.4 SHA-256/1bf78c2ade46f209bf93ebe72ed2af5b989ca7a1de0a015fc1b92a62f56b6549
            mavenplugin/maven/org.codehaus.plexus/plexus-archiver 4.10.0 SHA-256/4c07814ff4a39199999ae82bba1e38aa4f25637467fcac6a66ed63a76535799a
            mavenplugin/maven/org.codehaus.plexus/plexus-cipher 2.0 SHA-256/9a7f1b5c5a9effd61eadfd8731452a2f76a8e79111fac391ef75ea801bea203a
            mavenplugin/maven/org.codehaus.plexus/plexus-classworlds 2.9.0 SHA-256/1ad3292cd563381e3fd632f3fded1988f9e9b2be7a9f3db63ff4c4cedba13fa5
            mavenplugin/maven/org.codehaus.plexus/plexus-component-annotations 2.2.0 SHA-256/50edb93c73786e62822b4fe1336e22880fdf147191373cf5c911370e16748fcf
            mavenplugin/maven/org.codehaus.plexus/plexus-interpolation 1.28 SHA-256/ab2a8715570438a2e4164d85ad3e8d489eabc38ea5093c2eb8ab7f58403535b5
            mavenplugin/maven/org.codehaus.plexus/plexus-io 3.5.0 SHA-256/965ed28912cf1ae4c628112c4009e0c19819bc44ed5db8af54ee5eda21036a3e
            mavenplugin/maven/org.codehaus.plexus/plexus-java 1.3.0 SHA-256/f455405f84e37e6b998970cab24e3963a4fdd802e9830fecac7549378b1048a9
            mavenplugin/maven/org.codehaus.plexus/plexus-sec-dispatcher 2.0 SHA-256/873139960c4c780176dda580b003a2c4bf82188bdce5bb99234e224ef7acfceb
            mavenplugin/maven/org.codehaus.plexus/plexus-utils 3.6.0 SHA-256/27ef130e32c236090e408fb5498d94cb9ea26d14070fb1c8985d607b62d098d1
            mavenplugin/maven/org.codehaus.plexus/plexus-velocity 2.2.0 SHA-256/3e7e902f492c973cf210ddb8267843a3b65e83f5067467e2f4d9af0051f6b8b9
            mavenplugin/maven/org.codehaus.plexus/plexus-xml 3.0.1 SHA-256/c1a510a87a62bd2d74ac1472dd31c3f9e9b0b8b8568f37d77c0f135415bebd05
            mavenplugin/maven/org.eclipse.sisu/org.eclipse.sisu.inject 0.9.0.M4 SHA-256/1cbd7a965a5e2a9ea823bab311962a4e5aa5c240705bdbad5a52b40ffdfa1004
            mavenplugin/maven/org.eclipse.sisu/org.eclipse.sisu.plexus 0.9.0.M4 SHA-256/b90579bc652eac7331436e0a25533fce14130b9c6e015f2dd3a3d4bb07e942b7
            mavenplugin/maven/org.jsoup/jsoup 1.18.1 SHA-256/3bb5b0ec02998abe45a51f37d7ce67c3068b4ccd4ab63c965929ec5074d64e91
            mavenplugin/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            mavenplugin/maven/org.ow2.asm/asm 9.8 SHA-256/876eab6a83daecad5ca67eb9fcabb063c97b5aeb8cf1fca7a989ecde17522051
            mavenplugin/maven/org.ow2.asm/asm-analysis 9.7.1 SHA-256/85b29371884ba31bb76edf22323c2c24e172c3267a67152eba3d1ccc2e041ef2
            mavenplugin/maven/org.ow2.asm/asm-tree 9.7.1 SHA-256/9929881f59eb6b840e86d54570c77b59ce721d104e6dfd7a40978991c2d3b41f
            mavenplugin/maven/org.ow2.asm/asm-util 9.7.1 SHA-256/f885be71b5c90556f5f1ad1c4f9276b29b96057c497d46666fe4ddbec3cb43c6
            mavenplugin/maven/org.slf4j/slf4j-api 1.7.36 SHA-256/d3ef575e3e4979678dc01bf1dcce51021493b4d11fb7f1be8ad982877c16a1c0
            mavenplugin/maven/org.tukaani/xz 1.9 SHA-256/211b306cfc44f8f96df3a0a3ddaf75ba8c5289eed77d60d72f889bb855f535e5
            """;

    @TempDir
    private Path root, project, stubs;

    @Test
    public void describes_the_mojos_of_the_classes_with_the_javadoc_of_their_sources() throws IOException {
        prepare();

        execute(new MavenPluginModule(repositories(), resolvers()).pinning(Pinning.STRICT));

        assertThat(root.resolve("mavenplugin/descriptor/output/classes/META-INF/maven/plugin.xml"))
                .as("the descriptor holds what Maven's own generator reads off the classes and their sources")
                .content()
                .contains("<groupId>example</groupId>")
                .contains("<artifactId>greet-maven-plugin</artifactId>")
                .contains("<goalPrefix>greet</goalPrefix>")
                .contains("<goal>hello</goal>")
                .contains("<description>Greets whoever runs the build.</description>")
                .contains("<description>The name to greet.</description>")
                .contains("<requiredJavaVersion>17</requiredJavaVersion>");
        assertThat(root.resolve("mavenplugin/descriptor/output/classes/META-INF/maven/example/greet-maven-plugin/plugin-help.xml"))
                .as("the help descriptor sits beside the POM of the plugin")
                .exists();
    }

    @Test
    public void lists_what_the_plugin_runs_with_and_reads_the_maven_it_needs_off_what_it_compiles_against()
            throws IOException {
        prepare();

        execute(new MavenPluginModule(repositories(), resolvers()).pinning(Pinning.STRICT).goalPrefix("hi"));

        assertThat(root.resolve("mavenplugin/descriptor/output/classes/META-INF/maven/plugin.xml"))
                .content()
                .as("a goal prefix it is named replaces the one of the artifact")
                .contains("<goalPrefix>hi</goalPrefix>")
                .as("a dependency resolved for the runtime is one the plugin runs with")
                .contains("<artifactId>runtime</artifactId>")
                .as("a dependency resolved for compiling alone is provided by Maven")
                .doesNotContain("<artifactId>maven-plugin-api</artifactId>")
                .as("the Maven API it compiles against names the Maven it needs")
                .contains("<requiredMavenVersion>3.9.11</requiredMavenVersion>");
    }

    private void prepare() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path annotations = Files.createDirectories(stubs.resolve("org/apache/maven/plugins/annotations"));
        Files.writeString(annotations.resolve("Mojo.java"), """
                package org.apache.maven.plugins.annotations;
                public @interface Mojo {
                    String name();
                }
                """);
        Files.writeString(annotations.resolve("Parameter.java"), """
                package org.apache.maven.plugins.annotations;
                public @interface Parameter {
                    String property() default "";
                    String defaultValue() default "";
                }
                """);
        Path source = Files.createDirectories(project.resolve(BuildStep.SOURCES).resolve("example")).resolve("GreetMojo.java");
        Files.writeString(source, """
                package example;

                import org.apache.maven.plugins.annotations.Mojo;
                import org.apache.maven.plugins.annotations.Parameter;

                /**
                 * Greets whoever runs the build.
                 */
                @Mojo(name = "hello")
                public class GreetMojo {

                    /**
                     * The name to greet.
                     */
                    @Parameter(property = "greet.name", defaultValue = "${user.name}")
                    private String name;
                }
                """);
        Path compiled = Files.createDirectories(stubs.resolve("classes"));
        assertThat(javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-d", compiled.toString(),
                "--release", "17",
                annotations.resolve("Mojo.java").toString(),
                annotations.resolve("Parameter.java").toString())).as("the annotations compile").isZero();
        Path classes = Files.createDirectories(project.resolve(BuildStep.CLASSES));
        assertThat(javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-d", classes.toString(),
                "-cp", compiled.toString(),
                "--release", "17",
                source.toString())).as("the mojo compiles").isZero();
        Path resolved = Files.createDirectories(project.resolve("resolved"));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(resolved.resolve("api.jar")));
             Stream<Path> files = Files.walk(compiled)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                jar.putNextEntry(new JarEntry(compiled.relativize(file).toString().replace(File.separatorChar, '/')));
                Files.copy(file, jar);
                jar.closeEntry();
            }
        }
        try (JarOutputStream _ = new JarOutputStream(Files.newOutputStream(resolved.resolve("runtime.jar")))) {
        }
        SequencedProperties dependencies = new SequencedProperties();
        dependencies.setProperty("main/compile/maven/org.apache.maven/maven-plugin-api/3.9.11", "resolved/api.jar");
        dependencies.setProperty("main/compile/maven/example/runtime/1.0", "resolved/runtime.jar");
        dependencies.setProperty("main/runtime/maven/example/runtime/1.0", "resolved/runtime.jar");
        dependencies.store(project.resolve(BuildStep.DEPENDENCIES));
        Files.writeString(project.resolve("pom.xml"), """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>example</groupId>
                    <artifactId>greet-maven-plugin</artifactId>
                    <version>1.0</version>
                </project>
                """);
    }

    private static Map<String, build.jenesis.Repository> repositories() {
        return Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE));
    }

    private static Map<String, build.jenesis.Resolver> resolvers() {
        return Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE));
    }

    private void execute(MavenPluginModule module) throws IOException {
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
        executor.addModule("mavenplugin", module, "project");
        executor.execute();
    }
}
