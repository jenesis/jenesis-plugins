package build.jenesis.plugin.revapi.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.revapi.RevapiModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RevapiModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void compares_no_api_of_a_test_module() throws IOException {
        SequencedProperties module = new SequencedProperties();
        module.setProperty("test", "main_artifact");
        module.store(project.resolve(BuildStep.MODULE));
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
        executor.addModule("revapi", new RevapiModule(Map.of(), Map.of()).baseline("org.example/library/1.0.0"), "project");

        assertThat(executor.execute().sequencedKeySet())
                .as("the tests of a module have no release of their own to be compatible with")
                .noneMatch(key -> key.startsWith("revapi/"));
    }

    @Test
    public void requires_the_revapi_extensions_and_the_baseline_it_names() throws IOException {
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
        executor.addModule("revapi", new RevapiModule(Map.of(), Map.of()).baseline("org.example/library/1.0.0"), "project");
        executor.execute("revapi/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("revapi").resolve("required")
                .resolve("output").resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames()).containsExactlyInAnyOrder(
                "revapi/runtime/maven/org.revapi/revapi-java/RELEASE",
                "revapi/runtime/maven/org.revapi/revapi-basic-features/RELEASE",
                "revapi/runtime/maven/org.revapi/revapi-reporter-json/RELEASE",
                "revapi/baseline/maven/org.example/library/1.0.0");
    }

}
