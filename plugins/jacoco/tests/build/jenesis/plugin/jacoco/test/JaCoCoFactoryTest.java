package build.jenesis.plugin.jacoco.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.Environment;
import build.jenesis.Observation;
import build.jenesis.plugin.jacoco.JaCoCo;
import build.jenesis.plugin.jacoco.JaCoCoFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JaCoCoFactoryTest {

    @Test
    public void attaches_the_agent_in_the_group_it_is_handed_and_reports_beside_it() throws IOException {
        Observation observation = create(Map.of("minimum.line", "0.8", "maximum.missed.branch", "3"));
        assertThat(observation.engine())
                .as("the agent resolves and is pinned in the group the loading build names")
                .isEqualTo(new JaCoCo("coverage"));
        assertThat(observation.report()).isNotNull();
    }

    @Test
    public void refuses_a_value_that_names_no_counter() {
        assertThatThrownBy(() -> create(Map.of("minimum.lines", "0.8")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("takes no value minimum.lines")
                .hasMessageContaining("[instruction, branch, line, complexity, method, class]");
    }

    @Test
    public void refuses_a_ratio_that_is_no_number() {
        assertThatThrownBy(() -> create(Map.of("minimum.line", "most")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no covered ratio");
    }

    @Test
    public void refuses_a_ratio_above_one() {
        assertThatThrownBy(() -> create(Map.of("minimum.line", "80")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is no ratio");
    }

    private static Observation create(Map<String, String> values) throws IOException {
        return new JaCoCoFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                new Semaphore(1),
                "coverage",
                Map.of(),
                Map.of());
    }
}
