package build.jenesis.plugin.scalafmt;

import module java.base;

public final class ScalafmtDriver {

    private ScalafmtDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Class<?> cli = Class.forName("org.scalafmt.cli.Cli$"), options = Class.forName("org.scalafmt.cli.CliOptions$");
        Object instance = cli.getField("MODULE$").get(null),
                defaults = options.getMethod("default").invoke(options.getField("MODULE$").get(null));
        try {
            Object code;
            try {
                code = cli.getMethod("mainWithOptions", String[].class, Class.forName("org.scalafmt.cli.CliOptions"))
                        .invoke(instance, arguments, defaults);
            } catch (NoSuchMethodException _) {
                Class<?> predef = Class.forName("scala.Predef$"),
                        await = Class.forName("scala.concurrent.Await$"),
                        duration = Class.forName("scala.concurrent.duration.Duration$");
                Object result = cli.getMethod("mainWithOptions",
                        Class.forName("org.scalafmt.cli.CliOptions"),
                        Class.forName("scala.collection.immutable.Seq")).invoke(instance,
                        defaults,
                        predef.getMethod("copyArrayToImmutableIndexedSeq", Object.class)
                                .invoke(predef.getField("MODULE$").get(null), (Object) arguments));
                code = await.getMethod("result",
                        Class.forName("scala.concurrent.Awaitable"),
                        Class.forName("scala.concurrent.duration.Duration")).invoke(await.getField("MODULE$").get(null),
                        result,
                        duration.getMethod("Inf").invoke(duration.getField("MODULE$").get(null)));
            }
            return (int) code.getClass().getMethod("code").invoke(code);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
