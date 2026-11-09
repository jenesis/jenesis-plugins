package build.jenesis.plugin.openjavaformat.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.openjavaformat.OpenJavaFormatModule;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenJavaFormatModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_emits_the_open_java_format_maven_coordinate() throws IOException {
        BuildExecutor executor = newExecutor();
        executor.addSource("project", project);
        executor.addModule("open-java-format", new OpenJavaFormatModule(Map.of(), Map.of()), "project");
        executor.execute("open-java-format/required");

        Path requiredOutput = root.resolve("open-java-format").resolve("required").resolve("output");
        SequencedProperties requires = SequencedProperties.ofFiles(requiredOutput.resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("open-java-format/runtime/maven/dev.openjavaformat/open-java-format/RELEASE");
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
