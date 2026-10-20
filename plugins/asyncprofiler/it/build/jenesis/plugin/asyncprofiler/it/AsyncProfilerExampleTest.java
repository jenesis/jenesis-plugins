package build.jenesis.plugin.asyncprofiler.it;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;
import build.jenesis.plugin.harness.ExampleProject;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncProfilerExampleTest {

    @TempDir
    private Path folder;

    @Test
    public void profiles_the_tests_it_observes() throws IOException {
        ExampleProject project = ExampleProject.of(folder, AsyncProfilerFactory.class)
                .write("jenesis.plugins.properties", "asyncprofiler+observe=build.jenesis.plugin.asyncprofiler\n")
                .write("build.jenesis/plugin-asyncprofiler.properties", """
                        event=itimer
                        interval=1ms
                        collapsed
                        """)
                .write("sources/module-info.java", "module example { exports example; }\n")
                .write("sources/example/Hasher.java", """
                        package example;
                        public class Hasher {
                            public long hash(long rounds) {
                                long hash = 0;
                                for (long round = 0; round < rounds; round++) {
                                    hash = 31 * hash + Long.toString(round).hashCode();
                                }
                                return hash;
                            }
                        }
                        """)
                .write("tests/module-info.java", """
                        /**
                         * @jenesis.test example
                         */
                        module example.test {
                            requires example;
                            requires org.junit.jupiter.api;
                        }
                        """)
                .write("tests/example/test/HasherTest.java", """
                        package example.test;
                        import example.Hasher;
                        import org.junit.jupiter.api.Test;
                        import static org.junit.jupiter.api.Assertions.assertNotEquals;
                        class HasherTest {
                            @Test
                            void hashes() {
                                long end = System.nanoTime() + 500_000_000L, hash = 0;
                                while (System.nanoTime() < end) {
                                    hash += new Hasher().hash(10_000);
                                }
                                assertNotEquals(1, hash);
                            }
                        }
                        """);

        project.make();

        assertThat(project.find("tests.collapsed"))
                .as("the agent the plugin attaches samples the tests' run and writes it in the format a value selects")
                .content()
                .contains("example/Hasher.hash");
    }
}
