package build.jenesis.plugin.spotless;

import module java.base;

public sealed interface SpotlessStep extends Serializable {

    String name();

    default SequencedMap<String, String> options() {
        return new LinkedHashMap<>();
    }

    default String artifact() {
        return null;
    }

    default String version() {
        return null;
    }

    default boolean javac() {
        return false;
    }

    private static SequencedMap<String, String> options(Object... pairs) {
        SequencedMap<String, String> options = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            if (pairs[index + 1] != null) {
                options.put((String) pairs[index], pairs[index + 1].toString());
            }
        }
        return options;
    }

    record TrimTrailingWhitespace() implements SpotlessStep {

        @Override
        public String name() {
            return "trimTrailingWhitespace";
        }
    }

    record EndWithNewline() implements SpotlessStep {

        @Override
        public String name() {
            return "endWithNewline";
        }
    }

    record LeadingTabsToSpaces(int size) implements SpotlessStep {

        @Override
        public String name() {
            return "leadingTabsToSpaces";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("size", size);
        }
    }

    record LeadingSpacesToTabs(int size) implements SpotlessStep {

        @Override
        public String name() {
            return "leadingSpacesToTabs";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("size", size);
        }
    }

    record ReplaceRegex(String regex, String replacement) implements SpotlessStep {

        @Override
        public String name() {
            return "replaceRegex";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("regex", regex, "replacement", replacement);
        }
    }

    record ImportOrder(List<String> order) implements SpotlessStep {

        @Override
        public String name() {
            return "importOrder";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("order", order.isEmpty() ? null : String.join(",", order));
        }
    }

    record FormatAnnotations() implements SpotlessStep {

        @Override
        public String name() {
            return "formatAnnotations";
        }
    }

    record GoogleJavaFormat(String version,
                            String style,
                            Boolean reflowLongStrings,
                            Boolean reorderImports,
                            Boolean formatJavadoc) implements SpotlessStep {

        @Override
        public String name() {
            return "googleJavaFormat";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version,
                    "style", style,
                    "reflowLongStrings", reflowLongStrings,
                    "reorderImports", reorderImports,
                    "formatJavadoc", formatJavadoc);
        }

        @Override
        public String artifact() {
            return "com.google.googlejavaformat/google-java-format";
        }

        @Override
        public boolean javac() {
            return true;
        }
    }

    record PalantirJavaFormat(String version, String style, Boolean formatJavadoc) implements SpotlessStep {

        @Override
        public String name() {
            return "palantirJavaFormat";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version, "style", style, "formatJavadoc", formatJavadoc);
        }

        @Override
        public String artifact() {
            return "com.palantir.javaformat/palantir-java-format";
        }

        @Override
        public boolean javac() {
            return true;
        }
    }

    record RemoveUnusedImports(String version) implements SpotlessStep {

        @Override
        public String name() {
            return "removeUnusedImports";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version);
        }

        @Override
        public String artifact() {
            return "com.google.googlejavaformat/google-java-format";
        }

        @Override
        public boolean javac() {
            return true;
        }
    }

    record PrinceOfSpace(String version,
                         String wrapStyle,
                         String indentStyle,
                         Integer indentSize,
                         Integer lineLength,
                         Boolean closingParenOnNewLine,
                         Boolean trailingCommas,
                         Integer javaLanguageLevel) implements SpotlessStep {

        @Override
        public String name() {
            return "princeOfSpace";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version,
                    "wrapStyle", wrapStyle,
                    "indentStyle", indentStyle,
                    "indentSize", indentSize,
                    "lineLength", lineLength,
                    "closingParenOnNewLine", closingParenOnNewLine,
                    "trailingCommas", trailingCommas,
                    "javaLanguageLevel", javaLanguageLevel);
        }

        @Override
        public String artifact() {
            return "io.github.agustafson.princeofspace/prince-of-space-core";
        }
    }

    record Ktfmt(String version,
                 String style,
                 Integer maxWidth,
                 Integer blockIndent,
                 Integer continuationIndent,
                 Boolean removeUnusedImports) implements SpotlessStep {

        @Override
        public String name() {
            return "ktfmt";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version,
                    "style", style,
                    "maxWidth", maxWidth,
                    "blockIndent", blockIndent,
                    "continuationIndent", continuationIndent,
                    "removeUnusedImports", removeUnusedImports);
        }

        @Override
        public String artifact() {
            return "org.jetbrains.kotlinx/ktfmt";
        }
    }

    record Ktlint(String version, TreeMap<String, String> editorConfig) implements SpotlessStep {

        @Override
        public String name() {
            return "ktlint";
        }

        @Override
        public SequencedMap<String, String> options() {
            SequencedMap<String, String> options = SpotlessStep.options("version", version);
            editorConfig.forEach((key, value) -> options.put("editorconfig." + key, value));
            return options;
        }

        @Override
        public String artifact() {
            return "com.pinterest.ktlint/ktlint-cli";
        }
    }

    record Scalafmt(String version, String scalaMajorVersion, TreeMap<String, String> configuration)
            implements SpotlessStep {

        @Override
        public String name() {
            return "scalafmt";
        }

        @Override
        public SequencedMap<String, String> options() {
            SequencedMap<String, String> options = SpotlessStep.options("version", version,
                    "scalaMajorVersion", scalaMajorVersion);
            configuration.forEach((key, value) -> options.put("configuration." + key, value));
            return options;
        }

        @Override
        public String artifact() {
            return "org.scalameta/scalafmt-core_" + (scalaMajorVersion == null ? "2.13" : scalaMajorVersion);
        }
    }

    record LicenseHeader(String header, String delimiter) implements SpotlessStep {

        @Override
        public String name() {
            return "licenseHeader";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("header", header, "delimiter", delimiter);
        }
    }

    record JacksonJson(String version) implements SpotlessStep {

        @Override
        public String name() {
            return "jacksonJson";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version);
        }

        @Override
        public String artifact() {
            return "com.fasterxml.jackson.core/jackson-databind";
        }
    }

    record JacksonYaml(String version) implements SpotlessStep {

        @Override
        public String name() {
            return "jacksonYaml";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version);
        }

        @Override
        public String artifact() {
            return "com.fasterxml.jackson.dataformat/jackson-dataformat-yaml";
        }
    }

    record Flexmark(String version) implements SpotlessStep {

        @Override
        public String name() {
            return "flexmark";
        }

        @Override
        public SequencedMap<String, String> options() {
            return SpotlessStep.options("version", version);
        }

        @Override
        public String artifact() {
            return "com.vladsch.flexmark/flexmark-all";
        }
    }
}
