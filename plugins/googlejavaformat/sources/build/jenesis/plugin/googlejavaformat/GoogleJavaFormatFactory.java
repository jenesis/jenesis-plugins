package build.jenesis.plugin.googlejavaformat;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class GoogleJavaFormatFactory implements BuildModuleFactory {

    private static final String STYLE = "style", REFLOW = "reflow", JAVADOC = "javadoc", IMPORTS = "imports";
    private static final String SORT = "sort", REMOVE = "remove", NONE = "none";
    private static final Set<String> KEYS = Set.of(STYLE, REFLOW, JAVADOC, IMPORTS);

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
                throw new IllegalArgumentException("The google-java-format plugin takes no value " + entry.getKey()
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
            throw new IllegalArgumentException("The google-java-format plugin names " + IMPORTS + "="
                    + values.value(IMPORTS) + ", but google-java-format takes " + SORT + ", " + REMOVE
                    + " or both, comma-separated, for what it does to the imports, or " + NONE);
        }
        return GoogleJavaFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group)
                .aosp(switch (values.value(STYLE, "google")) {
                    case "google" -> false;
                    case "aosp" -> true;
                    default -> throw new IllegalArgumentException("The google-java-format plugin names " + STYLE + "="
                            + values.value(STYLE) + ", which google-java-format does not know - google or aosp");
                })
                .reflow(values.flag(REFLOW, true))
                .javadoc(values.flag(JAVADOC, true))
                .sortImports(imports.contains(SORT))
                .removeUnusedImports(imports.contains(REMOVE));
    }
}
