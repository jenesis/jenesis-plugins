package build.jenesis.plugin.scalafmt;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.BuildModuleName;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Enforcement;

@BuildModuleName("check")
public class ScalafmtFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The scalafmt check takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.source.scalafmt instead");
        }
        if (Enforcement.ofEnvironment(environment, ScalafmtModule.SETTING) == Enforcement.IGNORE) {
            return (_, _) -> { };
        }
        return ScalafmtModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
    }
}
