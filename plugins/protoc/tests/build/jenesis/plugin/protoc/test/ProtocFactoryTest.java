package build.jenesis.plugin.protoc.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Permits;
import build.jenesis.SequencedProperties;
import build.jenesis.plugin.protoc.ProtocFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProtocFactoryTest {

    @TempDir
    private Path root, project;

    @Test
    public void requires_the_tool_in_the_tool_group_of_the_plugin_it_is_handed() throws IOException {
        assertThat(required(new LinkedHashMap<>()))
                .isNotEmpty()
                .allMatch(key -> key.startsWith("quality:tool/"));
    }

    @Test
    public void requires_each_generator_in_a_group_of_its_own_beside_the_tool() throws IOException {
        assertThat(required(new LinkedHashMap<>(Map.of("plugins", "grpc-java=io.grpc/protoc-gen-grpc-java"))))
                .as("a generator follows a release of its own, so it is pinned apart from protoc")
                .anyMatch(key -> key.startsWith("quality:tool/"))
                .anyMatch(key -> key.startsWith("quality:grpc-java/runtime/maven/io.grpc/protoc-gen-grpc-java/"));
    }

    @Test
    public void refuses_a_generator_named_like_the_group_of_the_tool() {
        assertThatThrownBy(() -> new ProtocFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("plugins", "tool=io.grpc/protoc-gen-grpc-java")),
                Permits.of(1),
                "protoc",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be named tool");
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> new ProtocFactory().create(Environment.NONE,
                new LinkedHashMap<>(Map.of("unknown", "value")),
                Permits.of(1),
                "protoc",
                Map.of(),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }

    private Set<String> required(SequencedMap<String, String> values) throws IOException {
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
        executor.addModule("protoc", new ProtocFactory().create(Environment.NONE,
                values,
                Permits.of(1),
                "quality",
                Map.of(),
                Map.of()), "project");
        executor.execute("protoc/required");
        return SequencedProperties.ofFiles(root.resolve("protoc")
                .resolve("required")
                .resolve("output")
                .resolve(BuildStep.REQUIRES)).stringPropertyNames();
    }
}
