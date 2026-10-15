package build.jenesis.plugin.checkstyle;

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
import build.jenesis.project.Enforcement;
import build.jenesis.step.Bind;
import build.jenesis.step.Dependencies;
import build.jenesis.step.Findings;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class CheckstyleModule implements BuildExecutorModule {

    public static final String CHECK = "check";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String PROPERTIES = "checkstyle.properties", CONFIG_LOC_KEY = "config_loc";
    static final String SETTING = "source.checkstyle";
    private static final String REPORT = BuildStep.REPORTS + "checkstyle/checkstyle-report.xml";
    private static final String MAVEN_GROUP = "com.puppycrawl.tools", MAVEN_ARTIFACT = "checkstyle";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String configFile;
    private final boolean strict;
    private final SequencedMap<String, String> expansions;
    private final ProcessBuildStep.Terms terms;

    public CheckstyleModule(Map<String, Repository> repositories,
                            Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "checkstyle",
             "checkstyle.xml",
             false,
             Collections.emptySortedMap(),
             new ProcessBuildStep.Terms());
    }

    public static CheckstyleModule ofEnvironment(Environment environment,
                                                 Map<String, Repository> repositories,
                                                 Map<String, Resolver> resolvers) {
        return new CheckstyleModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "checkstyle",
                "checkstyle.xml",
                Enforcement.ofEnvironment(environment, SETTING) == Enforcement.STRICT,
                Collections.emptySortedMap(),
                ProcessBuildStep.Terms.ofEnvironment(environment, "checkstyle"));
    }

    private CheckstyleModule(Dependencies dependencies,
                             Pinning pinning,
                             String tool,
                             String configFile,
                             boolean strict,
                             SequencedMap<String, String> expansions,
                             ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.configFile = configFile;
        this.strict = strict;
        this.expansions = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(expansions));
        this.terms = terms;
    }

    public CheckstyleModule pinning(Pinning pinning) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms);
    }

    public CheckstyleModule tool(String tool) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms);
    }

    public CheckstyleModule configFile(String configFile) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms);
    }

    public CheckstyleModule strict(boolean strict) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms);
    }

    public CheckstyleModule expansions(SequencedMap<String, String> expansions) {
        if (expansions.containsKey(CONFIG_LOC_KEY)) {
            throw new IllegalArgumentException("The expansions set " + CONFIG_LOC_KEY + ", which the build sets to the"
                    + " folder of the configuration - remove it");
        }
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms);
    }

    public CheckstyleModule printing(BiConsumer<Boolean, String> printing) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms.printing(printing));
    }

    public CheckstyleModule permits(Permits permits) {
        return new CheckstyleModule(dependencies, pinning, tool, configFile, strict, expansions, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(REQUIRED, new Requires(tool), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(tool),
                resolveInputs);
        SequencedSet<String> checkInputs = new LinkedHashSet<>();
        checkInputs.add(DEPENDENCIES);
        checkInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(CHECK, new Check(terms, tool, configFile, strict, expansions), checkInputs);
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
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Check extends ProcessBuildStep {

        private final String tool;
        private final String configFile;
        private final boolean strict;
        private final SequencedMap<String, String> expansions;

        private Check(ProcessBuildStep.Terms terms,
                      String tool,
                      String configFile,
                      boolean strict,
                      SequencedMap<String, String> expansions) {
            super("checkstyle", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.configFile = configFile;
            this.strict = strict;
            this.expansions = expansions;
        }

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            List<String> folders = arguments.values().stream()
                    .filter(argument -> !argument.removed())
                    .map(argument -> "name=\"" + (argument.folder().resolve(Bind.SOURCES).toAbsolutePath()
                            + File.separator)
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&apos;"))
                    .toList();
            return super.apply(executor, context, arguments).thenCompose(result -> {
                Path report = context.next().resolve(REPORT);
                try {
                    if (Files.isRegularFile(report)) {
                        String content = Files.readString(report), named = content;
                        for (String folder : folders) {
                            named = named.replace(folder, "name=\"");
                        }
                        if (!named.equals(content)) {
                            Files.writeString(report, named);
                        }
                    }
                    return CompletableFuture.completedStage(result);
                } catch (IOException e) {
                    return CompletableFuture.failedStage(e);
                }
            });
        }

        @Override
        public boolean acceptableExitCode(int code,
                                          Executor executor,
                                          BuildStepContext context,
                                          SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            Path report = context.next().resolve(REPORT);
            return Findings.ofXml("checkstyle", report, "error")
                    .acceptable(code, context, true, strict, SETTING, terms.reporting());
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            List<String> jars = new ArrayList<>(), files = new ArrayList<>();
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
                Path candidate = argument.folder().resolve(configFile);
                if (Files.isRegularFile(candidate)) {
                    config = candidate;
                }
                Path sources = argument.folder().resolve(Bind.SOURCES);
                if (Files.exists(sources)) {
                    Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.toString().endsWith(".java")
                                    && !file.getFileName().toString().equals("module-info.java")) {
                                files.add(file.toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            if (files.isEmpty()) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No Checkstyle jars resolved upstream of the Checkstyle step");
            }
            if (config == null) {
                throw new IllegalStateException("No " + configFile + " found among the inputs of the Checkstyle step");
            }
            files.sort(null);
            Path report = context.next().resolve(REPORT);
            Files.createDirectories(report.getParent());
            SequencedProperties expanded = new SequencedProperties();
            expanded.putAll(expansions);
            expanded.setProperty(CONFIG_LOC_KEY, config.getParent().toString());
            Path expansionFile = context.supplement().resolve(PROPERTIES);
            expanded.store(expansionFile);
            List<String> commands = new ArrayList<>(List.of(
                    "-cp", ProcessHandler.OfProcess.driver(CheckstyleDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    CheckstyleDriver.class.getName(),
                    "-c", config.toString(),
                    "-p", expansionFile.toString(),
                    "-f", "xml",
                    "-o", report.toString()));
            commands.addAll(files);
            return CompletableFuture.completedStage(commands);
        }
    }
}
