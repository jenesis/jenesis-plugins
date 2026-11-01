package build.jenesis.plugin.spotless;

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

public class SpotlessModule implements BuildExecutorModule {

    public static final String FORMAT = "format";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies", TOOL = "tool";
    private static final List<String> LIBRARIES = List.of("com.diffplug.spotless/spotless-lib", "org.slf4j/slf4j-api"),
            JAVAC = List.of("api", "code", "file", "main", "parser", "tree", "util");

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String group;
    private final boolean verify;
    private final List<SpotlessSet> sets;
    private final ProcessBuildStep.Terms terms;

    public SpotlessModule(Map<String, Repository> repositories,
                          Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "spotless",
             true,
             List.of(),
             new ProcessBuildStep.Terms());
    }

    public static SpotlessModule ofEnvironment(Environment environment,
                                               Map<String, Repository> repositories,
                                               Map<String, Resolver> resolvers) {
        return new SpotlessModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "spotless",
                !environment.flag("format.rewrite", false, Setting.PROJECT),
                List.of(),
                ProcessBuildStep.Terms.ofEnvironment(environment, "spotless"));
    }

    private SpotlessModule(Dependencies dependencies,
                           Pinning pinning,
                           String group,
                           boolean verify,
                           List<SpotlessSet> sets,
                           ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.group = group;
        this.verify = verify;
        this.sets = sets;
        this.terms = terms;
    }

    public SpotlessModule pinning(Pinning pinning) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms);
    }

    public SpotlessModule group(String group) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms);
    }

    public SpotlessModule verify(boolean verify) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms);
    }

    public SpotlessModule sets(List<SpotlessSet> sets) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms);
    }

    public SpotlessModule printing(BiConsumer<Boolean, String> printing) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms.printing(printing));
    }

    public SpotlessModule permits(Permits permits) {
        return new SpotlessModule(dependencies, pinning, group, verify, sets, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) throws IOException {
        if (sets.isEmpty()) {
            throw new IllegalStateException("The Spotless module formats no files - name a set of files and its"
                    + " steps with sets(...)");
        }
        SequencedMap<String, String> tools = new LinkedHashMap<>();
        for (SpotlessStep step : sets.stream().flatMap(set -> set.steps().stream()).toList()) {
            if (step.artifact() == null) {
                continue;
            }
            String purpose = step.name().toLowerCase(Locale.ROOT),
                    coordinate = step.artifact() + "/" + (step.version() == null ? "RELEASE" : step.version()),
                    previous = tools.putIfAbsent(purpose, coordinate);
            if (previous != null && !previous.equals(coordinate)) {
                throw new IllegalArgumentException("The Spotless step " + step.name() + " is asked for as "
                        + previous + " and as " + coordinate + " - give every set the same version of it");
            }
        }
        resolve(buildExecutor, inherited.sequencedKeySet(), TOOL, LIBRARIES.stream()
                .map(library -> library + "/RELEASE")
                .toList());
        SequencedSet<String> formatInputs = new LinkedHashSet<>();
        formatInputs.add(DEPENDENCIES);
        for (Map.Entry<String, String> tool : tools.entrySet()) {
            buildExecutor.addModule(tool.getKey(),
                    (nested, nestedInherited) -> resolve(nested,
                            nestedInherited.sequencedKeySet(),
                            tool.getKey(),
                            List.of(tool.getValue())),
                    inherited.sequencedKeySet());
            formatInputs.add(tool.getKey());
        }
        formatInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(FORMAT,
                new Format(terms, group, verify, sets, List.copyOf(tools.sequencedKeySet())),
                formatInputs);
    }

    private void resolve(BuildExecutor buildExecutor,
                         SequencedSet<String> inherited,
                         String purpose,
                         List<String> coordinates) {
        buildExecutor.addStep(REQUIRED, new Requires(group + ":" + purpose, coordinates), inherited);
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited);
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(group + ":" + purpose),
                resolveInputs);
    }

    private record Requires(String group, List<String> coordinates) implements BuildStep {

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
            for (String coordinate : coordinates) {
                requires.setProperty(group + "/runtime/maven/" + coordinate, "");
            }
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Format extends FormatBuildStep {

        private final String group;
        private final List<SpotlessSet> sets;
        private final List<String> tools;

        private Format(ProcessBuildStep.Terms terms,
                       String group,
                       boolean verify,
                       List<SpotlessSet> sets,
                       List<String> tools) {
            super("spotless", group + ":" + TOOL, verify, terms);
            this.group = group;
            this.sets = sets;
            this.tools = tools;
        }

        @Override
        protected boolean isFormattable(Path file) {
            return sets.stream().anyMatch(set -> set.matches(file));
        }

        @Override
        protected CompletionStage<List<String>> process(Executor executor,
                                                        BuildStepContext context,
                                                        SequencedMap<String, BuildStepArgument> arguments,
                                                        SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            List<String> provisions = new ArrayList<>();
            for (String tool : tools) {
                List<String> jars = new ArrayList<>();
                for (BuildStepArgument argument : arguments.values()) {
                    if (!argument.removed()) {
                        for (Path jar : Dependencies.select(argument.folder(), group + ":" + tool, "runtime")) {
                            jars.add(jar.toString());
                        }
                    }
                }
                if (jars.isEmpty()) {
                    throw new IllegalStateException("No jars resolved in " + group + ":" + tool
                            + " upstream of the Spotless step");
                }
                provisions.add("--provision=" + tool + "=" + String.join(File.pathSeparator, jars));
            }
            return super.process(executor, context, arguments, properties).thenApply(commands -> {
                if (commands == null) {
                    return null;
                }
                List<String> provisioned = new ArrayList<>(commands);
                provisioned.addAll(provisioned.lastIndexOf("--"), provisions);
                return provisioned;
            });
        }

        @Override
        protected List<String> command(BuildStepContext context,
                                       List<String> jars,
                                       Path config,
                                       List<String> files,
                                       boolean verify) throws IOException {
            List<String> commands = new ArrayList<>();
            if (sets.stream().flatMap(set -> set.steps().stream()).anyMatch(SpotlessStep::javac)) {
                JAVAC.forEach(name -> commands.add("--add-exports=jdk.compiler/com.sun.tools.javac." + name
                        + "=ALL-UNNAMED"));
            }
            commands.add("-cp");
            commands.add(ProcessHandler.OfProcess.driver(SpotlessDriver.class) + File.pathSeparator
                    + String.join(File.pathSeparator, jars));
            commands.add(SpotlessDriver.class.getName());
            if (verify) {
                commands.add("--check");
            }
            for (SpotlessSet set : sets) {
                commands.add("--set=" + set.name());
                set.targets().forEach(target -> commands.add("--target=" + target));
                for (SpotlessStep step : set.steps()) {
                    commands.add("--step=" + step.name());
                    step.options().forEach((key, value) -> commands.add("--" + key + "=" + value.replace("\\", "\\\\")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")));
                }
            }
            commands.add("--");
            commands.addAll(files);
            return commands;
        }
    }
}
