package build.jenesis.plugin.revapi;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Enforcement;

public class RevapiFactory implements BuildModuleFactory {

    private static final String BASELINE = "baseline", SEVERITY = "severity";

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        String origin = "The revapi plugin";
        for (String key : properties.sequencedKeySet()) {
            if (!key.equals(BASELINE) && !key.equals(SEVERITY)) {
                throw new IllegalArgumentException(origin + " names " + key + ", but takes " + BASELINE
                        + "=<groupId>/<artifactId>[/<version>], " + SEVERITY + "=<"
                        + String.join("|", Arrays.stream(RevapiModule.Severity.values())
                                .map(RevapiFactory::spelt)
                                .toList())
                        + ">, and reads revapi's own configuration from a " + RevapiModule.ANALYSIS
                        + " it is handed as @analysis/" + RevapiModule.ANALYSIS + "=<file>");
            }
        }
        if (Enforcement.ofEnvironment(environment, RevapiModule.SETTING) == Enforcement.IGNORE) {
            return (_, _) -> { };
        }
        RevapiModule module = RevapiModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
        String severity = value(properties, SEVERITY);
        if (severity != null) {
            module = module.severity(severity(origin, severity));
        }
        return module.baseline(value(properties, BASELINE));
    }

    private static String value(SequencedMap<String, String> properties, String key) {
        String value = properties.get(key);
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static RevapiModule.Severity severity(String origin, String value) {
        for (RevapiModule.Severity constant : RevapiModule.Severity.values()) {
            if (spelt(constant).equals(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(origin + " names the " + SEVERITY + " " + value + ", but takes one of "
                + Arrays.stream(RevapiModule.Severity.values()).map(RevapiFactory::spelt).toList());
    }

    private static String spelt(Enum<?> constant) {
        return constant.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
