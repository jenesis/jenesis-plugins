package build.jenesis.plugin.ktfmt;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class KtfmtFormatFactory implements BuildModuleFactory {

    private static final String STYLE = "style", IMPORTS = "imports";
    private static final String SORT = "sort", REMOVE = "remove";
    private static final Set<String> KEYS = Set.of(STYLE, IMPORTS);

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException("The ktfmt plugin takes no value " + entry.getKey()
                        + " - its values are " + new TreeSet<>(KEYS)
                        + ", and jenesis.format.rewrite rewrites the sources");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        List<String> imports = values.entries(IMPORTS);
        if (imports != null && !imports.equals(List.of(SORT)) && !Set.copyOf(imports).equals(Set.of(SORT, REMOVE))) {
            throw new IllegalArgumentException("The ktfmt plugin names " + IMPORTS + "=" + values.value(IMPORTS)
                    + ", but ktfmt always sorts the imports - " + SORT + "," + REMOVE
                    + " to remove the unused ones as well, or " + SORT + " to keep them");
        }
        return KtfmtFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group)
                .style(switch (values.value(STYLE, "meta")) {
                    case "meta" -> KtfmtFormatModule.Style.META;
                    case "google" -> KtfmtFormatModule.Style.GOOGLE;
                    case "kotlinlang" -> KtfmtFormatModule.Style.KOTLINLANG;
                    default -> throw new IllegalArgumentException("The ktfmt plugin names " + STYLE + "="
                            + values.value(STYLE) + ", which ktfmt does not know - meta, google or kotlinlang");
                })
                .removeUnusedImports(imports == null || imports.contains(REMOVE));
    }
}
