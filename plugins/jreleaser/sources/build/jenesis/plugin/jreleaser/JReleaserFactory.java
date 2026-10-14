package build.jenesis.plugin.jreleaser;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class JReleaserFactory implements BuildModuleFactory {

    private static final String COMMAND = "command", SETTING = "JRELEASER_";

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        JReleaserModule module = JReleaserModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group);
        SequencedMap<String, String> settings = new LinkedHashMap<>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String value = property.getValue().strip();
            switch (property.getKey()) {
                case COMMAND -> module = module.command(value);
                default -> {
                    if (!property.getKey().startsWith(SETTING)) {
                        throw new IllegalArgumentException("The JReleaser plugin names " + property.getKey()
                                + ", but takes " + COMMAND + "=<command> and the " + SETTING + "* settings JReleaser"
                                + " reads, reads its configuration from one of " + JReleaserModule.CONFIGURATIONS
                                + " it is handed as @jreleaser=<file>, and runs with --dry-run unless the command line"
                                + " passes -Djenesis.release.publish=true");
                    }
                    settings.put(property.getKey(), value);
                }
            }
        }
        return module.settings(settings);
    }
}
