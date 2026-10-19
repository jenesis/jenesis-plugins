package build.jenesis.plugin.openrewrite;

import module java.base;

public final class OpenRewriteDriver {

    private OpenRewriteDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        List<String> recipes = new ArrayList<>();
        List<Path> classpath = new ArrayList<>(), roots = new ArrayList<>();
        Path config = null;
        boolean verify = true;
        for (int index = 0; index < arguments.length; index++) {
            switch (arguments[index]) {
                case "--recipes" -> recipes.addAll(List.of(arguments[++index].split(",")));
                case "--config" -> config = Path.of(arguments[++index]);
                case "--classpath" -> {
                    for (String element : arguments[++index].split(File.pathSeparator)) {
                        classpath.add(Path.of(element));
                    }
                }
                case "--verify" -> verify = true;
                case "--apply" -> verify = false;
                default -> roots.add(Path.of(arguments[index]));
            }
        }
        List<Path> files = new ArrayList<>();
        for (Path root : roots) {
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(file -> file.toString().endsWith(".java")
                                && !file.getFileName().toString().equals("module-info.java")
                                && Files.isRegularFile(file))
                        .sorted()
                        .forEach(files::add);
            }
        }
        if (files.isEmpty()) {
            return 0;
        }
        ClassLoader loader = OpenRewriteDriver.class.getClassLoader();
        Class<?> environmentType = Class.forName("org.openrewrite.config.Environment", true, loader),
                builderType = Class.forName("org.openrewrite.config.Environment$Builder", true, loader),
                resourceLoaderType = Class.forName("org.openrewrite.config.ResourceLoader", true, loader),
                contextType = Class.forName("org.openrewrite.ExecutionContext", true, loader),
                parserBuilderType = Class.forName("org.openrewrite.java.JavaParser$Builder", true, loader),
                sourceSetType = Class.forName("org.openrewrite.LargeSourceSet", true, loader),
                recipeType = Class.forName("org.openrewrite.Recipe", true, loader),
                runType = Class.forName("org.openrewrite.RecipeRun", true, loader),
                changesetType = Class.forName("org.openrewrite.Changeset", true, loader),
                resultType = Class.forName("org.openrewrite.Result", true, loader),
                sourceFileType = Class.forName("org.openrewrite.SourceFile", true, loader);
        Object builder = environmentType.getMethod("builder").invoke(null);
        builder = builderType.getMethod("scanRuntimeClasspath", String[].class).invoke(builder, (Object) new String[0]);
        if (config != null) {
            try (InputStream input = Files.newInputStream(config)) {
                Object resources = Class.forName("org.openrewrite.config.YamlResourceLoader", true, loader)
                        .getConstructor(InputStream.class, URI.class, Properties.class)
                        .newInstance(input, config.toUri(), new Properties());
                builder = builderType.getMethod("load", resourceLoaderType).invoke(builder, resources);
                if (recipes.isEmpty()) {
                    for (Object declared : (Collection<?>) resourceLoaderType.getMethod("listRecipes").invoke(resources)) {
                        recipes.add((String) recipeType.getMethod("getName").invoke(declared));
                    }
                }
            }
        }
        if (recipes.isEmpty()) {
            System.err.println("No recipe is named, and " + (config == null ? "no rewrite.yml is handed"
                    : config.getFileName() + " declares none") + " - declare one there, or name it as recipes=<recipe>");
            return 1;
        }
        Object environment = builderType.getMethod("build").invoke(builder);
        Object recipe = environmentType.getMethod("activateRecipes", Collection.class).invoke(environment, recipes);
        List<Throwable> errors = new ArrayList<>();
        Consumer<Throwable> onError = errors::add;
        Object context = Class.forName("org.openrewrite.InMemoryExecutionContext", true, loader)
                .getConstructor(Consumer.class)
                .newInstance(onError);
        Object parserBuilder = Class.forName("org.openrewrite.java.JavaParser", true, loader)
                .getMethod("fromJavaVersion")
                .invoke(null);
        parserBuilder = parserBuilderType.getMethod("classpath", Collection.class).invoke(parserBuilder, classpath);
        parserBuilder = parserBuilderType.getMethod("logCompilationWarningsAndErrors", boolean.class)
                .invoke(parserBuilder, false);
        Object parser = parserBuilderType.getMethod("build").invoke(parserBuilder);
        Path relativeTo = roots.size() == 1 ? roots.getFirst() : null;
        List<?> sources = ((Stream<?>) Class.forName("org.openrewrite.Parser", true, loader)
                .getMethod("parse", Iterable.class, Path.class, contextType)
                .invoke(parser, files, relativeTo, context)).toList();
        Object sourceSet = Class.forName("org.openrewrite.internal.InMemoryLargeSourceSet", true, loader)
                .getConstructor(List.class)
                .newInstance(sources);
        Object run = recipeType.getMethod("run", sourceSetType, contextType).invoke(recipe, sourceSet, context);
        Object changeset = runType.getMethod("getChangeset").invoke(run);
        List<?> results = (List<?>) changesetType.getMethod("getAllResults").invoke(changeset);
        for (Throwable error : errors) {
            System.err.println("OpenRewrite: " + error);
        }
        if (results.isEmpty()) {
            return 0;
        }
        if (verify) {
            for (Object result : results) {
                System.out.println(resultType.getMethod("diff").invoke(result));
            }
            System.out.println("The recipes " + String.join(", ", recipes) + " change " + results.size() + " file"
                    + (results.size() == 1 ? "" : "s") + " - run with -Djenesis.format.rewrite=true to apply them");
            return 1;
        }
        for (Object result : results) {
            Object before = resultType.getMethod("getBefore").invoke(result),
                    after = resultType.getMethod("getAfter").invoke(result);
            if (before == null || after == null) {
                System.err.println("OpenRewrite " + (before == null ? "adds" : "deletes") + " a file, which the build"
                        + " does not apply - the recipes " + String.join(", ", recipes) + " only change files here");
                return 1;
            }
            Path origin = (Path) sourceFileType.getMethod("getSourcePath").invoke(before),
                    target = (Path) sourceFileType.getMethod("getSourcePath").invoke(after);
            if (!origin.equals(target)) {
                System.err.println("OpenRewrite moves " + origin + " to " + target + ", which the build does not apply"
                        + " - the recipes " + String.join(", ", recipes) + " only change files here");
                return 1;
            }
            Path file = relativeTo == null ? origin : relativeTo.resolve(origin);
            Files.writeString(file, (String) sourceFileType.getMethod("printAll").invoke(after));
        }
        return 0;
    }
}
