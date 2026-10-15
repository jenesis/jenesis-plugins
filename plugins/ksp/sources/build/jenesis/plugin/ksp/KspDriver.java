package build.jenesis.plugin.ksp;

import module java.base;

public final class KspMain {

    private static final String KSP = "com.google.devtools.ksp.";

    private KspMain() {
    }

    public static void main(String[] args) throws Exception {
        ClassLoader loader = KspMain.class.getClassLoader();
        Object parsed = Class.forName(KSP + "processing.KspJvmArgParserKt", true, loader)
                .getMethod("kspJvmArgParser", String[].class)
                .invoke(null, (Object) args);
        Object config = parsed.getClass().getMethod("getFirst").invoke(parsed);
        List<?> processorPath = (List<?>) parsed.getClass().getMethod("getSecond").invoke(parsed);
        URL[] urls = new URL[processorPath.size()];
        for (int index = 0; index < urls.length; index++) {
            urls[index] = Path.of(processorPath.get(index).toString()).toUri().toURL();
        }
        Class<?> logger = Class.forName(KSP + "processing.KspGradleLogger", true, loader);
        int code;
        try (URLClassLoader processors = new URLClassLoader(urls, loader)) {
            List<Object> providers = new ArrayList<>();
            ServiceLoader.load(Class.forName(KSP + "processing.SymbolProcessorProvider", false, loader), processors)
                    .forEach(providers::add);
            Object processing = Class.forName(KSP + "impl.KotlinSymbolProcessing", true, loader)
                    .getConstructor(Class.forName(KSP + "processing.KSPConfig", false, loader),
                            List.class,
                            Class.forName(KSP + "processing.KSPLogger", false, loader))
                    .newInstance(config,
                            providers,
                            logger.getConstructor(int.class).newInstance(logger.getField("LOGGING_LEVEL_WARN").getInt(null)));
            Object executed = processing.getClass().getMethod("execute").invoke(processing);
            code = (int) executed.getClass().getMethod("getCode").invoke(executed);
        }
        if (code != 0) {
            throw new IllegalStateException("KSP failed with exit code " + code + ", as reported above");
        }
    }
}
