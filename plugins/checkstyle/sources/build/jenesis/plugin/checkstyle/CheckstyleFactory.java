package build.jenesis.plugin.checkstyle;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class CheckstyleFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        return CheckstyleModule.ofEnvironment(environment, repositories, resolvers)
                .expansions(properties)
                .permits(permits)
                .tool(group);
    }
}
