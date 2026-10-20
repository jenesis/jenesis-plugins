package build.jenesis.plugin.asyncprofiler.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.plugin.asyncprofiler.AsyncProfiler;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncProfilerTest {

    private static final String AGENT = "maven/tools.profiler/async-profiler";

    @TempDir
    private Path folder;

    @Test
    public void resolves_async_profiler_as_the_agent() {
        assertThat(new AsyncProfiler("profile", new LinkedHashMap<>()).coordinates())
                .containsExactly(Map.entry(AGENT, "RELEASE"));
    }

    @Test
    public void starts_the_agent_with_its_options_and_writes_a_flame_graph_to_the_reports() {
        SequencedMap<String, String> options = new LinkedHashMap<>();
        options.put("event", "itimer");
        options.put("threads", "");
        Path agent = folder.resolve("async-profiler.jar"), output = folder.resolve("output");
        SequencedMap<String, Path> resolved = new LinkedHashMap<>();
        resolved.put(AGENT, agent);

        List<String> commands = new AsyncProfiler("profile", options).commands(resolved, output);

        assertThat(commands).containsExactly("--enable-native-access=ALL-UNNAMED",
                "-javaagent:" + agent.toAbsolutePath() + "=start,event=itimer,threads,file="
                        + output.resolve("reports/profile/tests.html").toAbsolutePath());
        assertThat(output.resolve("reports/profile"))
                .as("async-profiler writes no file into a folder that does not exist")
                .isDirectory();
    }

    @Test
    public void names_the_report_after_the_format_an_option_selects() {
        SequencedMap<String, String> options = new LinkedHashMap<>();
        options.put("jfr", "");
        Path output = folder.resolve("output");
        SequencedMap<String, Path> resolved = new LinkedHashMap<>();
        resolved.put(AGENT, folder.resolve("async-profiler.jar"));

        assertThat(new AsyncProfiler("profile", options).commands(resolved, output).getLast())
                .endsWith(",file=" + output.resolve("reports/profile/tests.jfr").toAbsolutePath());
    }

    @Test
    public void attaches_nothing_where_the_agent_is_not_resolved() {
        assertThat(new AsyncProfiler("profile", new LinkedHashMap<>()).commands(new LinkedHashMap<>(), folder))
                .isEmpty();
    }
}
