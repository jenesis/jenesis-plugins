package build.jenesis.plugin.asciidoctor;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class AsciidoctorFactory implements BuildModuleFactory {

    private static final String BACKEND = "backend", OUTPUT = "output", ATTRIBUTE = "attribute.";
    private static final Set<String> BACKENDS = Set.of("html5", "docbook5");

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        String backend = "html5", output = "docs";
        SequencedMap<String, String> attributes = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String value = entry.getValue().strip();
            if (entry.getKey().equals(BACKEND)) {
                if (!BACKENDS.contains(value)) {
                    throw new IllegalArgumentException("The Asciidoctor plugin names " + BACKEND + "=" + value
                            + ", which Asciidoctor does not convert to - " + new TreeSet<>(BACKENDS));
                }
                backend = value;
            } else if (entry.getKey().equals(OUTPUT)) {
                Path folder = Path.of(value).normalize();
                if (value.isEmpty() || folder.isAbsolute() || folder.startsWith("..")) {
                    throw new IllegalArgumentException("The Asciidoctor plugin names " + OUTPUT + "=" + value
                            + ", which is no folder within the project's staged files - name a relative folder,"
                            + " such as " + OUTPUT + "=docs");
                }
                output = folder.toString();
            } else if (entry.getKey().startsWith(ATTRIBUTE) && entry.getKey().length() > ATTRIBUTE.length()) {
                attributes.put(entry.getKey().substring(ATTRIBUTE.length()), value);
            } else {
                throw new IllegalArgumentException("The Asciidoctor plugin takes no value " + entry.getKey()
                        + " - its values are " + BACKEND + ", " + OUTPUT + " and " + ATTRIBUTE + "<name>");
            }
        }
        return AsciidoctorModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool")
                .backend(backend)
                .output(output)
                .attributes(attributes);
    }
}
