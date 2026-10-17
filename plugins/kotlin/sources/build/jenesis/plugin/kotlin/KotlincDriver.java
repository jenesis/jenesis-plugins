package build.jenesis.plugin.kotlin;

import module java.base;

public final class KotlincDriver {

    private KotlincDriver() {
    }

    public static int main(String[] arguments) throws ReflectiveOperationException {
        Class<?> type = Class.forName("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler");
        Object code = type.getMethod("exec", PrintStream.class, String[].class)
                .invoke(type.getConstructor().newInstance(), System.err, arguments);
        return (Integer) code.getClass().getMethod("getCode").invoke(code);
    }
}
