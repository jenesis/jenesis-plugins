package build.jenesis.plugin.openrewrite;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.step.Dependencies;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class OpenRewriteModule implements BuildExecutorModule {

    public static final String REWRITE = "rewrite", CONFIGURATION = "rewrite.yml";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String MAVEN_GROUP = "org.openrewrite", MAVEN_ARTIFACT = "rewrite-java-25";
    private static final String LOGGING_GROUP = "org.slf4j", LOGGING_ARTIFACT = "slf4j-nop";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final List<String> recipes;
    private final boolean verify;
    private final ProcessBuildStep.Terms terms;

    public OpenRewriteModule(Map<String, Repository> repositories,
                             Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "openrewrite",
             "main",
             List.of(),
             true,
             new ProcessBuildStep.Terms());
    }

    public static OpenRewriteModule ofEnvironment(Environment environment,
                                                  Map<String, Repository> repositories,
                                                  Map<String, Resolver> resolvers) {
        return new OpenRewriteModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "openrewrite",
                "main",
                List.of(),
                !environment.flag("format.rewrite", false),
                ProcessBuildStep.Terms.ofEnvironment(environment, "openrewrite"));
    }

    private OpenRewriteModule(Dependencies dependencies,
                              Pinning pinning,
                              String tool,
                              String group,
                              List<String> recipes,
                              boolean verify,
                              ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.group = group;
        this.recipes = recipes;
        this.verify = verify;
        this.terms = terms;
    }

    public OpenRewriteModule pinning(Pinning pinning) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms);
    }

    public OpenRewriteModule tool(String tool) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms);
    }

    public OpenRewriteModule group(String group) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms);
    }

    public OpenRewriteModule recipes(List<String> recipes) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, List.copyOf(recipes), verify, terms);
    }

    public OpenRewriteModule verify(boolean verify) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms);
    }

    public OpenRewriteModule printing(BiConsumer<Boolean, String> printing) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms.printing(printing));
    }

    public OpenRewriteModule permits(Permits permits) {
        return new OpenRewriteModule(dependencies, pinning, tool, group, recipes, verify, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        if (recipes.isEmpty()) {
            throw new IllegalStateException("The OpenRewrite module names no recipe to run - name the recipes, such as"
                    + " org.openrewrite.java.RemoveUnusedImports");
        }
        buildExecutor.addStep(REQUIRED, new Requires(tool), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(tool),
                resolveInputs);
        SequencedSet<String> rewriteInputs = new LinkedHashSet<>();
        rewriteInputs.add(DEPENDENCIES);
        rewriteInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(REWRITE, new Rewrite(terms, tool, group, recipes, verify), rewriteInputs);
    }

    private record Requires(String tool) implements BuildStep {

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
            return false;
        }

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            SequencedProperties requires = new SequencedProperties();
            requires.setProperty(tool + "/runtime/maven/" + MAVEN_GROUP + "/" + MAVEN_ARTIFACT + "/RELEASE", "");
            requires.setProperty(tool + "/runtime/maven/" + LOGGING_GROUP + "/" + LOGGING_ARTIFACT + "/RELEASE", "");
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Rewrite extends ProcessBuildStep {

        private final String tool;
        private final String group;
        private final List<String> recipes;
        private final boolean verify;

        private Rewrite(ProcessBuildStep.Terms terms, String tool, String group, List<String> recipes, boolean verify) {
            super("openrewrite", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.group = group;
            this.recipes = recipes;
            this.verify = verify;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
            return !verify || arguments.values().stream().anyMatch(BuildStepArgument::hasChanged);
        }

        @Override
        protected CompletionStage<List<String>> process(Executor executor,
                                                        BuildStepContext context,
                                                        SequencedMap<String, BuildStepArgument> arguments,
                                                        SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            List<String> jars = new ArrayList<>(), classpath = new ArrayList<>(), roots = new ArrayList<>();
            Path config = null;
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                for (String scope : List.of("runtime", "plugin")) {
                    for (Path jar : Dependencies.select(argument.folder(), tool, scope)) {
                        jars.add(jar.toString());
                    }
                }
                for (Path jar : Dependencies.select(argument.folder(), group, "compile")) {
                    classpath.add(jar.toString());
                }
                Path candidate = argument.folder().resolve(CONFIGURATION);
                if (Files.isRegularFile(candidate)) {
                    config = candidate;
                }
                Path sources = argument.folder().resolve(BuildStep.SOURCES);
                if (Files.isDirectory(sources)) {
                    roots.add(sources.toString());
                }
            }
            if (roots.isEmpty()) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No OpenRewrite jars resolved upstream of the OpenRewrite step");
            }
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(OpenRewriteDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    OpenRewriteDriver.class.getName(),
                    "--recipes",
                    String.join(",", recipes)));
            if (config != null) {
                commands.add("--config");
                commands.add(config.toString());
            }
            if (!classpath.isEmpty()) {
                commands.add("--classpath");
                commands.add(String.join(File.pathSeparator, classpath));
            }
            commands.add(verify ? "--verify" : "--apply");
            commands.addAll(roots);
            return CompletableFuture.completedStage(commands);
        }
    }
}
