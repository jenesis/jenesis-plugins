package build.jenesis.plugin.scala.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Observation;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.scala.ScalaFactory;
import build.jenesis.project.InferredCompilerChainModule;
import build.jenesis.project.InferredDocumentationChainModule;
import build.jenesis.project.Language;
import build.jenesis.project.PluginModule;
import build.jenesis.project.TestFramework;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaLanguageRunTest {

    @TempDir
    private Path root, project;

    @Test
    public void compiles_scala_before_javac_and_documents_it_beside_java() throws IOException {
        ScalaPins.write(project, "scala:tool", "scala:zinc", "scala:scaladoc");
        Path sample = Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
        Files.writeString(sample.resolve("Greeter.java"), """
                package sample;
                /** Greets. */
                public class Greeter {
                    /** @return a greeting */
                    public String hello() { return "Hello"; }
                }
                """);
        Files.writeString(sample.resolve("Sample.scala"), """
                package sample
                /** Extends a greeting. */
                class Sample:
                  def greet(): String = Greeter().hello() + " from Scala"
                """);
        Files.writeString(sample.resolve("Caller.java"), """
                package sample;
                /** Calls Scala. */
                public class Caller {
                    /** @return what Scala says */
                    public String call() { return new Sample().greet(); }
                }
                """);
        Files.writeString(sample.resolve("data.txt"), "data");
        Map<String, Repository> repositories = Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE));
        Map<String, Resolver> resolvers = Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE));
        Language language = new ScalaFactory().create(new Environment(Map.of("dependency.pin", "strict")),
                new LinkedHashMap<>(),
                Permits.of(1),
                "scala",
                repositories,
                resolvers);
        SequencedMap<String, PluginModule> languages = new LinkedHashMap<>(Map.of("scala", new Plugged(language)));

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
        executor.addModule("chain",
                new InferredCompilerChainModule().languages(languages),
                "project");
        executor.addModule("doc",
                new InferredDocumentationChainModule().languages(languages),
                "project",
                "chain");
        executor.execute();

        Path compile = root.resolve("chain").resolve(InferredCompilerChainModule.COMPILE);
        Path scala = compile.resolve("scala/classes/output").resolve(BuildStep.CLASSES);
        assertThat(scala.resolve("sample/Sample.class")).isNotEmptyFile();
        assertThat(scala.resolve("sample/data.txt"))
                .as("the build copies the resources itself")
                .doesNotExist();
        assertThat(compile.resolve(InferredCompilerChainModule.JAVAC).resolve("output").resolve(BuildStep.CLASSES)
                .resolve("sample/Caller.class"))
                .as("javac compiles the Java source that calls Scala against the classes the plugin wrote")
                .isNotEmptyFile();
        assertThat(compile.resolve(InferredCompilerChainModule.RESOURCE).resolve("output").resolve(BuildStep.CLASSES)
                .resolve("sample/data.txt"))
                .hasContent("data");
        Path javadoc = root.resolve("doc/document/aggregate/output/javadoc");
        assertThat(javadoc.resolve("sample/Caller.html")).isNotEmptyFile();
        assertThat(javadoc.resolve("scala/index.html"))
                .as("Scaladoc is placed in a folder named after the plugin beside the documentation of javac")
                .isNotEmptyFile();
    }

    private record Plugged(Language language) implements PluginModule {

        @Override
        public BuildExecutorModule preparation() {
            return (_, _) -> { };
        }

        @Override
        public Observation observation(SequencedMap<String, Path> prepared) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestFramework framework(SequencedMap<String, Path> prepared) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Language language(SequencedMap<String, Path> prepared) {
            return language;
        }

        @Override
        public PluginModule toolRepositories(Map<String, Repository> toolRepositories) {
            return this;
        }

        @Override
        public PluginModule toolResolvers(Map<String, Resolver> toolResolvers) {
            return this;
        }

        @Override
        public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        }
    }
}
