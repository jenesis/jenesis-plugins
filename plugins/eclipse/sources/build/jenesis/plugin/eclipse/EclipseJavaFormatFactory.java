package build.jenesis.plugin.eclipse;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class EclipseJavaFormatFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The Eclipse formatter plugin takes no values, but was handed "
                    + properties.sequencedKeySet() + ": bind the formatter profile exported from Eclipse as @profile/"
                    + EclipseJavaFormatModule.PROFILE + "=<file>, and set jenesis.format.rewrite to rewrite the sources");
        }
        return EclipseJavaFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group);
    }
}
