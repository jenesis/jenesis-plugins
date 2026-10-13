package build.jenesis.plugin.revapi;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class RevapiFactory implements BuildModuleFactory {

    private static final String BASELINE = "baseline", SEVERITY = "severity", ANALYSIS = "configuration";

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Path configuration,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) throws IOException {
        String origin = configuration == null ? "The revapi plugin" : configuration.toString();
        for (String key : properties.sequencedKeySet()) {
            if (!key.equals(BASELINE) && !key.equals(SEVERITY) && !key.equals(ANALYSIS)) {
                throw new IllegalArgumentException(origin + " names " + key + ", but takes " + BASELINE
                        + "=<groupId>/<artifactId>[/<version>], " + SEVERITY + "=<"
                        + String.join("|", Arrays.stream(RevapiModule.Severity.values())
                                .map(RevapiFactory::spelt)
                                .toList())
                        + "> and " + ANALYSIS + "=<a JSON file of revapi's own configuration beside it>");
            }
        }
        RevapiModule module = RevapiModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
        String severity = value(properties, SEVERITY), name = value(properties, ANALYSIS);
        if (severity != null) {
            module = module.severity(severity(origin, severity));
        }
        if (name != null) {
            if (configuration == null) {
                throw new IllegalStateException("The revapi plugin names the configuration " + name
                        + " but was activated by no file to resolve it against - activate it by the file beside it");
            }
            Path sibling = Path.of(name).normalize();
            if (sibling.isAbsolute() || sibling.startsWith("..")) {
                throw new IllegalArgumentException(origin + " names the configuration " + name
                        + ", which is not beside or below it - place the file in its folder");
            }
            Path resolved = configuration.resolveSibling(sibling);
            if (!Files.isRegularFile(resolved)) {
                throw new IllegalArgumentException(origin + " names the configuration " + name
                        + ", which does not exist beside it");
            }
            try {
                module = module.analysis(Files.readString(resolved));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(resolved + ": " + e.getMessage());
            }
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
