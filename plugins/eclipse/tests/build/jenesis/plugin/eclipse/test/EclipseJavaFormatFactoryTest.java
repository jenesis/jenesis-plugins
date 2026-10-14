package build.jenesis.plugin.eclipse.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EclipseJavaFormatFactoryTest {

    @Test
    public void creates_a_formatter_that_reads_its_profile_from_what_the_project_binds() {
        assertThat(create(Map.of())).isNotNull();
    }

    @Test
    public void refuses_a_profile_named_as_a_value_since_a_file_is_bound() {
        assertThatThrownBy(() -> create(Map.of("profile", "profile.xml")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@profile/eclipse-formatter.xml=<file>");
    }

    private static Object create(Map<String, String> values) {
        return new EclipseJavaFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                Permits.of(1),
                "eclipse-java-format",
                Map.of(),
                Map.of());
    }
}
