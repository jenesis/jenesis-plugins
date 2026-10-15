package build.jenesis.plugin.ktfmt;

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
import build.jenesis.step.FormatBuildStep;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class KtfmtFormatModule implements BuildExecutorModule {

    public static final String FORMAT = "format";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String MAVEN_GROUP = "com.facebook", MAVEN_ARTIFACT = "ktfmt";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String group;
    private final boolean verify;
    private final Style style;
    private final boolean removeUnusedImports;
    private final ProcessBuildStep.Terms terms;

    public KtfmtFormatModule(Map<String, Repository> repositories,
                             Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "ktfmt-format",
             true,
             Style.META,
             true,
             new ProcessBuildStep.Terms());
    }

    public static KtfmtFormatModule ofEnvironment(Environment environment,
                                                  Map<String, Repository> repositories,
                                                  Map<String, Resolver> resolvers) {
        return new KtfmtFormatModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "ktfmt-format",
                !environment.flag("format.rewrite", false),
                Style.META,
                true,
                ProcessBuildStep.Terms.ofEnvironment(environment, "ktfmt-format"));
    }

    private KtfmtFormatModule(Dependencies dependencies,
                              Pinning pinning,
                              String group,
                              boolean verify,
                              Style style,
                              boolean removeUnusedImports,
                              ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.group = group;
        this.verify = verify;
        this.style = style;
        this.removeUnusedImports = removeUnusedImports;
        this.terms = terms;
    }

    public enum Style {

        META("--meta-style"),
        GOOGLE("--google-style"),
        KOTLINLANG("--kotlinlang-style");

        private final String option;

        Style(String option) {
            this.option = option;
        }
    }

    public KtfmtFormatModule pinning(Pinning pinning) {
        return new KtfmtFormatModule(dependencies, pinning, group, verify, style, removeUnusedImports, terms);
    }

    public KtfmtFormatModule group(String group) {
        return new KtfmtFormatModule(dependencies, pinning, group, verify, style, removeUnusedImports, terms);
    }

    public KtfmtFormatModule verify(boolean verify) {
        return new KtfmtFormatModule(dependencies, pinning, group, verify, style, removeUnusedImports, terms);
    }

    public KtfmtFormatModule style(Style style) {
        return new KtfmtFormatModule(dependencies, pinning, group, verify, style, removeUnusedImports, terms);
    }

    public KtfmtFormatModule removeUnusedImports(boolean removeUnusedImports) {
        return new KtfmtFormatModule(dependencies, pinning, group, verify, style, removeUnusedImports, terms);
    }

    public KtfmtFormatModule printing(BiConsumer<Boolean, String> printing) {
        return new KtfmtFormatModule(dependencies,
                pinning,
                group,
                verify,
                style,
                removeUnusedImports,
                terms.printing(printing));
    }

    public KtfmtFormatModule permits(Permits permits) {
        return new KtfmtFormatModule(dependencies,
                pinning,
                group,
                verify,
                style,
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
        buildExecutor.addStep(FORMAT, new Format(terms, group, verify, style, removeUnusedImports), formatInputs);
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

        private final Style style;
        private final boolean removeUnusedImports;

        private Format(ProcessBuildStep.Terms terms,
                       String group,
                       boolean verify,
                       Style style,
                       boolean removeUnusedImports) {
            super("ktfmt-format", group, verify, terms);
            this.style = style;
            this.removeUnusedImports = removeUnusedImports;
        }

        @Override
        protected boolean isFormattable(Path file) {
            return file.toString().endsWith(".kt");
        }

        @Override
        protected List<String> command(BuildStepContext context,
                                       List<String> jars,
                                       Path config,
                                       List<String> files,
                                       boolean verify) throws IOException {
            List<String> commands = new ArrayList<>(List.of(
                    "-cp", ProcessHandler.OfProcess.driver(KtfmtDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    KtfmtDriver.class.getName(),
                    style.option));
            if (!removeUnusedImports) {
                commands.add("--do-not-remove-unused-imports");
            }
            if (verify) {
                commands.add("--dry-run");
                commands.add("--set-exit-if-changed");
            }
            commands.addAll(files);
            return commands;
        }
    }
}
