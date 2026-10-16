package build.jenesis.plugin.revapi;

import module java.base;

public final class RevapiDriver {

    private RevapiDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Class<?> mapperType = Class.forName("com.fasterxml.jackson.databind.ObjectMapper"),
                jsonNode = Class.forName("com.fasterxml.jackson.databind.JsonNode"),
                arrayNode = Class.forName("com.fasterxml.jackson.databind.node.ArrayNode"),
                objectNode = Class.forName("com.fasterxml.jackson.databind.node.ObjectNode"),
                archive = Class.forName("org.revapi.Archive"),
                api = Class.forName("org.revapi.API"),
                apiBuilder = Class.forName("org.revapi.API$Builder"),
                revapiType = Class.forName("org.revapi.Revapi"),
                revapiBuilder = Class.forName("org.revapi.Revapi$Builder"),
                contextType = Class.forName("org.revapi.AnalysisContext"),
                contextBuilder = Class.forName("org.revapi.AnalysisContext$Builder"),
                resultType = Class.forName("org.revapi.AnalysisResult");
        Constructor<?> fileArchive = Class.forName("org.revapi.base.FileArchive").getConstructor(File.class);
        try {
            Object configuration = mapperType.getMethod("createArrayNode")
                    .invoke(mapperType.getConstructor().newInstance());
            Method put = objectNode.getMethod("put", String.class, String.class),
                    flag = objectNode.getMethod("put", String.class, boolean.class);
            Object reporter = arrayNode.getMethod("addObject").invoke(configuration);
            put.invoke(reporter, "extension", "revapi.reporter.json");
            Object options = objectNode.getMethod("putObject", String.class).invoke(reporter, "configuration");
            put.invoke(options, "minSeverity", arguments[1]);
            put.invoke(options, "output", arguments[0]);
            flag.invoke(options, "indent", true);
            flag.invoke(options, "keepEmptyFile", true);
            List<Object> support = new ArrayList<>();
            for (String name : List.of(arguments).subList(5, arguments.length)) {
                support.add(fileArchive.newInstance(new File(name)));
            }
            Method of = api.getMethod("of", archive.arrayType()),
                    supportedBy = apiBuilder.getMethod("supportedBy", Iterable.class),
                    build = apiBuilder.getMethod("build");
            Object[] apis = new Object[2];
            for (int index = 0; index < apis.length; index++) {
                Object archives = Array.newInstance(archive, 1);
                Array.set(archives, 0, fileArchive.newInstance(new File(arguments[3 + index])));
                apis[index] = build.invoke(supportedBy.invoke(of.invoke(null, archives), support));
            }
            Object revapi = revapiBuilder.getMethod("build").invoke(revapiBuilder
                    .getMethod("withAllExtensionsFromThreadContextClassLoader")
                    .invoke(revapiType.getMethod("builder").invoke(null)));
            Object context = contextType.getMethod("builder", revapiType).invoke(null, revapi);
            context = contextBuilder.getMethod("withOldAPI", api).invoke(context, apis[0]);
            context = contextBuilder.getMethod("withNewAPI", api).invoke(context, apis[1]);
            context = contextBuilder.getMethod("withConfiguration", jsonNode).invoke(context, configuration);
            if (!arguments[2].equals("-")) {
                try (InputStream in = Files.newInputStream(Path.of(arguments[2]))) {
                    context = contextBuilder.getMethod("mergeConfigurationFromJSONStream", InputStream.class)
                            .invoke(context, in);
                }
            }
            try (AutoCloseable result = (AutoCloseable) revapiType.getMethod("analyze", contextType)
                    .invoke(revapi, contextBuilder.getMethod("build").invoke(context))) {
                resultType.getMethod("throwIfFailed").invoke(result);
            }
            return 0;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
