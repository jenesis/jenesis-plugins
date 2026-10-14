package build.jenesis.plugin.scalafmt;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.BuildModuleName;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

@BuildModuleName("format")
public class ScalafmtFormatFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The scalafmt formatter takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.format.rewrite instead");
        }
        return ScalafmtFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group);
    }
}
