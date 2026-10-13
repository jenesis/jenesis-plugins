package build.jenesis.plugin.eclipse.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.Environment;
import build.jenesis.plugin.eclipse.EclipseJavaFormatFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EclipseJavaFormatFactoryTest {

    @TempDir
    private Path project;

    @Test
    public void resolves_the_profile_beside_the_file_that_activates_it() throws IOException {
        Path configuration = Files.writeString(project.resolve("javaformat.properties"), "profile=profile.xml\n");
        Files.writeString(project.resolve("profile.xml"), "<profiles/>");

        assertThat(create(configuration, Map.of("profile", "profile.xml"))).isNotNull();
    }

    @Test
    public void refuses_a_profile_outside_the_folder_of_the_file_that_activates_it() {
        Path configuration = project.resolve("javaformat.properties");

        assertThatThrownBy(() -> create(configuration, Map.of("profile", "../profile.xml")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not beside or below");
    }

    @Test
    public void refuses_a_profile_that_is_no_file() {
        Path configuration = project.resolve("javaformat.properties");

        assertThatThrownBy(() -> create(configuration, Map.of("profile", "missing.xml")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is no file");
    }

    @Test
    public void refuses_a_value_it_does_not_know() {
        assertThatThrownBy(() -> create(project.resolve("javaformat.properties"), Map.of("style", "aosp")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no value style");
    }

    private static Object create(Path configuration, Map<String, String> values) {
        return new EclipseJavaFormatFactory().create(Environment.NONE,
                new LinkedHashMap<>(values),
                configuration,
                new Semaphore(1),
                "eclipse-java-format",
                Map.of(),
                Map.of());
    }
}
