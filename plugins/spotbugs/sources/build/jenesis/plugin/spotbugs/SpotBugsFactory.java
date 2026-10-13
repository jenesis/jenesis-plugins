package build.jenesis.plugin.spotbugs;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Enforcement;

public class SpotBugsFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The SpotBugs plugin takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.validator.spotbugs instead");
        }
        if (Enforcement.ofEnvironment(environment, SpotBugsModule.SETTING) == Enforcement.IGNORE) {
            return (_, _) -> { };
        }
        return SpotBugsModule.ofEnvironment(environment, repositories, resolvers).permits(permits).tool(group);
    }
}
