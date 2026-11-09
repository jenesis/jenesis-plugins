package build.jenesis.plugin.openjavaformat;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;
import build.jenesis.Environment;
import build.jenesis.Environment.Setting;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.step.Dependencies;
import build.jenesis.step.FormatBuildStep;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class OpenJavaFormatModule implements BuildExecutorModule {

    public static final String FORMAT = "format";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String MAVEN_GROUP = "dev.openjavaformat", MAVEN_ARTIFACT = "open-java-format";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String group;
    private final boolean verify;
    private final boolean reflow;
    private final boolean sortImports;
    private final boolean removeUnusedImports;
    private final ProcessBuildStep.Terms terms;

    public OpenJavaFormatModule(Map<String, Repository> repositories,
                                    Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "open-java-format",
             true,
             true,
             true,
             true,
             new ProcessBuildStep.Terms());
    }

    public static OpenJavaFormatModule ofEnvironment(Environment environment,
                                                         Map<String, Repository> repositories,
                                                         Map<String, Resolver> resolvers) {
        return new OpenJavaFormatModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "open-java-format",
                !environment.flag("format.rewrite", false, Setting.PROJECT),
                true,
                true,
                true,
                ProcessBuildStep.Terms.ofEnvironment(environment, "open-java-format"));
    }

    private OpenJavaFormatModule(Dependencies dependencies,
                                     Pinning pinning,
                                     String group,
                                     boolean verify,
                                     boolean reflow,
                                     boolean sortImports,
                                     boolean removeUnusedImports,
                                     ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.group = group;
        this.verify = verify;
        this.reflow = reflow;
        this.sortImports = sortImports;
        this.removeUnusedImports = removeUnusedImports;
        this.terms = terms;
    }

    public OpenJavaFormatModule pinning(Pinning pinning) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule group(String group) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule verify(boolean verify) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule reflow(boolean reflow) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule sortImports(boolean sortImports) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule removeUnusedImports(boolean removeUnusedImports) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms);
    }

    public OpenJavaFormatModule printing(BiConsumer<Boolean, String> printing) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms.printing(printing));
    }

    public OpenJavaFormatModule permits(Permits permits) {
        return new OpenJavaFormatModule(dependencies,
                pinning,
                group,
                verify,
                reflow,
                sortImports,
                removeUnusedImports,
                terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(REQUIRED, new Requires(group), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(group),
                resolveInputs);
        SequencedSet<String> formatInputs = new LinkedHashSet<>();
        formatInputs.add(DEPENDENCIES);
        formatInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(FORMAT,
                new Format(terms, group, verify, reflow, sortImports, removeUnusedImports),
                formatInputs);
    }

    private record Requires(String group) implements BuildStep {

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
            requires.setProperty(group + "/runtime/maven/" + MAVEN_GROUP + "/" + MAVEN_ARTIFACT + "/RELEASE", "");
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Format extends FormatBuildStep {

        private final boolean reflow, sortImports, removeUnusedImports;

        private Format(ProcessBuildStep.Terms terms,
                       String group,
                       boolean verify,
                       boolean reflow,
                       boolean sortImports,
                       boolean removeUnusedImports) {
            super("open-java-format", group, verify, terms);
            this.reflow = reflow;
            this.sortImports = sortImports;
            this.removeUnusedImports = removeUnusedImports;
        }

        @Override
        protected boolean isFormattable(Path file) {
            return file.toString().endsWith(".java")
                    && !file.getFileName().toString().equals("module-info.java");
        }

        @Override
        protected List<String> command(BuildStepContext context,
                                       List<String> jars,
                                       Path config,
                                       List<String> files,
                                       boolean verify) throws IOException {
            List<String> commands = new ArrayList<>(List.of(
                    "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                    "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                    "-cp",
                    ProcessHandler.OfProcess.driver(OpenJavaFormatDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    OpenJavaFormatDriver.class.getName()));
            if (!reflow) {
                commands.add("--skip-reflowing-long-strings");
            }
            if (!sortImports) {
                commands.add("--skip-sorting-imports");
            }
            if (!removeUnusedImports) {
                commands.add("--skip-removing-unused-imports");
            }
            if (verify) {
                commands.add("--dry-run");
                commands.add("--set-exit-if-changed");
            } else {
                commands.add("--replace");
            }
            commands.addAll(files);
            return commands;
        }
    }
}
