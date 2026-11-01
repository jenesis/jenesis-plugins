package build.jenesis.plugin.princeofspace.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.princeofspace.PrinceOfSpaceModule;

import static org.assertj.core.api.Assertions.assertThat;

public class PrinceOfSpaceModuleTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_step_emits_the_prince_of_space_maven_coordinate() throws IOException {
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
        executor.addModule("prince-of-space", new PrinceOfSpaceModule(Map.of(), Map.of()), "project");
        executor.execute("prince-of-space/required");

        SequencedProperties requires = SequencedProperties.ofFiles(root.resolve("prince-of-space")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES));
        assertThat(requires.stringPropertyNames())
                .containsExactly("prince-of-space/runtime/maven/io.github.agustafson.princeofspace"
                        + "/prince-of-space-core/RELEASE");
    }
}
