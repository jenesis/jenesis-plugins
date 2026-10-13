package build.jenesis.plugin.openapi;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class OpenApiFactory implements BuildModuleFactory {

    private static final Set<String> KEYS = Set.of("generator", "package", "sources", "arguments");

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
                throw new IllegalArgumentException("The OpenAPI plugin takes no value " + entry.getKey()
                        + ": name generator, the OpenAPI Generator to run, package, the package of the generated"
                        + " sources, sources, the folder of the generated project they are collected from, or"
                        + " arguments, those handed to the tool");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        OpenApiModule module = OpenApiModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .packageName(values.value("package"))
                .arguments(values.words("arguments"));
        String generator = values.value("generator");
        if (generator != null) {
            module = module.generator(generator);
        }
        String sources = values.value("sources");
        return sources == null ? module : module.sourceFolder(sources);
    }
}
