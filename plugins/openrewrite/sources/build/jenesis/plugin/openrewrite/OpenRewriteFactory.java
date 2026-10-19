package build.jenesis.plugin.openrewrite;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class OpenRewriteFactory implements BuildModuleFactory {

    private static final String RECIPES = "recipes";

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!entry.getKey().equals(RECIPES)) {
                throw new IllegalArgumentException("The OpenRewrite plugin takes no value " + entry.getKey()
                        + " - its one value is " + RECIPES + ", the comma-separated recipes to run, which "
                        + OpenRewriteModule.CONFIGURATION + " can declare, and jenesis.format.rewrite applies them");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        List<String> recipes = values.entries(RECIPES);
        if (recipes == null || recipes.isEmpty()) {
            throw new IllegalArgumentException("The OpenRewrite plugin names no recipe - write " + RECIPES
                    + "=<recipe>[,<recipe>...], such as " + RECIPES + "=org.openrewrite.java.RemoveUnusedImports, or a"
                    + " recipe that " + OpenRewriteModule.CONFIGURATION + " declares");
        }
        return OpenRewriteModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool")
                .recipes(recipes);
    }
}
