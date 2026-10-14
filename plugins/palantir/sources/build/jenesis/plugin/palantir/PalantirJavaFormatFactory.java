package build.jenesis.plugin.palantir;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class PalantirJavaFormatFactory implements BuildModuleFactory {

    private static final String REFLOW = "reflow", IMPORTS = "imports";
    private static final String SORT = "sort", REMOVE = "remove", NONE = "none";
    private static final Set<String> KEYS = Set.of(REFLOW, IMPORTS);

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
                throw new IllegalArgumentException("The Palantir Java Format plugin takes no value " + entry.getKey()
                        + " - its values are " + new TreeSet<>(KEYS)
                        + ", and jenesis.format.rewrite rewrites the sources");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        List<String> imports = values.entries(IMPORTS);
        if (imports == null) {
            imports = List.of(SORT, REMOVE);
        } else if (imports.equals(List.of(NONE))) {
            imports = List.of();
        } else if (!Set.of(SORT, REMOVE).containsAll(imports)) {
            throw new IllegalArgumentException("The Palantir Java Format plugin names " + IMPORTS + "="
                    + values.value(IMPORTS) + ", but palantir-java-format takes " + SORT + ", " + REMOVE
                    + " or both, comma-separated, for what it does to the imports, or " + NONE);
        }
        return PalantirJavaFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group)
                .reflow(values.flag(REFLOW, true))
                .sortImports(imports.contains(SORT))
                .removeUnusedImports(imports.contains(REMOVE));
    }
}
