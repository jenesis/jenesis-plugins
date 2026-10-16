package build.jenesis.plugin.eclipse;

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
import build.jenesis.step.Bind;
import build.jenesis.step.Dependencies;
import build.jenesis.step.FormatBuildStep;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class EclipseJavaFormatModule implements BuildExecutorModule {

    public static final String FORMAT = "format", PROFILE = "eclipse-formatter.xml";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies", CONFIGURATION = "profile";
    private static final String MAVEN_GROUP = "org.eclipse.jdt", MAVEN_ARTIFACT = "org.eclipse.jdt.core";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String group;
    private final boolean verify;
    private final Path profile;
    private final ProcessBuildStep.Terms terms;

    public EclipseJavaFormatModule(Map<String, Repository> repositories,
                                   Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "eclipse-java-format",
             true,
             null,
             new ProcessBuildStep.Terms());
    }

    public static EclipseJavaFormatModule ofEnvironment(Environment environment,
                                                        Map<String, Repository> repositories,
                                                        Map<String, Resolver> resolvers) {
        return new EclipseJavaFormatModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "eclipse-java-format",
                !environment.flag("format.rewrite", false),
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "eclipse-java-format"));
    }

    private EclipseJavaFormatModule(Dependencies dependencies,
                                    Pinning pinning,
                                    String group,
                                    boolean verify,
                                    Path profile,
                                    ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.group = group;
        this.verify = verify;
        this.profile = profile;
        this.terms = terms;
    }

    public EclipseJavaFormatModule pinning(Pinning pinning) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms);
    }

    public EclipseJavaFormatModule group(String group) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms);
    }

    public EclipseJavaFormatModule verify(boolean verify) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms);
    }

    public EclipseJavaFormatModule profile(Path profile) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms);
    }

    public EclipseJavaFormatModule printing(BiConsumer<Boolean, String> printing) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms.printing(printing));
    }

    public EclipseJavaFormatModule permits(Permits permits) {
        return new EclipseJavaFormatModule(dependencies, pinning, group, verify, profile, terms.permits(permits));
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
        if (profile != null) {
            buildExecutor.addSource(CONFIGURATION, new Bind(Map.of(Path.of(""), Path.of(PROFILE))), profile);
            formatInputs.add(CONFIGURATION);
        }
        formatInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(FORMAT, new Format(terms, group, verify), formatInputs);
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

        private Format(ProcessBuildStep.Terms terms, String group, boolean verify) {
            super("eclipse-java-format", group, verify, terms);
        }

        @Override
        protected boolean isFormattable(Path file) {
            return file.toString().endsWith(".java")
                    && !file.getFileName().toString().equals("module-info.java");
        }

        @Override
        protected Path config(Path folder) {
            Path candidate = folder.resolve(PROFILE);
            return Files.isRegularFile(candidate) ? candidate : null;
        }

        @Override
        protected List<String> command(BuildStepContext context,
                                       List<String> jars,
                                       Path config,
                                       List<String> files,
                                       boolean verify) throws IOException {
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(EclipseJavaFormatDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    EclipseJavaFormatDriver.class.getName(),
                    verify ? "verify" : "rewrite",
                    config == null ? "" : config.toString()));
            commands.addAll(files);
            return commands;
        }
    }
}
