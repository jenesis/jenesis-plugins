package build.jenesis.plugin.forbiddenapis;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class ForbiddenApisFactory implements BuildModuleFactory {

    private static final String BUNDLED = "bundled", SIGNATURES = "signatures", SUPPRESS = "suppress",
            INCLUDES = "includes", EXCLUDES = "excludes", WARN = "warn", ALLOW_MISSING_CLASSES = "allowmissingclasses",
            IGNORE_SIGNATURES_OF_MISSING_CLASSES = "ignoresignaturesofmissingclasses";
    private static final Set<String> KEYS = Set.of(BUNDLED, SIGNATURES, SUPPRESS, INCLUDES, EXCLUDES, WARN,
            ALLOW_MISSING_CLASSES, IGNORE_SIGNATURES_OF_MISSING_CLASSES);

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
                throw new IllegalArgumentException("The forbiddenapis plugin names " + entry.getKey() + ", but takes "
                        + String.join(", ", new TreeSet<>(KEYS)));
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        List<String> bundled = Objects.requireNonNullElse(values.entries(BUNDLED), List.of()),
                signatures = Objects.requireNonNullElse(values.entries(SIGNATURES), List.of());
        if (bundled.isEmpty() && signatures.isEmpty()) {
            throw new IllegalArgumentException("The forbiddenapis plugin names no signatures - set " + BUNDLED
                    + "=<bundled signatures, as jdk-unsafe,jdk-deprecated> or " + SIGNATURES
                    + "=<files of signatures bound as @<input>/<file>=<path>>");
        }
        return ForbiddenApisModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group)
                .bundled(bundled)
                .signatures(signatures.stream().map(Path::of).toList())
                .suppress(Objects.requireNonNullElse(values.entries(SUPPRESS), List.of()))
                .includes(Objects.requireNonNullElse(values.entries(INCLUDES), List.of()))
                .excludes(Objects.requireNonNullElse(values.entries(EXCLUDES), List.of()))
                .warn(Objects.requireNonNullElse(values.entries(WARN), List.of()))
                .allowMissingClasses(values.flag(ALLOW_MISSING_CLASSES, false))
                .ignoreSignaturesOfMissingClasses(values.flag(IGNORE_SIGNATURES_OF_MISSING_CLASSES, false));
    }
}
