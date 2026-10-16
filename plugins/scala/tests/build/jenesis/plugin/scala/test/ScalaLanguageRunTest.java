package build.jenesis.plugin.scala.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Language;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.scala.ScalaFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaLanguageRunTest {

    @TempDir
    private Path root, project;

    @Test
    public void compiles_and_documents_scala() throws IOException {
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
        executor.addModule("scala", language.compiler(), "project");
        executor.addModule("scaladoc", language.documentation(), "project", "scala");
        executor.execute();

        Path classes = root.resolve("scala/classes/output").resolve(BuildStep.CLASSES);
        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
        assertThat(classes.resolve("sample/data.txt"))
                .as("the build copies the resources itself")
                .doesNotExist();
        assertThat(root.resolve("scaladoc")).isNotEmptyDirectory();
    }
}
