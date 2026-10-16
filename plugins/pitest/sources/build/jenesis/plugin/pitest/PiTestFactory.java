package build.jenesis.plugin.pitest;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class PiTestFactory implements BuildModuleFactory {

    private static final String TARGET_CLASSES = "targetClasses",
            TARGET_TESTS = "targetTests",
            OUTPUT_FORMATS = "outputFormats",
            MUTATORS = "mutators",
            FAIL_WHEN_NO_MUTATIONS = "failWhenNoMutations";
    private static final List<String> KEYS = List.of(TARGET_CLASSES,
            TARGET_TESTS,
            OUTPUT_FORMATS,
            MUTATORS,
            FAIL_WHEN_NO_MUTATIONS);

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
                throw new IllegalArgumentException("The PIT plugin names " + entry.getKey() + ", but takes one of "
                        + KEYS);
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        PiTestModule module = PiTestModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool");
        List<String> targetClasses = values.entries(TARGET_CLASSES), targetTests = values.entries(TARGET_TESTS);
        List<String> outputFormats = values.entries(OUTPUT_FORMATS), mutators = values.entries(MUTATORS);
        module = targetClasses == null ? module : module.targetClasses(targetClasses);
        module = targetTests == null ? module : module.targetTests(targetTests);
        module = mutators == null ? module : module.mutators(mutators);
        if (outputFormats != null) {
            List<PiTestModule.OutputFormat> formats = new ArrayList<>();
            for (String format : outputFormats) {
                formats.add(Arrays.stream(PiTestModule.OutputFormat.values())
                        .filter(candidate -> candidate.name().equalsIgnoreCase(format))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The PIT plugin names the format " + format
                                + " of " + OUTPUT_FORMATS + ", but takes some of "
                                + List.of(PiTestModule.OutputFormat.values()))));
            }
            module = module.outputFormats(formats);
        }
        String failWhenNoMutations = values.getProperty(FAIL_WHEN_NO_MUTATIONS);
        if (failWhenNoMutations != null) {
            try {
                module = module.failWhenNoMutations(values.flag(FAIL_WHEN_NO_MUTATIONS));
            } catch (IllegalArgumentException _) {
                throw new IllegalArgumentException("The PIT plugin names the value " + failWhenNoMutations + " of "
                        + FAIL_WHEN_NO_MUTATIONS + ", which is no flag, expected true, false, or the value left empty");
            }
        }
        return module;
    }
}
