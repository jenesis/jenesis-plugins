package build.jenesis.plugin.asyncprofiler.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.Environment;
import build.jenesis.Observation;
import build.jenesis.Permits;
import build.jenesis.plugin.asyncprofiler.AsyncProfiler;
import build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsyncProfilerFactoryTest {

    @Test
    public void attaches_the_agent_in_the_group_it_is_handed_with_its_values_as_options() throws IOException {
        SequencedMap<String, String> values = new LinkedHashMap<>();
        values.put("event", "itimer");
        values.put("threads", "");
        Observation observation = create(values);
        assertThat(observation.engine())
                .as("the agent resolves and is pinned in the group the loading build names")
                .isEqualTo(new AsyncProfiler("profile", values));
        assertThat(observation.report())
                .as("async-profiler writes its report itself")
                .isNull();
    }

    @Test
    public void refuses_a_file_which_the_build_names() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("file", "profile.html"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reports/profile/")
                .hasMessageContaining("leave file out");
    }

    @Test
    public void refuses_a_value_that_holds_a_comma() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("include", "a,b"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("holds no comma")
                .hasMessageContaining("'include=a,b'");
    }

    private static Observation create(SequencedMap<String, String> values) throws IOException {
        return new AsyncProfilerFactory().create(Environment.NONE,
                values,
                Permits.of(1),
                "profile",
                Map.of(),
                Map.of());
    }
}
