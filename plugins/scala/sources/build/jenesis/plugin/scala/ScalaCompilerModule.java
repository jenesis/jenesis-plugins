package build.jenesis.plugin.scala;

import module java.base;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
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
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class ScalaCompilerModule implements BuildExecutorModule {

    public static final String RESOLVED_ARTIFACTS = "artifacts", COMPILED_CLASSES = "classes";
    private static final String REQUIRED = "required",
            RESOLUTION = "dependencies",
            ZINC_RESOLUTION = "zinc",
            STATE = "state.properties";
    private static final List<String> PREFERRED_PREFIXES = List.of("maven", "module");
    private static final String MODULE_NAME = "org.scala.lang.scala3.compiler",
            MAVEN_GROUP = "org.scala-lang",
            MAVEN_ARTIFACT = "scala3-compiler_3",
            BRIDGE_ARTIFACT = "scala3-sbt-bridge",
            ZINC_GROUP = "org.scala-sbt",
            ZINC_ARTIFACT = "zinc_2.13";

    private final Map<String, Resolver> resolvers;
    private final Dependencies dependencies;
    private final Pinning pinning;
    private final boolean incremental;
    private final String tool;
    private final String zinc;
    private final String group;
    private final Function<List<String>, ? extends ProcessHandler> factory;
    private final ProcessBuildStep.Terms terms;

    public ScalaCompilerModule(Map<String, Repository> repositories,
                               Map<String, Resolver> resolvers) {
        this(resolvers,
             new Dependencies(repositories, resolvers),
             null,
             false,
             "scalac",
             "zinc",
             "main",
             null,
             new ProcessBuildStep.Terms());
    }

    public static ScalaCompilerModule ofEnvironment(Environment environment,
                                                    Map<String, Repository> repositories,
                                                    Map<String, Resolver> resolvers) {
        return new ScalaCompilerModule(resolvers,
                Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                environment.flag("scala.incremental", false),
                "scalac",
                "zinc",
                "main",
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "scalac"));
    }

    private ScalaCompilerModule(Map<String, Resolver> resolvers,
                                Dependencies dependencies,
                                Pinning pinning,
                                boolean incremental,
                                String tool,
                                String zinc,
                                String group,
                                Function<List<String>, ? extends ProcessHandler> factory,
                                ProcessBuildStep.Terms terms) {
        this.resolvers = resolvers;
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.incremental = incremental;
        this.tool = tool;
        this.zinc = zinc;
        this.group = group;
        this.factory = factory;
        this.terms = terms;
    }

    public ScalaCompilerModule factory(Function<List<String>, ? extends ProcessHandler> factory) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule pinning(Pinning pinning) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule incremental(boolean incremental) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule tool(String tool) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule zinc(String zinc) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule group(String group) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms);
    }

    public ScalaCompilerModule printing(BiConsumer<Boolean, String> printing) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms.printing(printing));
    }

    public ScalaCompilerModule permits(Permits permits) {
        return new ScalaCompilerModule(resolvers, dependencies, pinning, incremental, tool, zinc, group, factory, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        SequencedSet<String> upstream = inherited.sequencedKeySet();
        buildExecutor.addStep(REQUIRED, new Requires(Set.copyOf(resolvers.keySet()), tool, zinc, incremental), upstream);
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(upstream);
        buildExecutor.addModule(RESOLUTION,
                dependencies.pinning(pinning).group(tool),
                resolveInputs);
        SequencedSet<String> compileInputs = new LinkedHashSet<>();
        compileInputs.add(RESOLUTION);
        if (incremental) {
            buildExecutor.addModule(ZINC_RESOLUTION,
                    dependencies.pinning(pinning).group(zinc),
                    resolveInputs);
            compileInputs.add(ZINC_RESOLUTION);
        }
        compileInputs.addAll(upstream);
        buildExecutor.addStep(COMPILED_CLASSES,
                factory == null
                        ? new Compile(terms, incremental, tool, zinc, group)
                        : new Compile(terms, incremental, tool, zinc, group, factory),
                compileInputs);
    }

    @Override
    public Optional<String> resolve(String path) {
        if (path.equals(COMPILED_CLASSES)) {
            return Optional.of(COMPILED_CLASSES);
        }
        if (path.equals(RESOLUTION)) {
            return Optional.of(RESOLVED_ARTIFACTS);
        }
        if (path.equals(ZINC_RESOLUTION)) {
            return Optional.of(ZINC_RESOLUTION);
        }
        return Optional.empty();
    }

    private record Requires(Set<String> prefixes, String tool, String zinc, boolean incremental) implements BuildStep {

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
            return false;
        }

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
                        "No suitable resolver for Scala compiler. Available prefixes: " + prefixes
                                + ". Expected one of: " + PREFERRED_PREFIXES);
            }
            String coordinate = switch (selectedPrefix) {
                case "module" -> selectedPrefix + "/" + MODULE_NAME;
                case "maven" -> selectedPrefix + "/" + MAVEN_GROUP + "/" + MAVEN_ARTIFACT + "/RELEASE";
                default -> throw new IllegalStateException("Unreachable");
            };
            SequencedProperties requires = new SequencedProperties();
            requires.setProperty(tool + "/runtime/" + coordinate, "");
            if (incremental) {
                if (!prefixes.contains("maven")) {
                    throw new IllegalStateException("jenesis.scala.incremental resolves Zinc and the Scala"
                            + " compiler's bridge to it from a Maven repository, but only " + prefixes
                            + " resolve here: name a Maven repository, or set jenesis.scala.incremental=false");
                }
                requires.setProperty(tool + "/runtime/maven/" + MAVEN_GROUP + "/" + BRIDGE_ARTIFACT + "/RELEASE", "");
                requires.setProperty(zinc + "/runtime/maven/" + ZINC_GROUP + "/" + ZINC_ARTIFACT + "/RELEASE", "");
            }
            requires.store(context.next().resolve(REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Compile extends ProcessBuildStep {

        private final boolean incremental;
        private final String tool;
        private final String zinc;
        private final String group;

        private Compile(ProcessBuildStep.Terms terms,
                        boolean incremental,
                        String tool,
                        String zinc,
                        String group) {
            this(terms, incremental, tool, zinc, group, ProcessHandler.OfProcess.ofJavaHome("bin/java"));
        }

        private Compile(ProcessBuildStep.Terms terms,
                        boolean incremental,
                        String tool,
                        String zinc,
                        String group,
                        Function<List<String>, ? extends ProcessHandler> factory) {
            super("scalac", factory, terms);
            this.incremental = incremental;
            this.tool = tool;
            this.zinc = zinc;
            this.group = group;
        }

        @Override
        protected ProcessHandler handler(BuildStepContext context, List<String> commands) throws IOException {
            if (!incremental) {
                return super.handler(context, commands);
            }
            ProcessHandler.OfProcess handler = ProcessHandler.OfProcess.ofJavaMain().apply(commands).shared(true);
            return handler.daemon(terms.daemon()).aot(terms.aot());
        }

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            if (!incremental) {
                return super.apply(executor, context, arguments);
            }
            return super.apply(executor, context, arguments).thenComposeAsync(result -> {
                Path compiled = context.persistent().resolve(CLASSES), target = context.next().resolve(CLASSES);
                if (!result.next() || !Files.isDirectory(compiled)) {
                    return CompletableFuture.completedStage(result);
                }
                try {
                    Files.walkFileTree(compiled, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.getFileName().toString().endsWith(".class") && ClassFile.of()
                                    .parse(file)
                                    .findAttribute(Attributes.sourceFile())
                                    .map(attribute -> attribute.sourceFile().stringValue().endsWith(".java"))
                                    .orElse(false)) {
                                return FileVisitResult.CONTINUE;
                            }
                            Path published = target.resolve(compiled.relativize(file));
                            Files.createDirectories(published.getParent());
                            Files.copy(file, published, StandardCopyOption.COPY_ATTRIBUTES);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
                return CompletableFuture.completedStage(result);
            }, executor);
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            return Javac.hasRelevantChange(arguments,
                    Set.of(".scala", ".java"),
                    Set.of("scalac", "javac"),
                    false);
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            Path target = Files.createDirectory(context.next().resolve(CLASSES));
            List<String> files = new ArrayList<>(),
                    jars = new ArrayList<>(),
                    classpath = new ArrayList<>(),
                    plugins = new ArrayList<>();
            String release = null;
            Path sourceroot = null;
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "runtime")) {
                    jars.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), tool, "plugin")) {
                    plugins.add(jar.toString());
                }
                for (Path jar : Dependencies.select(argument.folder(), group, "compile")) {
                    classpath.add(jar.toString());
                }
                Path javacProperties = ProcessBuildStep.locate(argument.folder(), ProcessBuildStep.PROCESS, "javac");
                if (javacProperties != null) {
                    String value = SequencedProperties.ofFiles(javacProperties).value("--release");
                    if (value != null) {
                        release = value;
                    }
                }
                Path classes = argument.folder().resolve(CLASSES);
                if (Files.exists(classes)) {
                    classpath.add(classes.toString());
                }
                Path sources = argument.folder().resolve(Bind.SOURCES);
                if (Files.exists(sources)) {
                    if (sourceroot == null) {
                        sourceroot = sources;
                    }
                    Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            return BuildStep.underBuildJenesis(sources.relativize(dir))
                                    ? FileVisitResult.SKIP_SUBTREE
                                    : FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String name = file.toString();
                            if (name.endsWith(".scala")
                                    || (name.endsWith(".java")
                                    && !file.getFileName().toString().equals("module-info.java"))) {
                                files.add(name);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            files.sort(null);
            if (files.stream().noneMatch(name -> name.endsWith(".scala"))) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException(
                        "No compiler jars resolved upstream of the Scala compile step");
            }
            for (List<String> entries : List.of(jars, classpath, plugins)) {
                for (String entry : entries) {
                    if (entry.indexOf(File.pathSeparatorChar) != -1) {
                        throw new IllegalArgumentException(
                                "Path entry contains separator '" + File.pathSeparator + "': " + entry);
                    }
                }
            }
            List<String> userClasspath = new ArrayList<>(jars);
            userClasspath.addAll(classpath);
            if (!incremental) {
                List<String> commands = new ArrayList<>(List.of(
                        "-cp", String.join(File.pathSeparator, jars),
                        "dotty.tools.dotc.Main",
                        "-d", target.toString(),
                        "-classpath", String.join(File.pathSeparator, userClasspath),
                        "-sourceroot", sourceroot.toString()));
                if (release != null) {
                    commands.add("-release");
                    commands.add(release);
                }
                for (String plugin : plugins) {
                    commands.add("-Xplugin:" + plugin);
                }
                commands.addAll(files);
                return CompletableFuture.completedStage(commands);
            }
            List<String> engine = new ArrayList<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (!argument.removed()) {
                    for (Path jar : Dependencies.select(argument.folder(), zinc, "runtime")) {
                        engine.add(jar.toString());
                    }
                }
            }
            if (engine.isEmpty()) {
                throw new IllegalStateException("No Zinc jars resolved upstream of the Scala compile step");
            }
            CodeSource source = IncrementalScalaCompiler.class.getProtectionDomain().getCodeSource();
            if (source == null) {
                throw new IllegalStateException("jenesis.scala.incremental forks Zinc with the plugin's own"
                        + " classes, but the plugin was loaded from no location it can hand a JVM: set"
                        + " jenesis.scala.incremental=false");
            }
            String plugin;
            try {
                plugin = Path.of(source.getLocation().toURI()).toString();
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
            List<String> commands = new ArrayList<>(List.of(
                    "-cp", plugin + File.pathSeparator + String.join(File.pathSeparator, engine),
                    IncrementalScalaCompiler.class.getName()));
            for (String jar : jars) {
                commands.add(IncrementalScalaCompiler.COMPILER);
                commands.add(jar);
            }
            for (String entry : userClasspath) {
                commands.add(IncrementalScalaCompiler.CLASSPATH);
                commands.add(entry);
            }
            List<String> scalac = new ArrayList<>(List.of("-sourceroot", sourceroot.toString())),
                    javac = new ArrayList<>(List.of("-proc:none", "-nowarn"));
            if (release != null) {
                scalac.addAll(List.of("-release", release));
                javac.addAll(List.of("--release", release));
            }
            for (String compilerPlugin : plugins) {
                scalac.add("-Xplugin:" + compilerPlugin);
            }
            scalac.forEach(option -> commands.addAll(List.of(IncrementalScalaCompiler.SCALAC, option)));
            javac.forEach(option -> commands.addAll(List.of(IncrementalScalaCompiler.JAVAC, option)));
            Path state = context.persistent();
            SequencedProperties configuration = new SequencedProperties();
            configuration.setProperty("configuration", String.join("\n", jars) + "\n" + String.join("\n", engine)
                    + "\n" + String.join("\n", scalac) + "\n" + String.join("\n", javac));
            configuration.setProperty("state", state.toAbsolutePath().toString());
            if (context.previous() == null
                    || !Files.isRegularFile(state.resolve(STATE))
                    || !SequencedProperties.ofFiles(state.resolve(STATE)).equals(configuration)) {
                try (Stream<Path> stale = Files.walk(state)) {
                    for (Path file : stale.filter(file -> !file.equals(state)).sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(file);
                    }
                }
            }
            configuration.store(state.resolve(STATE));
            commands.addAll(List.of(
                    IncrementalScalaCompiler.ANALYSIS, state.resolve("analysis.bin").toString(),
                    IncrementalScalaCompiler.CLASSES, state.resolve(CLASSES).toString(),
                    IncrementalScalaCompiler.SOURCES));
            commands.addAll(files);
            return CompletableFuture.completedStage(commands);
        }
    }
}
