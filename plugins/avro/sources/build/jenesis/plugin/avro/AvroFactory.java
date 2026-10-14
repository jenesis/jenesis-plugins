package build.jenesis.plugin.avro;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class AvroFactory implements BuildModuleFactory {

    private static final Set<String> KEYS = Set.of("arguments");

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException("The Avro plugin takes no value " + entry.getKey()
                        + ": name arguments, those handed to the tool");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return AvroModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .arguments(values.words("arguments"));
    }
}
