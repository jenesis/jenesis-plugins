package build.jenesis.plugin.mavenplugin;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class MavenPluginFactory implements BuildModuleFactory {

    private static final String GOAL_PREFIX = "goalprefix", MAVEN = "maven", TOOLS = "tools";
    private static final Set<String> KEYS = Set.of(GOAL_PREFIX, MAVEN, TOOLS);

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException("The Maven plugin plugin names " + entry.getKey() + ", but takes "
                        + String.join(", ", new TreeSet<>(KEYS)));
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        MavenPluginModule module = MavenPluginModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool")
                .goalPrefix(values.value(GOAL_PREFIX));
        if (values.value(MAVEN) != null) {
            module = module.maven(values.value(MAVEN));
        }
        if (values.value(TOOLS) != null) {
            module = module.tools(values.value(TOOLS));
        }
        return module;
    }
}
