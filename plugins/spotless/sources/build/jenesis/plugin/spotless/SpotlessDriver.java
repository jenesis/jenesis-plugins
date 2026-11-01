package build.jenesis.plugin.spotless;

import module java.base;
import com.diffplug.spotless.Formatter;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.LineEnding;
import com.diffplug.spotless.Provisioner;
import com.diffplug.spotless.generic.EndWithNewlineStep;
import com.diffplug.spotless.generic.IndentStep;
import com.diffplug.spotless.generic.LicenseHeaderStep;
import com.diffplug.spotless.generic.ReplaceRegexStep;
import com.diffplug.spotless.generic.TrimTrailingWhitespaceStep;
import com.diffplug.spotless.java.FormatAnnotationsStep;
import com.diffplug.spotless.java.GoogleJavaFormatStep;
import com.diffplug.spotless.java.ImportOrderStep;
import com.diffplug.spotless.java.PalantirJavaFormatStep;
import com.diffplug.spotless.java.PrinceOfSpaceStep;
import com.diffplug.spotless.java.RemoveUnusedImportsStep;
import com.diffplug.spotless.json.JacksonJsonConfig;
import com.diffplug.spotless.kotlin.KtLintStep;
import com.diffplug.spotless.kotlin.KtfmtStep;
import com.diffplug.spotless.json.JacksonJsonStep;
import com.diffplug.spotless.markdown.FlexmarkConfig;
import com.diffplug.spotless.markdown.FlexmarkStep;
import com.diffplug.spotless.scala.ScalaFmtStep;
import com.diffplug.spotless.yaml.JacksonYamlConfig;
import com.diffplug.spotless.yaml.JacksonYamlStep;

public class SpotlessDriver {

