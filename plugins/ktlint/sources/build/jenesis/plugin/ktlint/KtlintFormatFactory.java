package build.jenesis.plugin.ktlint;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.BuildModuleName;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;

@BuildModuleName("format")
public class KtlintFormatFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Path configuration,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The ktlint formatter takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.format.rewrite instead");
        }
        return KtlintFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group);
    }
}
