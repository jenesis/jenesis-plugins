package build.jenesis.plugin.checkstyle;

import module java.base;

public final class CheckstyleDriver {

    private CheckstyleDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Class<?> main = Class.forName("com.puppycrawl.tools.checkstyle.Main"),
                options = Class.forName("com.puppycrawl.tools.checkstyle.Main$CliOptions"),
                commandLine = Class.forName("picocli.CommandLine"),
                parseResult = Class.forName("picocli.CommandLine$ParseResult");
        Constructor<?> constructor = options.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object parsed = constructor.newInstance(), command = commandLine.getConstructor(Object.class).newInstance(parsed);
        commandLine.getMethod("setUsageHelpWidth", int.class).invoke(command, 100);
        commandLine.getMethod("setCaseInsensitiveEnumValuesAllowed", boolean.class).invoke(command, true);
        Method execute = main.getDeclaredMethod("execute", parseResult, options);
        execute.setAccessible(true);
        try {
            return (int) execute.invoke(null,
                    commandLine.getMethod("parseArgs", String[].class).invoke(command, (Object) arguments),
                    parsed);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
