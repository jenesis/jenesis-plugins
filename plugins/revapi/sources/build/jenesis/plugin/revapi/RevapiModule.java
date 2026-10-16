package build.jenesis.plugin.revapi;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.Environment;
import build.jenesis.Json;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.project.Enforcement;
import build.jenesis.step.Dependencies;
import build.jenesis.step.Findings;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class RevapiModule implements BuildExecutorModule {

    public static final String COMPARE = "compare", CONFIGURATION = "revapi.properties", ANALYSIS = "revapi.json";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    static final String SETTING = "artifact.revapi", REPORT = BuildStep.REPORTS + "revapi/revapi.json";
    private static final String MAVEN_GROUP = "org.revapi";
    private static final List<String> EXTENSIONS = List.of("revapi-java", "revapi-basic-features", "revapi-reporter-json");

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final String baseline;
    private final Severity severity;
    private final boolean strict;
    private final ProcessBuildStep.Terms terms;

    public RevapiModule(Map<String, Repository> repositories,
                        Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
                null,
                "revapi",
                "main",
                null,
                Severity.POTENTIALLY_BREAKING,
                false,
                new ProcessBuildStep.Terms());
    }

    public static RevapiModule ofEnvironment(Environment environment,
                                             Map<String, Repository> repositories,
                                             Map<String, Resolver> resolvers) {
        return new RevapiModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "revapi",
                "main",
                null,
                Severity.POTENTIALLY_BREAKING,
                Enforcement.ofEnvironment(environment, SETTING) == Enforcement.STRICT,
                ProcessBuildStep.Terms.ofEnvironment(environment, "revapi"));
    }

    private RevapiModule(Dependencies dependencies,
                         Pinning pinning,
                         String tool,
                         String group,
                         String baseline,
                         Severity severity,
                         boolean strict,
                         ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.group = group;
        this.baseline = baseline;
        this.severity = severity;
        this.strict = strict;
        this.terms = terms;
    }

    public RevapiModule pinning(Pinning pinning) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule tool(String tool) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule group(String group) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule baseline(String baseline) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule severity(Severity severity) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule strict(boolean strict) {
        return new RevapiModule(dependencies, pinning, tool, group, baseline, severity, strict, terms);
    }

    public RevapiModule printing(BiConsumer<Boolean, String> printing) {
        return new RevapiModule(dependencies,
                pinning,
                tool,
                group,
                baseline,
                severity,
                strict,
                terms.printing(printing));
    }

    public RevapiModule permits(Permits permits) {
        return new RevapiModule(dependencies,
                pinning,
                tool,
                group,
                baseline,
                severity,
                strict,
                terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) throws IOException {
        Path described = BuildStep.locate(new LinkedHashSet<>(inherited.values()), BuildStep.MODULE);
        if (described != null && SequencedProperties.ofFiles(described).getProperty("test") != null) {
            return;
        }
        buildExecutor.addStep(REQUIRED,
                new Baseline(tool,
                        baseline,
                        EXTENSIONS.stream().map(artifact -> MAVEN_GROUP + "/" + artifact).toList(),
                        CONFIGURATION),
                inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(tool),
                resolveInputs);
        SequencedSet<String> compareInputs = new LinkedHashSet<>();
        compareInputs.add(DEPENDENCIES);
        compareInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(COMPARE, new Compare(terms, tool, group, severity, strict), compareInputs);
    }

    private static class Compare extends ProcessBuildStep {

        private final String tool;
        private final String group;
        private final Severity severity;
        private final boolean strict;

        private Compare(ProcessBuildStep.Terms terms,
                        String tool,
                        String group,
                        Severity severity,
                        boolean strict) {
            super("revapi", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.group = group;
            this.severity = severity;
            this.strict = strict;
        }

        @Override
        public boolean acceptableExitCode(int code,
                                          Executor executor,
                                          BuildStepContext context,
                                          SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            Path report = context.next().resolve(REPORT);
            if (code != 0 || !Files.isRegularFile(report)) {
                return false;
            }
            return new Findings("revapi", report, Json.parse(Files.readString(report)) instanceof List<?> found
                    ? found.size()
                    : -1).acceptable(code, context, false, strict, SETTING, terms.reporting());
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            List<String> jars = new ArrayList<>(), baselines = new ArrayList<>(), support = new ArrayList<>();
            Path artifact = null, analysis = null;
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "runtime")) {
                    jars.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "baseline")) {
                    baselines.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), group, "compile")) {
                    support.add(jar.toString());
                }
                Path candidate = argument.folder().resolve(ANALYSIS);
                if (Files.isRegularFile(candidate)) {
                    analysis = candidate;
                }
                Path artifacts = argument.folder().resolve(BuildStep.ARTIFACTS);
                if (Files.isDirectory(artifacts)) {
                    try (DirectoryStream<Path> files = Files.newDirectoryStream(artifacts, "*.jar")) {
                        for (Path file : files) {
                            artifact = file;
                        }
                    }
                }
            }
            if (artifact == null) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No revapi jars resolved upstream of the revapi step");
            }
            if (baselines.size() != 1) {
                throw new IllegalStateException("Expected exactly one baseline artifact upstream of the revapi step: "
                        + baselines);
            }
            Path report = context.next().resolve(REPORT);
            Files.createDirectories(report.getParent());
            if (analysis != null && !(Json.parse(Files.readString(analysis)) instanceof List<?>)) {
                throw new IllegalArgumentException(analysis + " is no JSON array - write revapi's configuration as an"
                        + " array of {\"extension\": ..., \"configuration\": ...} objects");
            }
            String configuration = analysis == null ? "-" : analysis.toString();
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(RevapiDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    RevapiDriver.class.getName(),
                    report.toString(),
                    severity.name(),
                    configuration,
                    baselines.getFirst(),
                    artifact.toString()));
            commands.addAll(support);
            return CompletableFuture.completedStage(commands);
        }
    }

    public enum Severity {

        EQUIVALENT, NON_BREAKING, POTENTIALLY_BREAKING, BREAKING
    }
}
