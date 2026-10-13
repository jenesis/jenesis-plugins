package build.jenesis.plugin.groovy;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.Environment;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.step.Dependencies;
import build.jenesis.step.Javac;
import build.jenesis.step.Javadoc;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class GroovyDocumentationModule implements BuildExecutorModule {

    public static final String REQUIRED = "required", RESOLVED = "dependencies", DOCUMENTED = "documented";
    private static final String ARTIFACTS = "artifacts";

    private final Map<String, Resolver> resolvers;
    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final boolean includeJava;
    private final boolean timestamped;
    private final Function<List<String>, ? extends ProcessHandler> factory;
    private final ProcessBuildStep.Terms terms;

    public GroovyDocumentationModule(Map<String, Repository> repositories,
                                     Map<String, Resolver> resolvers) {
        this(resolvers,
                new Dependencies(repositories, resolvers),
                null,
                "groovy",
                "main",
                false,
                false,
                null,
                new ProcessBuildStep.Terms());
    }

    public static GroovyDocumentationModule ofEnvironment(Environment environment,
                                                          Map<String, Repository> repositories,
                                                          Map<String, Resolver> resolvers) {
        return new GroovyDocumentationModule(resolvers,
                Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "groovy",
                "main",
                false,
                BuildStep.timestamp(environment) == null,
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "groovydoc"));
    }

    private GroovyDocumentationModule(Map<String, Resolver> resolvers,
                                      Dependencies dependencies,
                                      Pinning pinning,
                                      String tool,
                                      String group,
                                      boolean includeJava,
                                      boolean timestamped,
                                      Function<List<String>, ? extends ProcessHandler> factory,
                                      ProcessBuildStep.Terms terms) {
        this.resolvers = resolvers;
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.group = group;
        this.includeJava = includeJava;
        this.timestamped = timestamped;
        this.factory = factory;
        this.terms = terms;
    }

    public GroovyDocumentationModule pinning(Pinning pinning) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule tool(String tool) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule group(String group) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule includeJava(boolean includeJava) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule timestamped(boolean timestamped) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule factory(Function<List<String>, ? extends ProcessHandler> factory) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms);
    }

    public GroovyDocumentationModule printing(BiConsumer<Boolean, String> printing) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms.printing(printing));
    }

    public GroovyDocumentationModule permits(Semaphore permits) {
        return new GroovyDocumentationModule(resolvers, dependencies, pinning, tool, group, includeJava, timestamped, factory, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        SequencedSet<String> upstream = inherited.sequencedKeySet();
        buildExecutor.addStep(REQUIRED,
                new GroovyRequires(new TreeSet<>(resolvers.keySet()),
                        tool,
                        "org.apache.groovy.groovydoc",
                        "groovy-groovydoc",
                        "Groovy documentation"),
                upstream);
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(upstream);
        buildExecutor.addModule(RESOLVED, dependencies.pinning(pinning).group(tool), resolveInputs);
        SequencedSet<String> documentInputs = new LinkedHashSet<>();
        documentInputs.add(RESOLVED);
        documentInputs.addAll(upstream);
        buildExecutor.addStep(DOCUMENTED,
                factory == null
                        ? new Document(terms, includeJava, tool, group, timestamped)
                        : new Document(terms, includeJava, tool, group, timestamped, factory),
                documentInputs);
    }

    @Override
    public Optional<String> resolve(String path) {
        return switch (path) {
            case DOCUMENTED -> Optional.of(DOCUMENTED);
            case RESOLVED -> Optional.of(ARTIFACTS);
            default -> Optional.empty();
        };
    }

    private static class Document extends ProcessBuildStep {

        private final boolean includeJava;
        private final String tool;
        private final String group;
        private final boolean timestamped;

        private Document(ProcessBuildStep.Terms terms,
                         boolean includeJava,
                         String tool,
                         String group,
                         boolean timestamped) {
            this(terms, includeJava, tool, group, timestamped, ProcessHandler.OfProcess.ofJavaHome("bin/java"));
        }

        private Document(ProcessBuildStep.Terms terms,
                         boolean includeJava,
                         String tool,
                         String group,
                         boolean timestamped,
                         Function<List<String>, ? extends ProcessHandler> factory) {
            super("groovydoc", factory, terms);
            this.includeJava = includeJava;
            this.tool = tool;
            this.group = group;
            this.timestamped = timestamped;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            return Javac.hasRelevantChange(arguments,
                    Set.of(".groovy", ".java"),
                    Set.of("groovydoc", "javac"),
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
            List<String> roots = new ArrayList<>(), rootFiles = new ArrayList<>(), jars = new ArrayList<>(),
                    classpath = new ArrayList<>();
            SequencedSet<String> packages = new TreeSet<>();
            boolean[] groovy = new boolean[1];
            String release = null;
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
                    jars.add(jar.toString());
                }
                Path javac = ProcessBuildStep.locate(argument.folder(), ProcessBuildStep.PROCESS, "javac");
                if (javac != null) {
                    String value = SequencedProperties.ofFiles(javac).value("--release");
                    if (value != null) {
                        release = value;
                    }
                }
                Path sources = argument.folder().resolve(SOURCES);
                if (Files.exists(sources)) {
                    roots.add(sources.toString());
                    Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String name = file.getFileName().toString();
                            boolean source = name.endsWith(".groovy");
                            if (source || includeJava && name.endsWith(".java") && !name.equals("module-info.java")) {
                                groovy[0] |= source;
                                Path parent = sources.relativize(file).getParent();
                                if (parent == null) {
                                    rootFiles.add(file.toString());
                                } else {
                                    packages.add(parent.toString().replace(File.separatorChar, '.'));
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            if (!groovy[0]) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No groovydoc jars resolved upstream of the Groovy documentation step");
            }
            List<String> commands = new ArrayList<>(List.of(
                    "-cp", String.join(File.pathSeparator, Stream.concat(jars.stream(), classpath.stream()).toList()),
                    "org.codehaus.groovy.tools.groovydoc.Main",
                    "-d", output.toString(),
                    "-javaVersion", languageLevel(release),
                    "-sourcepath", String.join(File.pathSeparator, roots)));
            if (!timestamped) {
                commands.add("-notimestamp");
            }
            commands.addAll(packages);
            commands.addAll(rootFiles);
            return CompletableFuture.completedStage(commands);
        }

        private static String languageLevel(String release) {
            String feature = release == null ? "21" : release;
            if (feature.startsWith("1.")) {
                feature = feature.substring(2);
            }
            int level;
            try {
                level = Integer.parseInt(feature);
            } catch (NumberFormatException _) {
                level = 21;
            }
            return "JAVA_" + Math.clamp(level, 8, 25);
        }
    }
}
