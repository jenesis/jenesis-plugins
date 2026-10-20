package build.jenesis.plugin.asyncprofiler;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.ObservabilityFactory;
import build.jenesis.Observation;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class AsyncProfilerFactory implements ObservabilityFactory {

    @Override
    public Observation create(Environment environment,
                              SequencedMap<String, String> properties,
                              Permits permits,
                              String group,
                              Map<String, Repository> repositories,
                              Map<String, Resolver> resolvers) {
        String system = System.getProperty("os.name"), architecture = System.getProperty("os.arch");
        if (!system.startsWith("Mac") && !(system.startsWith("Linux")
                && Set.of("amd64", "x86_64", "aarch64").contains(architecture))) {
            throw new IllegalStateException("async-profiler ships its library for Linux on x64 and arm64 and for"
                    + " macOS, not for " + system + " on " + architecture + " - switch the plugin off with"
                    + " -Djenesis.observe.asyncprofiler=false where it cannot run");
        }
        SequencedMap<String, String> options = new LinkedHashMap<>();
        properties.forEach((key, value) -> {
            String option = value.strip();
            if (key.equals("file")) {
                throw new IllegalArgumentException("The async-profiler plugin writes what it sampled to the test"
                        + " step's reports/" + group + "/ - leave file out of its values");
            } else if (key.isBlank() || (key + option).contains(",") || key.contains("=")) {
                throw new IllegalArgumentException("A value of the async-profiler plugin is an option of"
                        + " async-profiler, such as event=cpu or a flag such as threads, that holds no comma, and the"
                        + " key no equals sign: '" + key + (option.isEmpty() ? "" : "=" + option) + "'");
            }
            options.put(key, option);
        });
        return new Observation(new AsyncProfiler(group, options), null);
    }
}
