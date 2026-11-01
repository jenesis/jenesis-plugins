package build.jenesis.plugin.princeofspace;

import module java.base;
import io.princeofspace.Formatter;
import io.princeofspace.model.FormatResult;
import io.princeofspace.model.FormatterConfig;
import io.princeofspace.model.IndentStyle;
import io.princeofspace.model.JavaLanguageLevel;
import io.princeofspace.model.WrapStyle;

public final class PrinceOfSpaceDriver {

    private PrinceOfSpaceDriver() {
    }

    public static int main(String[] arguments) throws IOException {
        FormatterConfig.Builder config = FormatterConfig.builder();
        int index = 0;
        while (index < arguments.length && arguments[index].startsWith("--")) {
            String argument = arguments[index++];
            int equals = argument.indexOf('=');
            String value = equals < 0 ? "" : argument.substring(equals + 1);
            switch (equals < 0 ? argument : argument.substring(0, equals)) {
                case "--check" -> {
                }
                case "--wrap-style" -> config.wrapStyle(WrapStyle.valueOf(value.toUpperCase(Locale.ROOT)));
                case "--indent-style" -> config.indentStyle(IndentStyle.valueOf(value.toUpperCase(Locale.ROOT)));
                case "--indent-size" -> config.indentSize(Integer.parseInt(value));
                case "--line-length" -> config.lineLength(Integer.parseInt(value));
                case "--closing-paren-on-new-line" -> config.closingParenOnNewLine(Boolean.parseBoolean(value));
                case "--trailing-commas" -> config.trailingCommas(Boolean.parseBoolean(value));
                case "--java-version" -> config.javaLanguageLevel(JavaLanguageLevel.of(Integer.parseInt(value)));
                default -> throw new IllegalArgumentException("Unknown argument " + argument);
            }
        }
        boolean check = Arrays.asList(arguments).subList(0, index).contains("--check");
        Formatter formatter = new Formatter(config.build());
        int code = 0;
        for (String name : Arrays.asList(arguments).subList(index, arguments.length)) {
            Path file = Path.of(name);
            String source = Files.readString(file, StandardCharsets.UTF_8);
            switch (formatter.formatResult(source, file)) {
                case FormatResult.Success(String formatted) when formatted.equals(source) -> {
                }
                case FormatResult.Success _ when check -> {
                    System.err.println(file + " is not formatted as Prince of Space formats it");
                    code = Math.max(code, 1);
                }
                case FormatResult.Success(String formatted) -> Files.writeString(file, formatted, StandardCharsets.UTF_8);
                case FormatResult.Failure failure -> {
                    System.err.println(failure.message());
                    code = 2;
                }
            }
        }
        return code;
    }
}
