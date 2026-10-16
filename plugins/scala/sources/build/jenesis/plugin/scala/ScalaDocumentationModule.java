package build.jenesis.plugin.scala;

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
import build.jenesis.step.Javac;
import build.jenesis.step.Javadoc;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class ScalaDocumentationModule implements BuildExecutorModule {

    public static final String DOCUMENTED = "documented", RESOLVED_ARTIFACTS = "artifacts";
    private static final String REQUIRED = "required", RESOLUTION = "dependencies";
    private static final List<String> PREFERRED_PREFIXES = List.of("maven", "module");
    private static final String MODULE_NAME = "org.scala.lang.scaladoc",
            MAVEN_GROUP = "org.scala-lang",
            MAVEN_ARTIFACT = "scaladoc_3";

    private final Map<String, Resolver> resolvers;
    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final Function<List<String>, ? extends ProcessHandler> factory;
    private final ProcessBuildStep.Terms terms;

    public ScalaDocumentationModule(Map<String, Repository> repositories,
                                    Map<String, Resolver> resolvers) {
        this(resolvers,
             new Dependencies(repositories, resolvers),
             null,
             "scaladoc",
             "main",
             null,
             new ProcessBuildStep.Terms());
    }

    public static ScalaDocumentationModule ofEnvironment(Environment environment,
                                                         Map<String, Repository> repositories,
                                                         Map<String, Resolver> resolvers) {
        return new ScalaDocumentationModule(resolvers,
                Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "scaladoc",
                "main",
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "scaladoc"));
    }

    private ScalaDocumentationModule(Map<String, Resolver> resolvers,
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

    public ScalaDocumentationModule factory(Function<List<String>, ? extends ProcessHandler> factory) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public ScalaDocumentationModule pinning(Pinning pinning) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public ScalaDocumentationModule tool(String tool) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public ScalaDocumentationModule group(String group) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms);
    }

    public ScalaDocumentationModule printing(BiConsumer<Boolean, String> printing) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms.printing(printing));
    }

    public ScalaDocumentationModule permits(Permits permits) {
        return new ScalaDocumentationModule(resolvers, dependencies, pinning, tool, group, factory, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        SequencedSet<String> upstream = inherited.sequencedKeySet();
        buildExecutor.addStep(REQUIRED, new Requires(Set.copyOf(resolvers.keySet()), tool), upstream);
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(upstream);
        buildExecutor.addModule(RESOLUTION,
                dependencies.pinning(pinning).group(tool),
                resolveInputs);
        SequencedSet<String> documentInputs = new LinkedHashSet<>();
        documentInputs.add(RESOLUTION);
        documentInputs.addAll(upstream);
        buildExecutor.addStep(DOCUMENTED,
                factory == null ? new Document(terms, tool, group) : new Document(terms, tool, group, factory),
                documentInputs);
    }

    @Override
    public Optional<String> resolve(String path) {
        if (path.equals(DOCUMENTED)) {
            return Optional.of(DOCUMENTED);
        }
        if (path.equals(RESOLUTION)) {
            return Optional.of(RESOLVED_ARTIFACTS);
        }
        return Optional.empty();
    }

    private record Requires(Set<String> prefixes, String tool) implements BuildStep {

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            String selectedPrefix = null;
            for (String prefix : PREFERRED_PREFIXES) {
                if (prefixes.contains(prefix)) {
                    selectedPrefix = prefix;
                    break;
                }
            }
            if (selectedPrefix == null) {
                throw new IllegalStateException(
                        "No suitable resolver for Scala documentation. Available prefixes: " + prefixes
                                + ". Expected one of: " + PREFERRED_PREFIXES);
            }
            String coordinate = switch (selectedPrefix) {
                case "module" -> selectedPrefix + "/" + MODULE_NAME;
                case "maven" -> selectedPrefix + "/" + MAVEN_GROUP + "/" + MAVEN_ARTIFACT + "/RELEASE";
                default -> throw new IllegalStateException("Unreachable");
            };
            SequencedProperties requires = new SequencedProperties();
            requires.setProperty(tool + "/runtime/" + coordinate, "");
            if (selectedPrefix.equals("maven")) {
                requires.setProperty(tool + "/runtime/" + selectedPrefix + "/com.fasterxml.jackson.core/jackson-annotations/2.21", "");
            }
            requires.store(context.next().resolve(REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Document extends ProcessBuildStep {

        private final String tool;
        private final String group;

        private Document(ProcessBuildStep.Terms terms,
                         String tool,
                         String group) {
            this(terms, tool, group, ProcessHandler.OfProcess.ofJavaHome("bin/java"));
        }

        private Document(ProcessBuildStep.Terms terms,
                         String tool,
                         String group,
                         Function<List<String>, ? extends ProcessHandler> factory) {
            super("scaladoc", factory, terms);
            this.tool = tool;
            this.group = group;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            return Javac.hasRelevantChange(arguments,
                    Set.of(".scala"),
                    Set.of("scaladoc"),
                    false);
        }

        @Override
        public boolean acceptableExitCode(int code,
                                          Executor executor,
                                          BuildStepContext context,
                                          SequencedMap<String, BuildStepArgument> arguments) {
            return true;
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            Path output = Files.createDirectories(context.next().resolve(Javadoc.JAVADOC));
            List<String> files = new ArrayList<>(),
                    jars = new ArrayList<>(),
                    classRoots = new ArrayList<>(),
                    classpath = new ArrayList<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path classes = argument.folder().resolve(CLASSES);
                if (Files.exists(classes)) {
                    classRoots.add(classes.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "runtime")) {
                    jars.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), group, "compile")) {
                    classpath.add(jar.toString());
                }
                Path sources = argument.folder().resolve(Bind.SOURCES);
                if (Files.exists(sources)) {
                    Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String name = file.toString();
                            if (name.endsWith(".scala")) {
                                files.add(name);
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
                throw new IllegalStateException(
                        "No scaladoc jars resolved upstream of the Scala documentation step");
            }
            if (classRoots.isEmpty()) {
                return CompletableFuture.completedStage(null);
            }
            List<String> userClasspath = new ArrayList<>(classRoots);
            userClasspath.addAll(classpath);
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    String.join(File.pathSeparator, jars),
                    "dotty.tools.scaladoc.Main",
                    "-d",
                    output.toString(),
                    "-classpath",
                    String.join(File.pathSeparator, userClasspath),
                    "-project",
                    "documentation"));
            commands.addAll(classRoots);
            return CompletableFuture.completedStage(commands);
        }
    }
}
