package build.jenesis.plugin.detekt;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Enforcement;

public class DetektFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The detekt plugin takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.source.detekt instead");
        }
        if (Enforcement.ofEnvironment(environment, DetektModule.SETTING) == Enforcement.IGNORE) {
            return (_, _) -> { };
        }
        return DetektModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
    }
}
