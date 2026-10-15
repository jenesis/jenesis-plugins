package build.jenesis.plugin.pmd;

import module java.base;

public final class PmdDriver {

    private PmdDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        System.setProperty("picocli.disable.closures", "true");
        Class<?> commandLine = Class.forName("picocli.CommandLine");
        Object command = commandLine.getConstructor(Object.class).newInstance(
                Class.forName("net.sourceforge.pmd.cli.commands.internal.PmdRootCommand").getConstructor().newInstance());
        commandLine.getMethod("setCaseInsensitiveEnumValuesAllowed", boolean.class).invoke(command, true);
        try {
            return (int) commandLine.getMethod("execute", String[].class).invoke(command, (Object) arguments);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
