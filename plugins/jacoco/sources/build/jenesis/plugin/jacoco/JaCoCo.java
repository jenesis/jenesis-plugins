package build.jenesis.plugin.jacoco;

import module java.base;
import build.jenesis.ObservabilityEngine;

public record JaCoCo(String name) implements ObservabilityEngine {

    private static final String AGENT = "maven/org.jacoco/org.jacoco.agent/jar/runtime";

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
        return List.of("-javaagent:"
                + agent.toAbsolutePath()
                + "=destfile="
                + output.resolve("jacoco.exec").toAbsolutePath()
                + ",output=file,append=false");
    }
}
