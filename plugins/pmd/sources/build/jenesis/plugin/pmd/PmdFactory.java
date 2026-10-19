package build.jenesis.plugin.pmd;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class PmdFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The PMD plugin takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.source.pmd and jenesis.pmd.priority instead");
        }
        return PmdModule.ofEnvironment(environment, repositories, resolvers).permits(permits).tool(group + ":tool");
    }
}
