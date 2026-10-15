package build.jenesis.plugin.jacoco;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.ObservabilityFactory;
import build.jenesis.Observation;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class JaCoCoFactory implements ObservabilityFactory {

    private static final String MINIMUM = "minimum.", MAXIMUM_MISSED = "maximum.missed.";

    @Override
    public Observation create(Environment environment,
                              SequencedMap<String, String> properties,
                              Permits permits,
                              String group,
                              Map<String, Repository> repositories,
                              Map<String, Resolver> resolvers) {
        SequencedMap<JaCoCoModule.Counter, Double> minimum = new LinkedHashMap<>();
        SequencedMap<JaCoCoModule.Counter, Long> maximumMissed = new LinkedHashMap<>();
        List<String> counters = Arrays.stream(JaCoCoModule.Counter.values())
                .map(candidate -> candidate.name().toLowerCase(Locale.ROOT))
                .toList();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey(), value = entry.getValue().strip();
            boolean ratio = key.startsWith(MINIMUM);
            String name = ratio ? key.substring(MINIMUM.length())
                    : key.startsWith(MAXIMUM_MISSED) ? key.substring(MAXIMUM_MISSED.length())
                    : null;
            if (name == null || !counters.contains(name)) {
                throw new IllegalArgumentException("The JaCoCo plugin takes no value " + key + ": name " + MINIMUM
                        + "<counter> or " + MAXIMUM_MISSED + "<counter> for a counter of " + counters);
            }
            JaCoCoModule.Counter counter = JaCoCoModule.Counter.valueOf(name.toUpperCase(Locale.ROOT));
            try {
                if (ratio) {
                    minimum.put(counter, Double.parseDouble(value));
                } else {
                    maximumMissed.put(counter, Long.parseLong(value));
                }
            } catch (NumberFormatException _) {
                throw new IllegalArgumentException("The value " + value + " of " + key + " is "
                        + (ratio
                                ? "no covered ratio, expected a number from 0 to 1, such as 0.8"
                                : "no count of missed items, expected a whole number of at least 0"));
            }
        }
        return new Observation(new JaCoCo(group), JaCoCoModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool")
                .minimum(minimum)
                .maximumMissed(maximumMissed));
    }
}
