package build.jenesis.plugin.ksp.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.ksp.KspFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class KspFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_ksp_apart_from_the_processors_of_the_group_it_is_handed() throws IOException {
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
        executor.addModule("ksp", new KspFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("moshi.generated", "javax.annotation.processing.Generated")),
                new Semaphore(1),
                "symbols",
                Map.of(),
                Map.of()), "project");
        executor.execute("ksp/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("ksp")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .as("KSP resolves apart from the processors, which bring a Kotlin standard library of their own")
                .containsExactly("symbols:runtime/runtime/maven/com.google.devtools.ksp/symbol-processing-aa-embeddable/RELEASE");
    }
}
