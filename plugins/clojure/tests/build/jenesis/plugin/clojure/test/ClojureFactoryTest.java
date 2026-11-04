package build.jenesis.plugin.clojure.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.Environment;
import build.jenesis.Language;
import build.jenesis.Permits;
import build.jenesis.plugin.clojure.ClojureCompilerModule;
import build.jenesis.plugin.clojure.ClojureFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClojureFactoryTest {

    @Test
    public void compiles_clojure_sources_after_javac_and_documents_none() {
        Language language = create(new LinkedHashMap<>());

        assertThat(language.extensions()).containsExactly(".clj", ".cljc");
        assertThat(language.order())
                .as("Clojure compiles against the classes javac wrote, as a namespace imports a Java class")
                .isEqualTo(Language.Order.AFTER_JAVAC);
        assertThat(language.compiler()).isInstanceOf(ClojureCompilerModule.class);
        assertThat(language.documentation()).isNull();
    }

    @Test
    public void takes_whether_to_compile_ahead_of_time() {
        assertThat(create(new LinkedHashMap<>(Map.of("aot", "false"))).compiler())
                .isInstanceOf(ClojureCompilerModule.class);
    }

    @Test
    public void refuses_a_value_it_does_not_take_naming_the_one_it_does() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("direct-linking", "true"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("direct-linking")
                .hasMessageContaining("aot=false");
    }

    @Test
    public void refuses_an_aot_value_that_is_not_a_flag() {
        assertThatThrownBy(() -> create(new LinkedHashMap<>(Map.of("aot", "sometimes"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sometimes");
    }

    private static Language create(SequencedMap<String, String> properties) {
        return new ClojureFactory().create(Environment.NONE,
                properties,
                Permits.of(1),
                "clojure",
                Map.of(),
                Map.of());
    }
}
