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
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.scala.ScalaFactory;
import build.jenesis.project.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScalaFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void claims_the_scala_sources_and_compiles_them_before_javac() throws IOException {
        Language language = create(Environment.NONE, Map.of());

        assertThat(language.extensions()).containsExactly(".scala");
        assertThat(language.order())
                .as("Scala compiles jointly with the Java sources, so javac compiles against its classes")
                .isEqualTo(Language.Order.BEFORE_JAVAC);
        assertThat(language.documentation()).isNotNull();
    }

    @Test
    public void refuses_a_value_since_settings_configure_it() {
        assertThatThrownBy(() -> create(Environment.NONE, Map.of("incremental", "true")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("takes no values")
                .hasMessageContaining("[incremental]")
                .hasMessageContaining("jenesis.scala.incremental");
    }

    @Test
    public void resolves_the_compiler_in_the_group_it_is_handed() throws IOException {
        assertThat(required(create(Environment.NONE, Map.of()).compiler()))
                .containsExactly("language/runtime/maven/org.scala-lang/scala3-compiler_3/RELEASE");
    }

    @Test
    public void resolves_zinc_in_a_group_beside_the_compiler_when_compiling_incrementally() throws IOException {
        assertThat(required(create(new Environment(Map.of("scala.incremental", "true")), Map.of()).compiler()))
                .containsExactlyInAnyOrder("language/runtime/maven/org.scala-lang/scala3-compiler_3/RELEASE",
                        "language/runtime/maven/org.scala-lang/scala3-sbt-bridge/RELEASE",
                        "language-zinc/runtime/maven/org.scala-sbt/zinc_2.13/RELEASE");
    }

    @Test
    public void resolves_scaladoc_in_a_group_beside_the_compiler() throws IOException {
        assertThat(required(create(Environment.NONE, Map.of()).documentation()))
                .as("Scaladoc follows a Scala release of its own, so it is pinned apart from the compiler")
                .contains("language-scaladoc/runtime/maven/org.scala-lang/scaladoc_3/RELEASE");
    }

    private static Language create(Environment environment, Map<String, String> values) throws IOException {
        return new ScalaFactory().create(environment,
                new LinkedHashMap<>(values),
                new Semaphore(1),
                "language",
                Map.of(),
                Map.of("maven", (_, _, _, _, _, _) -> new Resolver.Resolution(new LinkedHashMap<>(),
                        List.of(),
                        new LinkedHashMap<>())));
    }

    private Set<String> required(BuildExecutorModule module) throws IOException {
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
        executor.addModule("language", module, "project");
        executor.execute("language/required");
        return SequencedProperties.ofFiles(root.resolve("language/required/output").resolve(BuildStep.REQUIRES))
                .stringPropertyNames();
    }
}
