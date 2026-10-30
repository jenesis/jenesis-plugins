package build.jenesis.plugin.jacoco.test;

import module java.base;
import module java.compiler;
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
import build.jenesis.plugin.jacoco.JaCoCo;
import build.jenesis.plugin.jacoco.JaCoCoModule;
import build.jenesis.project.TestModule;
import build.jenesis.step.Javac;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JaCoCoModuleRunTest {

    private static final String PINS = """
            jacoco/maven/args4j/args4j 2.0.28 SHA-256/11b029a602e787e2bc08eb3b77eda1a4f5e8b263d22e3c5d6220cd5c51f30b18
            jacoco/maven/args4j/args4j-site/pom/2.0.28 2.0.28 SHA-256/79e151fb939f6c9c8d8f9ae59e5b0e7a862dd5a699b4d4954704e7ce25d4fac8
            jacoco/maven/args4j/args4j/pom/2.0.28 2.0.28 SHA-256/1f821c93660b7fa573692c305ca97092eab647cefeccfcb3f54771117c3737d3
            jacoco/maven/org.jacoco/org.jacoco.agent/jar/runtime 0.8.14 SHA-256/3fb76eea65f81bd9415202bab34b6571728841dff1ab8e6bbe81adc2e299face
            jacoco/maven/org.jacoco/org.jacoco.build/pom/0.8.14 0.8.14 SHA-256/87d059b35849df97779148fd7e52f63f1fcd03a4e7e64c2a61a6c9cce61a8de7
            jacoco/maven/org.jacoco/org.jacoco.cli 0.8.14 SHA-256/12d5d78351c638efeea71ac840f6a5cf7bcff89001ddb05eb89f956d1079a2c6
            jacoco/maven/org.jacoco/org.jacoco.cli/pom/0.8.14 0.8.14 SHA-256/240a1cdc87a5510fde23fbe14067589823b6b057d88e17a8b6fb984a4f54fbdd
            jacoco/maven/org.jacoco/org.jacoco.core 0.8.14 SHA-256/28abbf0eea5a08e4f24097f2fbac663ca17c341c25c3a04d90d6cd325943c995
            jacoco/maven/org.jacoco/org.jacoco.core/pom/0.8.14 0.8.14 SHA-256/7b75bae2c1779af544369833afe1d68f5a3955e91d166cb19dff886534b1ad6c
            jacoco/maven/org.jacoco/org.jacoco.report 0.8.14 SHA-256/a3e2026060ab8b8d5c650706406234bb4c033dfd5376afeb8b1666e8ed27c453
            jacoco/maven/org.jacoco/org.jacoco.report/pom/0.8.14 0.8.14 SHA-256/e2ebf2c580f86afa7f75f43a28109dd894c42463928bc9911777e35275108e58
            jacoco/maven/org.kohsuke/pom/pom/8 8 SHA-256/1efa02d3c0f11024319c6e258bb9c76f6255a8fa302a0d8001a253a4be8d7ce3
            jacoco/maven/org.ow2.asm/asm 9.9 SHA-256/03d99a74ad1ee5c71334ef67437f4ef4fe3488caa7c96d8645abc73c8e2017d4
            jacoco/maven/org.ow2.asm/asm-bom/pom/9.9 9.9 SHA-256/0fa27336fa7b62923ca9bdc69526ebe712a3bfb1f7ee8bf76c4a0b3f85f19d62
            jacoco/maven/org.ow2.asm/asm-commons 9.9 SHA-256/db2f6f26150bbe7c126606b4a1151836bcc22a1e05a423b3585698bece995ff8
            jacoco/maven/org.ow2.asm/asm-commons/pom/9.9 9.9 SHA-256/18a5d3ef1377e7544b3ca3210f261396b987f6010ef591d3872adae63119ee43
            jacoco/maven/org.ow2.asm/asm-tree 9.9 SHA-256/42178f3775c9c63f9e5e1446747d29b4eca4d91bd6e75e5c43cfa372a47d38c6
            jacoco/maven/org.ow2.asm/asm-tree/pom/9.9 9.9 SHA-256/d01788ed8e6e8e24acaf6cb4a7bdcc2733416252faa00232959e3e2e9df1bf44
            jacoco/maven/org.ow2.asm/asm/pom/9.9 9.9 SHA-256/cf861ef1e745d57154af79ae14df340ed5341e5dcd0157cefcd2eb7f1aa05c57
            jacoco/maven/org.ow2/ow2/pom/1.5.1 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
            """;

    @TempDir
    private Path root, dependencies, classes, sources;

    @Test
    public void collects_coverage_and_renders_a_report() throws Exception {
        cover(UnaryOperator.identity());

        Path exec = root.resolve("test").resolve("executed").resolve("output").resolve("jacoco.exec");
        assertThat(exec).as("the agent wrote its execution data into the test step output").isNotEmptyFile();
        assertThat(Files.readString(exec, StandardCharsets.ISO_8859_1))
                .as("the agent instrumented and recorded the covered class")
                .contains("coverage/Covered");

        Path xml = root.resolve("coverage").resolve("report").resolve("output")
                .resolve("reports").resolve("jacoco").resolve("jacoco.xml");
        assertThat(xml).as("the report processor rendered an XML report").isNotEmptyFile();
        assertThat(xml).content()
                .contains("name=\"coverage/Covered\"")
                .contains("covered=\"");
    }

    @Test
    public void passes_a_coverage_its_limits_admit() throws Exception {
        cover(module -> module.minimum(JaCoCoModule.Counter.METHOD, 0.5).maximumMissed(JaCoCoModule.Counter.METHOD, 1));

        assertThat(root.resolve("coverage").resolve("report").resolve("output")
                .resolve("reports").resolve("jacoco").resolve("jacoco.xml")).isNotEmptyFile();
    }

    @Test
    public void fails_the_build_on_a_coverage_below_a_minimum() {
        assertThatThrownBy(() -> cover(module -> module.minimum(JaCoCoModule.Counter.METHOD, 0.9)))
                .hasStackTraceContaining("a covered ratio of 0.9 of methods is not met: 4 of 5 covered");
    }

    @Test
    public void fails_the_build_on_more_missed_items_than_a_maximum() {
        assertThatThrownBy(() -> cover(module -> module.maximumMissed(JaCoCoModule.Counter.METHOD, 0)))
                .hasStackTraceContaining("at most 0 missed methods is not met: 1 missed");
    }

    private void cover(UnaryOperator<JaCoCoModule> limits) throws Exception {
        Path artifacts = Files.createDirectory(dependencies.resolve(BuildStep.ARTIFACTS));
        for (Path path : bootModuleJars()) {
            String fileName = path.getFileName().toString();
            if (fileName.endsWith("_rt.jar") || fileName.endsWith("-rt.jar")) {
                continue;
            }
            Files.copy(path, artifacts.resolve(fileName + "-" + UUID.randomUUID() + ".jar"));
        }

        Path sourcePackage = Files.createDirectories(sources.resolve(BuildStep.SOURCES).resolve("coverage"));
        Files.writeString(sourcePackage.resolve("Covered.java"), """
                package coverage;
                public class Covered {
                    public int add(int left, int right) {
                        return left + right;
                    }
                    public int negate(int value) {
                        return -value;
                    }
                }
                """);
        compileSources(classes.resolve(Javac.CLASSES + "coverage"), bootModuleJars(), Map.of(
                "Covered", """
                        package coverage;
                        public class Covered {
                            public int add(int left, int right) {
                                return left + right;
                            }
                            public int negate(int value) {
                                return -value;
                            }
                        }
                        """,
                "CoveredTest", """
                        package coverage;
                        public class CoveredTest {
                            @org.junit.jupiter.api.Test
                            public void adds() {
                                if (new Covered().add(2, 3) != 5) {
                                    throw new AssertionError();
                                }
                            }
                        }
                        """));

        SequencedProperties versions = new SequencedProperties();
        versions.setProperty("main/maven/org.junit.platform/junit-platform-console",
                "1.11.4 SHA-256/a9c3309cdfded3542200de85da6cb274864439d6b02ba80bb45ecc8e0bdf1be7");
        versions.setProperty("main/maven/org.junit.platform/junit-platform-reporting",
                "1.11.4 SHA-256/df6896109bfaef4de8d2fa9e3371a6176936d1a45a6c0e7fd8f7e6dd6f4c5597");
        versions.setProperty("main/maven/org.junit.platform/junit-platform-launcher",
                "1.11.4 SHA-256/d7430bd029e7fcced53ee445e4d2d1a8a1e043ea4c4df43b6335a857f79761ae");
        versions.setProperty("main/maven/org.junit.platform/junit-platform-engine",
                "1.11.3 SHA-256/0043f72f611664735da8dc9a308bf12ecd2236b05339351c4741edb4d8fab0da");
        versions.setProperty("main/maven/org.junit.platform/junit-platform-commons",
                "1.11.3 SHA-256/be262964b0b6b48de977c61d4f931df8cf61e80e750cc3f3a0a39cdd21c1008c");
        versions.setProperty("main/maven/org.opentest4j/opentest4j",
                "1.3.0 SHA-256/48e2df636cab6563ced64dcdff8abb2355627cb236ef0bf37598682ddf742f1b");
        versions.setProperty("main/maven/org.apiguardian/apiguardian-api",
                "1.1.2 SHA-256/b509448ac506d607319f182537f0b35d71007582ec741832a1f111e5b5b70b38");
        versions.load(new StringReader(PINS));
        versions.store(dependencies.resolve(BuildStep.VERSIONS));

        BuildExecutor executor = newExecutor();
        executor.addSource("dependencies", dependencies);
        executor.addSource("classes", classes);
        executor.addSource("sources", sources);
        executor.addModule(
                "test",
                new TestModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                        .observe(List.of(new JaCoCo("jacoco")))
                        .isTest(candidate -> candidate.endsWith("CoveredTest"))
                        .jarsOnly(false),
                "dependencies",
                "classes");
        executor.addModule(
                "coverage",
                limits.apply(new JaCoCoModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)), Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT).classes(true)),
                "test",
                "classes",
                "sources",
                "dependencies");
        executor.execute();
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

    private static void compileSources(Path classesDir, List<Path> classpath, Map<String, String> units)
            throws IOException {
        Files.createDirectories(classesDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classesDir.getParent()));
            fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath);
            List<JavaFileObject> sources = new ArrayList<>();
            for (Map.Entry<String, String> unit : units.entrySet()) {
                String body = unit.getValue();
                sources.add(new SimpleJavaFileObject(
                        URI.create("string:///coverage/" + unit.getKey() + ".java"),
                        JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return body;
                    }
                });
            }
            if (!compiler.getTask(null, fileManager, null, null, null, sources).call()) {
                throw new IllegalStateException("Failed to compile sources");
            }
        }
    }

    private static List<Path> bootModuleJars() throws IOException {
        Set<Path> jars = new LinkedHashSet<>();
        for (ResolvedModule resolved : ModuleLayer.boot().configuration().modules()) {
            String name = resolved.name();
            if (name.startsWith("java.") || name.startsWith("jdk.")) {
                continue;
            }
            URI location = resolved.reference().location().orElse(null);
            if (location == null) {
                continue;
            }
            Path path = Path.of(location);
            if (Files.isRegularFile(path)) {
                jars.add(path);
            }
        }
        Enumeration<URL> manifests = ClassLoader.getSystemClassLoader().getResources("META-INF/MANIFEST.MF");
        while (manifests.hasMoreElements()) {
            String url = manifests.nextElement().toString();
            if (!url.startsWith("jar:file:")) {
                continue;
            }
            int bang = url.indexOf("!/");
            if (bang < 0) {
                continue;
            }
            Path path = Path.of(URI.create(url.substring("jar:".length(), bang)));
            if (Files.isRegularFile(path)) {
                jars.add(path);
            }
        }
        return new ArrayList<>(jars);
    }
}
