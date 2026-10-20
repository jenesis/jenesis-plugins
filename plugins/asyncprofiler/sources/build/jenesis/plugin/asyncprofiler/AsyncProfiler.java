package build.jenesis.plugin.asyncprofiler;

import module java.base;
import build.jenesis.BuildStep;
import build.jenesis.ObservabilityEngine;

public record AsyncProfiler(String name, SequencedMap<String, String> options) implements ObservabilityEngine {

    private static final String AGENT = "maven/tools.profiler/async-profiler";
    private static final Map<String, String> FORMATS = Map.of("jfr", "jfr",
            "collapsed", "collapsed",
            "flat", "txt",
            "traces", "txt",
            "summary", "txt",
            "otlp", "otlp");

    public AsyncProfiler {
        options = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(options));
    }

    @Override
    public SequencedMap<String, String> coordinates() {
        SequencedMap<String, String> coordinates = new LinkedHashMap<>();
        coordinates.put(AGENT, "RELEASE");
        return coordinates;
    }

    @Override
    public List<String> commands(SequencedMap<String, Path> resolved, Path output) {
        Path agent = resolved.get(AGENT);
        if (agent == null) {
            return List.of();
        }
        Path folder = output.resolve(BuildStep.REPORTS + name);
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        StringBuilder arguments = new StringBuilder("start");
        options.forEach((key, value) -> arguments.append(',').append(key).append(value.isEmpty() ? "" : "=" + value));
        String extension = options.keySet().stream()
                .filter(FORMATS::containsKey)
                .map(FORMATS::get)
                .findFirst()
                .orElse("html");
        return List.of("--enable-native-access=ALL-UNNAMED", "-javaagent:"
                + agent.toAbsolutePath()
                + "="
                + arguments
                + ",file="
                + folder.resolve("tests." + extension).toAbsolutePath());
    }
}
