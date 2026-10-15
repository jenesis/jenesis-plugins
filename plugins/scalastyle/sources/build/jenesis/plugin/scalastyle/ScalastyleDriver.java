package build.jenesis.plugin.scalastyle;

import module java.base;

public final class ScalastyleDriver {

    private ScalastyleDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Class<?> main = Class.forName("org.scalastyle.Main$"), codec = Class.forName("scala.io.Codec$");
        Object instance = main.getField("MODULE$").get(null),
                config = main.getMethod("parseArgs", String[].class).invoke(instance, (Object) arguments);
        if ((boolean) config.getClass().getMethod("error").invoke(config)) {
            throw new IllegalArgumentException("Scalastyle refuses its command line " + List.of(arguments));
        }
        Method execute = main.getDeclaredMethod("execute", config.getClass(), Class.forName("scala.io.Codec"));
        execute.setAccessible(true);
        try {
            return (boolean) execute.invoke(instance,
                    config,
                    codec.getMethod("fallbackSystemCodec").invoke(codec.getField("MODULE$").get(null))) ? 1 : 0;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
