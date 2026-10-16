package build.jenesis.plugin.groovy;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.step.Dependencies;
import build.jenesis.step.Javac;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;
import build.jenesis.step.Versions;

public class GroovyCompilerModule implements BuildExecutorModule {

    public static final String REQUIRED = "required",
            RESOLVED = "dependencies",
            COMPILED = "compiled",
            VERSIONED = "classes";
    private static final String ARTIFACTS = "artifacts";

    private final Map<String, Resolver> resolvers;
    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final Function<List<String>, ? extends ProcessHandler> factory;
    private final ProcessBuildStep.Terms terms;

    public GroovyCompilerModule(Map<String, Repository> repositories,
                                Map<String, Resolver> resolvers) {
        this(resolvers,
                new Dependencies(repositories, resolvers),
                null,
                "groovy",
                "main",
                null,
                new ProcessBuildStep.Terms());
    }

    public static GroovyCompilerModule ofEnvironment(Environment environment,
                                                     Map<String, Repository> repositories,
                                                     Map<String, Resolver> resolvers) {
        return new GroovyCompilerModule(resolvers,
                Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "groovy",
                "main",
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "groovyc"));
    }

    private GroovyCompilerModule(Map<String, Resolver> resolvers,
                                 Dependencies dependencies,
                                 Pinning pinning,
                                 String tool,
                                 String group,
                                 Function<List<String>, ? extends ProcessHandler> factory,
                                 ProcessBuildStep.Terms terms) {
        this.resolvers = resolvers;
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.group = group;
        this.factory = factory;
        this.terms = terms;
    }

    public GroovyCompilerModule pinning(Pinning pinning) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public GroovyCompilerModule tool(String tool) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public GroovyCompilerModule group(String group) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public GroovyCompilerModule factory(Function<List<String>, ? extends ProcessHandler> factory) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public GroovyCompilerModule printing(BiConsumer<Boolean, String> printing) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms.printing(printing));
    }

    public GroovyCompilerModule permits(Permits permits) {
        return new GroovyCompilerModule(resolvers, dependencies, pinning, tool, group, factory, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        SequencedSet<String> upstream = inherited.sequencedKeySet();
        buildExecutor.addStep(REQUIRED,
                new GroovyRequires(new TreeSet<>(resolvers.keySet()),
                        tool,
                        "org.apache.groovy",
                        "groovy",
                        "the Groovy compiler"),
                upstream);
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(upstream);
        buildExecutor.addModule(RESOLVED, dependencies.pinning(pinning).group(tool), resolveInputs);
        SequencedSet<String> compileInputs = new LinkedHashSet<>();
        compileInputs.add(RESOLVED);
        compileInputs.addAll(upstream);
        buildExecutor.addStep(COMPILED,
                factory == null ? new Compile(terms, tool, group) : new Compile(terms, tool, group, factory),
                compileInputs);
        buildExecutor.addStep(VERSIONED, new Versions(), COMPILED);
    }

    @Override
    public Optional<String> resolve(String path) {
        return switch (path) {
            case VERSIONED -> Optional.of(VERSIONED);
            case RESOLVED -> Optional.of(ARTIFACTS);
            default -> Optional.empty();
        };
    }

    private static class Compile extends ProcessBuildStep {

        private final String tool;
        private final String group;

        private Compile(ProcessBuildStep.Terms terms, String tool, String group) {
            this(terms, tool, group, ProcessHandler.OfProcess.ofJavaHome("bin/java"));
        }

        private Compile(ProcessBuildStep.Terms terms,
                        String tool,
                        String group,
                        Function<List<String>, ? extends ProcessHandler> factory) {
            super("groovyc", factory, terms);
            this.tool = tool;
            this.group = group;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            return Javac.hasRelevantChange(arguments,
                    Set.of(".groovy", ".java"),
                    Set.of("groovyc", "javac"),
                    false);
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            Path target = Files.createDirectory(context.next().resolve(CLASSES));
            List<String> files = new ArrayList<>(), jars = new ArrayList<>(), classpath = new ArrayList<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path classes = argument.folder().resolve(CLASSES);
                if (Files.exists(classes)) {
                    classpath.add(classes.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "runtime")) {
                    jars.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), group, "compile")) {
                    classpath.add(jar.toString());
                }
                Path sources = argument.folder().resolve(SOURCES);
                if (Files.exists(sources)) {
                    Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            return BuildStep.underBuildJenesis(sources.relativize(dir))
                                    ? FileVisitResult.SKIP_SUBTREE
                                    : FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.getFileName().toString().endsWith(".groovy")) {
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
            files.sort(null);
            if (jars.isEmpty()) {
                throw new IllegalStateException("No compiler jars resolved upstream of the Groovy compile step");
            }
            List<String> compiling = Stream.concat(jars.stream(), classpath.stream()).toList();
            for (String entry : compiling) {
                if (entry.indexOf(File.pathSeparatorChar) != -1) {
                    throw new IllegalArgumentException("Path entry contains separator '" + File.pathSeparator + "': "
                            + entry);
                }
            }
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    String.join(File.pathSeparator, jars),
                    "org.codehaus.groovy.tools.FileSystemCompiler",
                    "-d",
                    target.toString(),
                    "--classpath",
                    String.join(File.pathSeparator, compiling)));
            commands.addAll(files);
            return CompletableFuture.completedStage(commands);
        }
    }
}
