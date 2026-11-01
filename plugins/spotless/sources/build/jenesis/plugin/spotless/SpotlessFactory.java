package build.jenesis.plugin.spotless;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class SpotlessFactory implements BuildModuleFactory {

    private static final String TARGET = "target", STEPS = "steps", SIZE = "size", REGEX = "regex",
            REPLACEMENT = "replacement", ORDER = "order", VERSION = "version", STYLE = "style",
            REFLOW = "reflowLongStrings", REORDER = "reorderImports", JAVADOC = "formatJavadoc",
            WRAP_STYLE = "wrapStyle", INDENT_STYLE = "indentStyle", INDENT_SIZE = "indentSize",
            LINE_LENGTH = "lineLength", CLOSING_PAREN = "closingParenOnNewLine", TRAILING_COMMAS = "trailingCommas",
            LANGUAGE_LEVEL = "javaLanguageLevel", MAX_WIDTH = "maxWidth", BLOCK_INDENT = "blockIndent",
            CONTINUATION_INDENT = "continuationIndent", UNUSED_IMPORTS = "removeUnusedImports",
            SCALA_VERSION = "scalaMajorVersion", HEADER = "header", DELIMITER = "delimiter";
    private static final Pattern SET = Pattern.compile("[a-z0-9-]+");
    private static final Set<String> OPEN = Set.of("ktlint", "scalafmt");
    private static final Map<String, Set<String>> OPTIONS = Map.ofEntries(
            Map.entry("trimTrailingWhitespace", Set.of()),
            Map.entry("endWithNewline", Set.of()),
            Map.entry("leadingTabsToSpaces", Set.of(SIZE)),
            Map.entry("leadingSpacesToTabs", Set.of(SIZE)),
            Map.entry("replaceRegex", Set.of(REGEX, REPLACEMENT)),
            Map.entry("importOrder", Set.of(ORDER)),
            Map.entry("formatAnnotations", Set.of()),
            Map.entry("googleJavaFormat", Set.of(VERSION, STYLE, REFLOW, REORDER, JAVADOC)),
            Map.entry("palantirJavaFormat", Set.of(VERSION, STYLE, JAVADOC)),
            Map.entry("removeUnusedImports", Set.of(VERSION)),
            Map.entry("princeOfSpace", Set.of(VERSION,
                    WRAP_STYLE,
                    INDENT_STYLE,
                    INDENT_SIZE,
                    LINE_LENGTH,
                    CLOSING_PAREN,
                    TRAILING_COMMAS,
                    LANGUAGE_LEVEL)),
            Map.entry("ktfmt", Set.of(VERSION, STYLE, MAX_WIDTH, BLOCK_INDENT, CONTINUATION_INDENT, UNUSED_IMPORTS)),
            Map.entry("ktlint", Set.of(VERSION)),
            Map.entry("scalafmt", Set.of(VERSION, SCALA_VERSION)),
            Map.entry("licenseHeader", Set.of(HEADER, DELIMITER)),
            Map.entry("jacksonJson", Set.of(VERSION)),
            Map.entry("jacksonYaml", Set.of(VERSION)),
            Map.entry("flexmark", Set.of(VERSION)));

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Permits permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        SequencedMap<String, String> targets = new LinkedHashMap<>(), steps = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> options = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String[] segments = entry.getKey().split("\\.", 3);
            if (segments.length < 2 || segments.length > 3 || !SET.matcher(segments[0]).matches()) {
                throw new IllegalArgumentException("The Spotless plugin takes no value " + entry.getKey()
                        + " - its values are <set>.target, <set>.steps and <set>.<step>.<option>, a set being named"
                        + " in lower case letters, digits and hyphens");
            }
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            if (segments.length == 3) {
                Set<String> taken = OPTIONS.get(segments[1]);
                if (taken == null) {
                    throw new IllegalArgumentException("The Spotless plugin names the step " + segments[1] + " in "
                            + entry.getKey() + ", but takes the steps " + new TreeSet<>(OPTIONS.keySet()));
                }
                if (!taken.contains(segments[2]) && !(OPEN.contains(segments[1]) && !segments[2].isEmpty())) {
                    throw new IllegalArgumentException("The Spotless step " + segments[1] + " takes no option "
                            + segments[2] + " in " + entry.getKey() + (taken.isEmpty()
                                    ? ", as it takes none"
                                    : " - its options are " + new TreeSet<>(taken)));
                }
                options.computeIfAbsent(segments[0], _ -> new HashMap<>())
                        .computeIfAbsent(segments[1], _ -> new HashMap<>())
                        .put(segments[2], entry.getValue() == null ? "" : entry.getValue());
            } else if (segments[1].equals(TARGET)) {
                targets.put(segments[0], value);
            } else if (segments[1].equals(STEPS)) {
                steps.put(segments[0], value);
            } else {
                throw new IllegalArgumentException("The Spotless plugin takes no value " + entry.getKey()
                        + " - a set takes " + segments[0] + "." + TARGET + " and " + segments[0] + "." + STEPS);
            }
        }
        SequencedSet<String> names = new LinkedHashSet<>(targets.sequencedKeySet());
        names.addAll(steps.sequencedKeySet());
        names.addAll(options.keySet());
        if (names.isEmpty()) {
            throw new IllegalArgumentException("The Spotless plugin formats no files - name a set of files as"
                    + " <set>.target=<glob>[,<glob>...] and its steps as <set>.steps=<step>[,<step>...], the steps"
                    + " being " + new TreeSet<>(OPTIONS.keySet()));
        }
        List<SpotlessSet> sets = new ArrayList<>();
        for (String name : names) {
            List<String> globs = split(targets.get(name)), named = split(steps.get(name));
            if (globs.isEmpty() || named.isEmpty()) {
                throw new IllegalArgumentException("The Spotless set " + name + " names no "
                        + (globs.isEmpty() ? TARGET : STEPS) + " - add " + name + "."
                        + (globs.isEmpty() ? TARGET + "=<glob>[,<glob>...]" : STEPS + "=<step>[,<step>...]"));
            }
            Map<String, Map<String, String>> configured = options.getOrDefault(name, Map.of());
            for (String step : configured.keySet()) {
                if (!named.contains(step)) {
                    throw new IllegalArgumentException("The Spotless set " + name + " configures the step " + step
                            + ", which " + name + "." + STEPS + " does not name");
                }
            }
            List<SpotlessStep> created = new ArrayList<>();
            for (String step : named) {
                Map<String, String> values = configured.getOrDefault(step, Map.of());
                created.add(switch (step) {
                    case "trimTrailingWhitespace" -> new SpotlessStep.TrimTrailingWhitespace();
                    case "endWithNewline" -> new SpotlessStep.EndWithNewline();
                    case "leadingTabsToSpaces" -> new SpotlessStep.LeadingTabsToSpaces(Objects.requireNonNullElse(number(name, step, values, SIZE, 1), 4));
                    case "leadingSpacesToTabs" -> new SpotlessStep.LeadingSpacesToTabs(Objects.requireNonNullElse(number(name, step, values, SIZE, 1), 4));
                    case "replaceRegex" -> {
                        String regex = values.get(REGEX);
                        if (regex == null || regex.isEmpty()) {
                            throw new IllegalArgumentException("The Spotless set " + name + " names the step " + step
                                    + " without a pattern - add " + name + "." + step + "." + REGEX + "=<pattern>");
                        }
                        try {
                            Pattern.compile(regex);
                        } catch (PatternSyntaxException e) {
                            throw new IllegalArgumentException("The Spotless set " + name + " names " + name + "."
                                    + step + "." + REGEX + "=" + regex + ", which is no regular expression: "
                                    + e.getDescription(), e);
                        }
                        yield new SpotlessStep.ReplaceRegex(regex, values.getOrDefault(REPLACEMENT, ""));
                    }
                    case "importOrder" -> new SpotlessStep.ImportOrder(split(values.get(ORDER)));
                    case "formatAnnotations" -> new SpotlessStep.FormatAnnotations();
                    case "googleJavaFormat" -> new SpotlessStep.GoogleJavaFormat(version(values),
                            choice(name, step, values, STYLE, List.of("GOOGLE", "AOSP")),
                            flag(name, step, values, REFLOW),
                            flag(name, step, values, REORDER),
                            flag(name, step, values, JAVADOC));
                    case "palantirJavaFormat" -> new SpotlessStep.PalantirJavaFormat(version(values),
                            choice(name, step, values, STYLE, List.of("PALANTIR", "AOSP", "GOOGLE")),
                            flag(name, step, values, JAVADOC));
                    case "removeUnusedImports" -> new SpotlessStep.RemoveUnusedImports(version(values));
                    case "princeOfSpace" -> new SpotlessStep.PrinceOfSpace(version(values),
                            choice(name, step, values, WRAP_STYLE, List.of("balanced", "wide", "narrow")),
                            choice(name, step, values, INDENT_STYLE, List.of("spaces", "tabs")),
                            number(name, step, values, INDENT_SIZE, 1),
                            number(name, step, values, LINE_LENGTH, 1),
                            flag(name, step, values, CLOSING_PAREN),
                            flag(name, step, values, TRAILING_COMMAS),
                            number(name, step, values, LANGUAGE_LEVEL, 8));
                    case "ktfmt" -> new SpotlessStep.Ktfmt(version(values),
                            choice(name, step, values, STYLE, List.of("META", "GOOGLE", "KOTLINLANG")),
                            number(name, step, values, MAX_WIDTH, 1),
                            number(name, step, values, BLOCK_INDENT, 1),
                            number(name, step, values, CONTINUATION_INDENT, 1),
                            flag(name, step, values, UNUSED_IMPORTS));
                    case "ktlint" -> new SpotlessStep.Ktlint(version(values), open(values, Set.of(VERSION)));
                    case "scalafmt" -> {
                        String version = version(values);
                        if (version == null) {
                            throw new IllegalArgumentException("The Spotless set " + name + " names the step " + step
                                    + " without a version, which scalafmt's configuration must name - add " + name
                                    + "." + step + "." + VERSION + "=<version>");
                        }
                        yield new SpotlessStep.Scalafmt(version,
                                values.containsKey(SCALA_VERSION) ? values.get(SCALA_VERSION).trim() : null,
                                open(values, Set.of(VERSION, SCALA_VERSION)));
                    }
                    case "licenseHeader" -> {
                        String header = values.get(HEADER);
                        if (header == null || header.isBlank()) {
                            throw new IllegalArgumentException("The Spotless set " + name + " names the step " + step
                                    + " without a header - add " + name + "." + step + "." + HEADER
                                    + "=<the header, lines separated by \\n>");
                        }
                        String delimiter = values.get(DELIMITER);
                        yield new SpotlessStep.LicenseHeader(header,
                                delimiter == null || delimiter.isBlank() ? null : delimiter);
                    }
                    case "jacksonJson" -> new SpotlessStep.JacksonJson(version(values));
                    case "jacksonYaml" -> new SpotlessStep.JacksonYaml(version(values));
                    case "flexmark" -> new SpotlessStep.Flexmark(version(values));
                    default -> throw new IllegalArgumentException("The Spotless set " + name + " names the step "
                            + step + ", but takes the steps " + new TreeSet<>(OPTIONS.keySet()));
                });
            }
            sets.add(new SpotlessSet(name, globs, List.copyOf(created)));
        }
        return SpotlessModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group)
                .sets(List.copyOf(sets));
    }

    private static List<String> split(String value) {
        return value == null ? List.of() : Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(element -> !element.isEmpty())
                .toList();
    }

    private static TreeMap<String, String> open(Map<String, String> values, Set<String> known) {
        TreeMap<String, String> open = new TreeMap<>(values);
        open.keySet().removeAll(known);
        return open;
    }

    private static Integer number(String name, String step, Map<String, String> values, String key, int minimum) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number >= minimum) {
                return number;
            }
        } catch (NumberFormatException _) {
        }
        throw new IllegalArgumentException("The Spotless set " + name + " names " + name + "." + step + "." + key
                + "=" + value.trim() + ", but takes a whole number of at least " + minimum);
    }

    private static String version(Map<String, String> values) {
        String version = values.get(VERSION);
        return version == null || version.isBlank() ? null : version.trim();
    }

    private static String choice(String name, String step, Map<String, String> values, String key, List<String> choices) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!choices.contains(value.trim())) {
            throw new IllegalArgumentException("The Spotless set " + name + " names " + name + "." + step + "." + key
                    + "=" + value.trim() + ", but takes one of " + choices);
        }
        return value.trim();
    }

    private static Boolean flag(String name, String step, Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) {
            return null;
        }
        return switch (value.trim()) {
            case "", "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("The Spotless set " + name + " names " + name + "." + step
                    + "." + key + "=" + value.trim() + ", but takes true or false");
        };
    }
}
