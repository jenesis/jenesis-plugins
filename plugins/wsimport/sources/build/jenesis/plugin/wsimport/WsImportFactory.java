package build.jenesis.plugin.wsimport;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class WsImportFactory implements BuildModuleFactory {

    private static final Set<String> KEYS = Set.of("package", "location", "arguments");

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
                throw new IllegalArgumentException("The wsimport plugin takes no value " + entry.getKey()
                        + ": name package, the package of the generated sources, or location, where the generated client reads its description, or arguments, those handed to the tool");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return WsImportModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .packageName(values.value("package"))
                .location(values.value("location"))
                .arguments(values.words("arguments"));
    }
}
