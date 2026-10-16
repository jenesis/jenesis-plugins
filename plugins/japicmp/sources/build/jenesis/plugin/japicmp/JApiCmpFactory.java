package build.jenesis.plugin.japicmp;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class JApiCmpFactory implements BuildModuleFactory {

    private static final String BASELINE = "baseline", ERROR_ON = "error-on-";
    private static final Set<String> KEYS = Set.of(BASELINE,
            "access",
            "include",
            "exclude",
            "format",
            "ignore-missing-classes",
            "only-incompatible",
            "only-modified",
            "semantic-versioning",
            "error-on-binary-incompatibility",
            "error-on-source-incompatibility",
            "error-on-modifications",
            "error-on-semantic-incompatibility");

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        String origin = "The japicmp plugin";
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey())) {
                throw new IllegalArgumentException(origin + " names " + entry.getKey()
                        + ", which japicmp does not take (expected one of " + new TreeSet<>(KEYS) + ")");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        JApiCmpModule module = JApiCmpModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool");
        String access = values.value("access");
        List<String> include = patterns(values.value("include")),
                exclude = patterns(values.value("exclude")),
                formats = values.entries("format");
        if (access != null) {
            module = module.access(named(JApiCmpModule.Access.class, origin, "access", access));
        }
        if (include != null) {
            module = module.include(include);
        }
        if (exclude != null) {
            module = module.exclude(exclude);
        }
        if (formats != null) {
            Set<JApiCmpModule.Format> selected = EnumSet.noneOf(JApiCmpModule.Format.class);
            for (String format : formats) {
                selected.add(named(JApiCmpModule.Format.class, origin, "format", format));
            }
            module = module.formats(selected);
        }
        Set<JApiCmpModule.Failure> errorOn = EnumSet.noneOf(JApiCmpModule.Failure.class);
        for (JApiCmpModule.Failure failure : JApiCmpModule.Failure.values()) {
            if (values.flag(ERROR_ON + spelt(failure))) {
                errorOn.add(failure);
            }
        }
        return module.baseline(values.value(BASELINE))
                .ignoreMissingClasses(values.flag("ignore-missing-classes", true))
                .onlyIncompatible(values.flag("only-incompatible"))
                .onlyModified(values.flag("only-modified"))
                .semanticVersioning(values.flag("semantic-versioning"))
                .errorOn(errorOn);
    }

    private static <E extends Enum<E>> E named(Class<E> type, String origin, String key, String value) {
        for (E constant : type.getEnumConstants()) {
            if (spelt(constant).equals(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(origin + " names the " + key + " " + value + ", but japicmp takes one of "
                + Arrays.stream(type.getEnumConstants()).map(JApiCmpFactory::spelt).toList());
    }

    private static String spelt(Enum<?> constant) {
        return constant.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static List<String> patterns(String value) {
        if (value == null) {
            return null;
        }
        List<String> patterns = new ArrayList<>();
        int depth = 0, start = 0;
        for (int index = 0; index <= value.length(); index++) {
            char character = index == value.length() ? ',' : value.charAt(index);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            } else if ((character == ',' || character == ';') && depth == 0) {
                String pattern = value.substring(start, index).strip();
                if (!pattern.isEmpty()) {
                    patterns.add(pattern);
                }
                start = index + 1;
            }
        }
        return patterns;
    }
}
