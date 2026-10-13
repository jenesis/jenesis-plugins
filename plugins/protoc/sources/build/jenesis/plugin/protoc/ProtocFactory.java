package build.jenesis.plugin.protoc;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class ProtocFactory implements BuildModuleFactory {

    private static final Set<String> KEYS = Set.of("classifier", "plugins", "arguments");

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Path configuration,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException("The protoc plugin takes no value " + entry.getKey()
                        + ": name classifier, the platform of the protoc executable, plugins, the protoc plugins"
                        + " as <name>=<groupId>/<artifactId>, or arguments, those handed to the tool");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        SequencedMap<String, String> plugins = new LinkedHashMap<>();
        List<String> entries = values.entries("plugins");
        if (entries != null) {
            for (String entry : entries) {
                int assign = entry.indexOf('=');
                if (assign < 1 || assign == entry.length() - 1) {
                    throw new IllegalArgumentException("Malformed protoc plugin: " + entry
                            + " (expected <name>=<groupId>/<artifactId>)");
                }
                plugins.put(entry.substring(0, assign).trim(), entry.substring(assign + 1).trim());
            }
        }
        ProtocModule module = ProtocModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .plugins(plugins)
                .arguments(values.words("arguments"));
        String classifier = values.value("classifier");
        return classifier == null ? module : module.classifier(classifier);
    }
}
