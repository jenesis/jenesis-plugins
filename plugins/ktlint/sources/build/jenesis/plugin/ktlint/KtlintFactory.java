package build.jenesis.plugin.ktlint;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.BuildModuleName;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Enforcement;

@BuildModuleName("check")
public class KtlintFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The ktlint check takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.source.ktlint instead");
        }
        if (Enforcement.ofEnvironment(environment, KtlintModule.SETTING) == Enforcement.IGNORE) {
            return (_, _) -> { };
        }
        return KtlintModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
    }
}
