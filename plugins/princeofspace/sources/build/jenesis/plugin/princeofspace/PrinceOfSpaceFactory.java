package build.jenesis.plugin.princeofspace;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class PrinceOfSpaceFactory implements BuildModuleFactory {

    private static final String WRAP_STYLE = "wrap-style",
            INDENT_STYLE = "indent-style",
            INDENT_SIZE = "indent-size",
            LINE_LENGTH = "line-length",
            CLOSING_PAREN_ON_NEW_LINE = "closing-paren-on-new-line",
            TRAILING_COMMAS = "trailing-commas",
            JAVA_VERSION = "java-version";
    private static final Set<String> KEYS = Set.of(WRAP_STYLE,
            INDENT_STYLE,
            INDENT_SIZE,
            LINE_LENGTH,
            CLOSING_PAREN_ON_NEW_LINE,
            TRAILING_COMMAS,
            JAVA_VERSION);
    private static final List<String> WRAP_STYLES = List.of("balanced", "wide", "narrow"),
            INDENT_STYLES = List.of("spaces", "tabs");

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
                throw new IllegalArgumentException("The Prince of Space plugin takes no value " + entry.getKey()
                        + " - its values are " + new TreeSet<>(KEYS)
                        + ", and jenesis.format.rewrite rewrites the sources");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return PrinceOfSpaceModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group + ":tool")
                .wrapStyle(choice(values, WRAP_STYLE, WRAP_STYLES))
                .indentStyle(choice(values, INDENT_STYLE, INDENT_STYLES))
                .indentSize(number(values, INDENT_SIZE, 4, 1))
                .lineLength(number(values, LINE_LENGTH, 120, 1))
                .closingParenOnNewLine(values.flag(CLOSING_PAREN_ON_NEW_LINE, true))
                .trailingCommas(values.flag(TRAILING_COMMAS, false))
                .javaVersion(number(values, JAVA_VERSION, 17, 8));
    }

    private static String choice(SequencedProperties values, String key, List<String> choices) {
        String value = values.value(key);
        if (value == null) {
            return choices.getFirst();
        }
        if (!choices.contains(value)) {
            throw new IllegalArgumentException("The Prince of Space plugin names " + key + "=" + value
                    + ", but takes one of " + choices);
        }
        return value;
    }

    private static int number(SequencedProperties values, String key, int fallback, int minimum) {
        String value = values.value(key);
        if (value == null) {
            return fallback;
        }
        try {
            int number = Integer.parseInt(value);
            if (number >= minimum) {
                return number;
            }
        } catch (NumberFormatException _) {
        }
        throw new IllegalArgumentException("The Prince of Space plugin names " + key + "=" + value
                + ", but takes a whole number of at least " + minimum);
    }
}
