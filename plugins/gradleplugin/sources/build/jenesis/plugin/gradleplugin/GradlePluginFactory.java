package build.jenesis.plugin.gradleplugin;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class GradlePluginFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The Gradle plugin plugin names "
                    + String.join(", ", properties.keySet()) + ", but takes no values - it reads the plugins of every"
                    + " staged jar from its META-INF/gradle-plugins");
        }
        return new GradlePluginModule();
    }
}
