package build.jenesis.plugin.groovy.test;

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
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.groovy.GroovyCompilerModule;
import build.jenesis.plugin.groovy.GroovyDocumentationModule;
import build.jenesis.step.Dependencies;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyModuleRunTest {

    private static final String PINS = """
            groovy/maven/com.github.javaparser/javaparser-core 3.28.2 SHA-256/b5499a3b1c40b16c0671fabe478c9aafeab38160c6fde74a6c13f42d86716ecd
            groovy/maven/org.apache.groovy/groovy 6.0.0 SHA-256/6bcbec490ac1ade0dca6566dca9d02897cbb8fc31ee4008e950451aa83b1ac76
            groovy/maven/org.apache.groovy/groovy-docgenerator 6.0.0 SHA-256/38e82ca37456f5eaad576ff9d12272eafa780d65a7f9b6d07d3883461509e838
            groovy/maven/org.apache.groovy/groovy-groovydoc 6.0.0 SHA-256/ed8639a52b400c45ce67918721dc7b2bd5e080819e14f304b99ef191c1759c32
            groovy/maven/org.apache.groovy/groovy-templates 6.0.0 SHA-256/c570490d7a8b96d5bf97a384a3ab71583c030102c938258d8785955662fa452d
            groovy/maven/org.apache.groovy/groovy-xml 6.0.0 SHA-256/64b8e4788280ed88be1f1e439a71100e3d391ed446ced5b20b8730a3618c8c57
            groovy/maven/org.commonmark/commonmark 0.30.0 SHA-256/090f0a758ac2f4f51aa86dff9dff0fdf7403666d0efd29f59886dc8e8b1eac53
            """;

    @TempDir
    private Path root, project, compiled;

    @Test
    public void compiles_a_groovy_source_against_a_downloaded_compiler() throws IOException, ReflectiveOperationException {
        pin();
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.groovy"), """
                package sample
                class Sample {
                    String greet() { "Hello world!" }
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovy", new GroovyCompilerModule(repositories(), resolvers()).pinning(Pinning.STRICT), "project");
        executor.execute();

        Path classes = root.resolve("groovy").resolve(GroovyCompilerModule.VERSIONED).resolve("output")
                .resolve(BuildStep.CLASSES);
        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
        List<URL> urls = new ArrayList<>(List.of(classes.toUri().toURL()));
        for (Path jar : Dependencies.all(root.resolve("groovy").resolve(GroovyCompilerModule.RESOLVED)
                .resolve("resolve").resolve("output"))) {
            urls.add(jar.toUri().toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), null)) {
            Class<?> sample = loader.loadClass("sample.Sample");
            assertThat(sample.getMethod("greet").invoke(sample.getConstructor().newInstance()))
                    .as("the compiled class runs on the Groovy runtime it was compiled with")
                    .isEqualTo("Hello world!");
        }
    }

    @Test
    public void groovy_resolves_java_types_from_the_compiled_class_path() throws IOException {
        pin();
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.groovy"), """
                package sample
                class Sample {
                    String greet() { new Greeter().hello() + " from Groovy" }
                }
                """);
        Path greeter = Files.createDirectories(compiled.resolve("src").resolve("sample")).resolve("Greeter.java");
        Files.writeString(greeter, """
                package sample;
                public class Greeter {
                    public String hello() { return "Hello"; }
                }
                """);
        Path greeterClasses = Files.createDirectories(compiled.resolve(BuildStep.CLASSES));
        assertThat(ToolProvider.findFirst("javac").orElseThrow()
                .run(System.out, System.err, "-d", greeterClasses.toString(), greeter.toString()))
                .isZero();

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addSource("classes", compiled);
        executor.addModule("groovy",
                new GroovyCompilerModule(repositories(), resolvers()).pinning(Pinning.STRICT),
                "project",
                "classes");
        executor.execute();

        Path classes = root.resolve("groovy").resolve(GroovyCompilerModule.COMPILED).resolve("output")
                .resolve(BuildStep.CLASSES);
        assertThat(classes.resolve("sample/Sample.class"))
                .as("Sample.groovy resolved the Greeter type from the compiled class path and compiled")
                .isNotEmptyFile();
        assertThat(classes.resolve("sample/Greeter.class"))
                .as("groovyc compiles only Groovy sources; the Java type comes from the class path, not recompilation")
                .doesNotExist();
    }

    @Test
    public void renders_the_documentation_of_a_groovy_source_with_a_downloaded_groovydoc() throws IOException {
        pin();
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("Sample.groovy"), """
                package sample
                /** Greets the world. */
                class Sample {
                    String greet() { "Hello world!" }
                }
                """);

        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("groovydoc",
                new GroovyDocumentationModule(repositories(), resolvers()).pinning(Pinning.STRICT),
                "project");
        executor.execute();

        Path documentation = root.resolve("groovydoc").resolve(GroovyDocumentationModule.DOCUMENTED).resolve("output")
                .resolve("javadoc");
        assertThat(documentation.resolve("index.html")).isNotEmptyFile();
        assertThat(documentation.resolve("sample/Sample.html"))
                .as("Groovydoc renders the documentation comment of the class")
                .content()
                .contains("Greets the world.");
    }

    private void pin() throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
    }

    private static Map<String, Repository> repositories() {
        return Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE));
    }

    private static Map<String, Resolver> resolvers() {
        return Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE));
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
