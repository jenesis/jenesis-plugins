package build.jenesis.plugin.antlr;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class AntlrFactory implements BuildModuleFactory {

    private static final Set<String> KEYS = Set.of("package", "arguments");

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException("The ANTLR plugin takes no value " + entry.getKey()
                        + ": name package, the package of the generated sources, or arguments, those handed to the"
                        + " tool");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return AntlrModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .packageName(values.value("package"))
                .arguments(values.words("arguments"));
    }
}