    public static int main(String[] args) throws IOException {
        boolean check = false, listing = false;
        Map<String, Set<File>> provisions = new HashMap<>();
        List<String> files = new ArrayList<>();
        SequencedMap<String, List<PathMatcher>> targets = new LinkedHashMap<>();
        SequencedMap<String, List<Map.Entry<String, Map<String, String>>>> steps = new LinkedHashMap<>();
        String set = null;
        Map<String, String> options = null;
        for (String argument : args) {
            int split = argument.indexOf('=');
            if (listing) {
                files.add(argument);
            } else if (argument.equals("--")) {
                listing = true;
            } else if (argument.equals("--check")) {
                check = true;
            } else if (argument.startsWith("--provision=") && argument.indexOf('=', split + 1) > 0) {
                int divider = argument.indexOf('=', split + 1);
                Set<File> jars = new LinkedHashSet<>();
                for (String jar : argument.substring(divider + 1).split(File.pathSeparator)) {
                    if (!jar.isEmpty()) {
                        jars.add(new File(jar));
                    }
                }
                provisions.put(argument.substring(split + 1, divider), jars);
            } else if (argument.startsWith("--set=")) {
                set = argument.substring(split + 1);
                targets.put(set, new ArrayList<>());
                steps.put(set, new ArrayList<>());
                options = null;
            } else if (argument.startsWith("--target=") && set != null) {
                targets.get(set).add(FileSystems.getDefault().getPathMatcher("glob:" + argument.substring(split + 1)));
            } else if (argument.startsWith("--step=") && set != null) {
                options = new LinkedHashMap<>();
                steps.get(set).add(Map.entry(argument.substring(split + 1), options));
            } else if (argument.startsWith("--") && options != null && split > 2) {
                StringBuilder value = new StringBuilder();
                for (int index = split + 1; index < argument.length(); index++) {
                    char character = argument.charAt(index);
                    if (character == '\\' && index + 1 < argument.length()) {
                        char escaped = argument.charAt(++index);
                        value.append(escaped == 'n' ? '\n' : escaped == 'r' ? '\r' : escaped);
                    } else {
                        value.append(character);
                    }
                }
                options.put(argument.substring(2, split), value.toString());
            } else {
                System.err.println("Unexpected argument " + argument);
                return 2;
            }
        }
        SequencedMap<String, Formatter> formatters = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map.Entry<String, Map<String, String>>>> entry : steps.entrySet()) {
            List<FormatterStep> created = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> step : entry.getValue()) {
                String name = step.getKey();
                Map<String, String> values = step.getValue();
                Provisioner provisioner = (transitives, coordinates) -> {
                    Set<File> jars = provisions.get(name.toLowerCase(Locale.ROOT));
                    if (jars == null) {
                        throw new IllegalStateException("No jars were resolved for the Spotless step " + name
                                + ", which asks for " + coordinates);
                    }
                    return jars;
                };
                created.add(switch (name) {
                    case "trimTrailingWhitespace" -> TrimTrailingWhitespaceStep.create();
                    case "endWithNewline" -> EndWithNewlineStep.create();
                    case "leadingTabsToSpaces" -> IndentStep.Type.SPACE.create(Integer.parseInt(values.get("size")));
                    case "leadingSpacesToTabs" -> IndentStep.Type.TAB.create(Integer.parseInt(values.get("size")));
                    case "replaceRegex" -> ReplaceRegexStep.create(entry.getKey(),
                            values.get("regex"),
                            values.getOrDefault("replacement", ""));
                    case "importOrder" -> ImportOrderStep.forJava().createFrom(values.containsKey("order")
                            ? values.get("order").split(",")
                            : new String[0]);
                    case "formatAnnotations" -> FormatAnnotationsStep.create();
                    case "googleJavaFormat" -> GoogleJavaFormatStep.create(GoogleJavaFormatStep.defaultGroupArtifact(),
                            values.getOrDefault("version", GoogleJavaFormatStep.defaultVersion()),
                            values.getOrDefault("style", GoogleJavaFormatStep.defaultStyle()),
                            provisioner,
                            values.containsKey("reflowLongStrings")
                                    ? Boolean.parseBoolean(values.get("reflowLongStrings"))
                                    : GoogleJavaFormatStep.defaultReflowLongStrings(),
                            values.containsKey("reorderImports")
                                    ? Boolean.parseBoolean(values.get("reorderImports"))
                                    : GoogleJavaFormatStep.defaultReorderImports(),
                            values.containsKey("formatJavadoc")
                                    ? Boolean.parseBoolean(values.get("formatJavadoc"))
                                    : GoogleJavaFormatStep.defaultFormatJavadoc());
                    case "palantirJavaFormat" -> PalantirJavaFormatStep.create(
                            values.getOrDefault("version", PalantirJavaFormatStep.defaultVersion()),
                            values.getOrDefault("style", PalantirJavaFormatStep.defaultStyle()),
                            values.containsKey("formatJavadoc")
                                    ? Boolean.parseBoolean(values.get("formatJavadoc"))
                                    : PalantirJavaFormatStep.defaultFormatJavadoc(),
                            provisioner);
                    case "removeUnusedImports" -> RemoveUnusedImportsStep.create(provisioner);
                    case "princeOfSpace" -> {
                        PrinceOfSpaceStep.Options settings = new PrinceOfSpaceStep.Options();
                        if (values.containsKey("wrapStyle")) {
                            settings.setWrapStyle(values.get("wrapStyle"));
                        }
                        if (values.containsKey("indentStyle")) {
                            settings.setIndentStyle(values.get("indentStyle"));
                        }
                        if (values.containsKey("indentSize")) {
                            settings.setIndentSize(Integer.parseInt(values.get("indentSize")));
                        }
                        if (values.containsKey("lineLength")) {
                            settings.setLineLength(Integer.parseInt(values.get("lineLength")));
                        }
                        if (values.containsKey("closingParenOnNewLine")) {
                            settings.setClosingParenOnNewLine(Boolean.parseBoolean(values.get("closingParenOnNewLine")));
                        }
                        if (values.containsKey("trailingCommas")) {
                            settings.setTrailingCommas(Boolean.parseBoolean(values.get("trailingCommas")));
                        }
                        if (values.containsKey("javaLanguageLevel")) {
                            settings.setJavaLanguageLevel(Integer.parseInt(values.get("javaLanguageLevel")));
                        }
                        yield PrinceOfSpaceStep.create(values.getOrDefault("version", PrinceOfSpaceStep.defaultVersion()),
                                provisioner,
                                settings);
                    }
                    case "ktfmt" -> {
                        KtfmtStep.KtfmtFormattingOptions settings = new KtfmtStep.KtfmtFormattingOptions();
                        if (values.containsKey("maxWidth")) {
                            settings.setMaxWidth(Integer.parseInt(values.get("maxWidth")));
                        }
                        if (values.containsKey("blockIndent")) {
                            settings.setBlockIndent(Integer.parseInt(values.get("blockIndent")));
                        }
                        if (values.containsKey("continuationIndent")) {
                            settings.setContinuationIndent(Integer.parseInt(values.get("continuationIndent")));
                        }
                        if (values.containsKey("removeUnusedImports")) {
                            settings.setRemoveUnusedImports(Boolean.parseBoolean(values.get("removeUnusedImports")));
                        }
                        yield KtfmtStep.create(values.getOrDefault("version", KtfmtStep.defaultVersion()),
                                provisioner,
                                values.containsKey("style") ? KtfmtStep.Style.valueOf(values.get("style")) : null,
                                settings);
                    }
                    case "ktlint" -> {
                        Map<String, Object> overrides = new TreeMap<>();
                        values.forEach((key, value) -> {
                            if (key.startsWith("editorconfig.")) {
                                overrides.put(key.substring("editorconfig.".length()), value);
                            }
                        });
                        yield KtLintStep.create(values.getOrDefault("version", KtLintStep.defaultVersion()),
                                provisioner,
                                null,
                                overrides,
                                List.of());
                    }
                    case "scalafmt" -> {
                        String version = values.getOrDefault("version", ScalaFmtStep.defaultVersion());
                        List<String> lines = new ArrayList<>(List.of("version = \"" + version + "\""));
                        values.entrySet().stream()
                                .filter(value -> value.getKey().startsWith("configuration."))
                                .map(value -> value.getKey().substring("configuration.".length()) + " = " + value.getValue())
                                .sorted()
                                .forEach(lines::add);
                        Path configuration = Files.createTempFile("scalafmt", ".conf");
                        configuration.toFile().deleteOnExit();
                        Files.write(configuration, lines);
                        yield ScalaFmtStep.create(version,
                                values.getOrDefault("scalaMajorVersion", ScalaFmtStep.defaultScalaMajorVersion()),
                                provisioner,
                                configuration.toFile());
                    }
                    case "licenseHeader" -> LicenseHeaderStep.headerDelimiter(values.get("header"),
                                    values.getOrDefault("delimiter", LicenseHeaderStep.DEFAULT_JAVA_HEADER_DELIMITER))
                            .build();
                    case "jacksonJson" -> JacksonJsonStep.create(new JacksonJsonConfig(),
                            values.getOrDefault("version", JacksonJsonStep.defaultVersion()),
                            provisioner);
                    case "jacksonYaml" -> JacksonYamlStep.create(new JacksonYamlConfig(),
                            values.getOrDefault("version", JacksonYamlStep.defaultVersion()),
                            provisioner);
                    case "flexmark" -> FlexmarkStep.create(values.getOrDefault("version", FlexmarkStep.defaultVersion()),
                            provisioner,
                            new FlexmarkConfig());
                    default -> throw new IllegalArgumentException("Unknown Spotless step " + name);
                });
            }
            formatters.put(entry.getKey(), Formatter.builder()
                    .lineEndingsPolicy(LineEnding.PRESERVE.createPolicy())
                    .encoding(StandardCharsets.UTF_8)
                    .steps(created)
                    .build());
        }
        int code = 0;
        try {
            for (String file : files) {
                Path path = Path.of(file);
                String raw = Files.readString(path), formatted = LineEnding.toUnix(raw);
                Formatter applied = null;
                for (Map.Entry<String, Formatter> entry : formatters.entrySet()) {
                    if (targets.get(entry.getKey()).stream().noneMatch(matcher -> IntStream.range(0, path.getNameCount())
                            .anyMatch(index -> matcher.matches(path.subpath(index, path.getNameCount()))))) {
                        continue;
                    }
                    try {
                        formatted = entry.getValue().compute(formatted, path.toFile());
                    } catch (RuntimeException exception) {
                        Throwable cause = exception;
                        while (cause.getCause() != null) {
                            cause = cause.getCause();
                        }
                        System.err.println(file + " cannot be formatted by the steps of " + entry.getKey() + ": " + cause);
                        return 2;
                    }
                    applied = entry.getValue();
                }
                if (applied == null) {
                    continue;
                }
                String result = applied.computeLineEndings(formatted, path.toFile());
                if (result.equals(raw)) {
                    continue;
                }
                if (check) {
                    System.err.println(file + " is not formatted as its Spotless steps format it");
                    code = 1;
                } else {
                    Files.writeString(path, result);
                }
            }
        } finally {
            formatters.values().forEach(Formatter::close);
        }
        return code;
    }
}
