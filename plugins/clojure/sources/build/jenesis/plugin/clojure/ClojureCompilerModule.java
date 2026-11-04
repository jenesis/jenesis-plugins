package build.jenesis.plugin.clojure;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;
import build.jenesis.Environment;
import build.jenesis.Permits;
import build.jenesis.step.Dependencies;
import build.jenesis.step.Javac;
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;
import build.jenesis.step.Versions;

public class ClojureCompilerModule implements BuildExecutorModule {

    public static final String COMPILED = "compiled", VERSIONED = "classes";
    public static final List<String> EXTENSIONS = List.of(".clj", ".cljc");
    private static final String COMPILER = "clojure/lang/Compile.class";
    private static final Set<String> READERS = Set.of("data_readers.clj", "data_readers.cljc");

    private final String group;
    private final boolean aot;
    private final Function<List<String>, ? extends ProcessHandler> factory;
    private final ProcessBuildStep.Terms terms;

    public ClojureCompilerModule() {
        this("main", true, null, new ProcessBuildStep.Terms());
    }

    public static ClojureCompilerModule ofEnvironment(Environment environment) {
        return new ClojureCompilerModule("main", true, null, ProcessBuildStep.Terms.ofEnvironment(environment, "clojure"));
    }

    private ClojureCompilerModule(String group,
                                  boolean aot,
                                  Function<List<String>, ? extends ProcessHandler> factory,
                                  ProcessBuildStep.Terms terms) {
        this.group = group;
        this.aot = aot;
        this.factory = factory;
        this.terms = terms;
    }

    public ClojureCompilerModule group(String group) {
        return new ClojureCompilerModule(group, aot, factory, terms);
    }

    public ClojureCompilerModule aot(boolean aot) {
        return new ClojureCompilerModule(group, aot, factory, terms);
    }

    public ClojureCompilerModule factory(Function<List<String>, ? extends ProcessHandler> factory) {
        return new ClojureCompilerModule(group, aot, factory, terms);
    }

    public ClojureCompilerModule printing(BiConsumer<Boolean, String> printing) {
        return new ClojureCompilerModule(group, aot, factory, terms.printing(printing));
    }

    public ClojureCompilerModule permits(Permits permits) {
        return new ClojureCompilerModule(group, aot, factory, terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(COMPILED,
                new Compile(terms,
                        group,
                        aot,
                        factory == null ? ProcessHandler.OfProcess.ofJavaHome("bin/java") : factory),
                inherited.sequencedKeySet());
        buildExecutor.addStep(VERSIONED, new Versions(), COMPILED);
    }

    @Override
    public Optional<String> resolve(String path) {
        return path.equals(VERSIONED) ? Optional.of(VERSIONED) : Optional.empty();
    }

    public static String namespace(String path) {
        String name = path.replace(File.separatorChar, '/');
        for (String extension : EXTENSIONS) {
            if (name.endsWith(extension)) {
                return name.substring(0, name.length() - extension.length()).replace('/', '.').replace('_', '-');
            }
        }
        throw new IllegalArgumentException("Not a Clojure source: " + path + " - expected a name ending in "
                + String.join(" or ", EXTENSIONS));
    }

    public static boolean defines(String type, Collection<String> namespaces) {
        for (String namespace : namespaces) {
            String prefix = namespace.replace('-', '_').replace('.', '/');
            if (type.equals(prefix)
                    || type.startsWith(prefix + "$")
                    || type.startsWith(prefix + "__")
                    || type.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    private static class Compile extends ProcessBuildStep {

        private final String group;
        private final boolean aot;

        private Compile(ProcessBuildStep.Terms terms,
                        String group,
                        boolean aot,
                        Function<List<String>, ? extends ProcessHandler> factory) {
            super("clojure", factory, terms);
            this.group = group;
            this.aot = aot;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            return Javac.hasRelevantChange(arguments,
                    Set.of(".clj", ".cljc", ".java"),
                    Set.of("clojure", "javac"),
                    false);
        }

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            return super.apply(executor, context, arguments).thenCompose(result -> {
                Path target = context.next().resolve(CLASSES);
                if (!result.next() || !aot || !Files.isDirectory(target)) {
                    return CompletableFuture.completedStage(result);
                }
                try {
                    SequencedSet<String> owned = new TreeSet<>(), foreign = new TreeSet<>();
                    try (Stream<Path> files = Files.walk(target)) {
                        for (Path file : files.filter(Files::isRegularFile).toList()) {
                            String name = target.relativize(file).toString();
                            if (EXTENSIONS.stream().anyMatch(name::endsWith) && !READERS.contains(name)) {
                                owned.add(namespace(name));
                            }
                        }
                    }
                    for (Path jar : classpath(arguments)) {
                        if (!Files.isRegularFile(jar)) {
                            continue;
                        }
                        try (JarFile file = new JarFile(jar.toFile())) {
                            for (JarEntry entry : Collections.list(file.entries())) {
                                if (EXTENSIONS.stream().anyMatch(entry.getName()::endsWith)
                                        && !READERS.contains(entry.getName())) {
                                    foreign.add(namespace(entry.getName()));
                                }
                            }
                        }
                    }
                    List<Path> files;
                    try (Stream<Path> walk = Files.walk(target)) {
                        files = walk.sorted(Comparator.reverseOrder()).toList();
                    }
                    for (Path file : files) {
                        if (Files.isDirectory(file)) {
                            if (!file.equals(target)) {
                                try (Stream<Path> children = Files.list(file)) {
                                    if (children.findAny().isEmpty()) {
                                        Files.delete(file);
                                    }
                                }
                            }
                            continue;
                        }
                        String name = target.relativize(file).toString().replace(File.separatorChar, '/');
                        if (name.endsWith(".class")) {
                            String type = name.substring(0, name.length() - ".class".length());
                            if (defines(type, foreign) && !defines(type, owned)) {
                                Files.delete(file);
                            }
                        }
                    }
                } catch (IOException e) {
                    return CompletableFuture.failedStage(e);
                }
                return CompletableFuture.completedStage(result);
            });
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            Path target = Files.createDirectory(context.next().resolve(CLASSES));
            SequencedSet<String> namespaces = new TreeSet<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path sources = argument.folder().resolve(SOURCES);
                if (!Files.exists(sources)) {
                    continue;
                }
                Files.walkFileTree(sources, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return BuildStep.underBuildJenesis(sources.relativize(dir))
                                ? FileVisitResult.SKIP_SUBTREE
                                : FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String name = sources.relativize(file).toString();
                        if (EXTENSIONS.stream().anyMatch(name::endsWith)) {
                            Path copy = target.resolve(name);
                            Files.createDirectories(copy.getParent());
                            Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
                            if (!READERS.contains(name)) {
                                namespaces.add(namespace(name));
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            if (!aot || namespaces.isEmpty()) {
                return CompletableFuture.completedStage(null);
            }
            List<String> classpath = new ArrayList<>(List.of(target.toString()));
            boolean compiler = false;
            for (Path entry : classpath(arguments)) {
                classpath.add(entry.toString());
                if (!compiler && Files.isRegularFile(entry)) {
                    try (JarFile jar = new JarFile(entry.toFile())) {
                        compiler = jar.getEntry(COMPILER) != null;
                    }
                }
            }
            if (!compiler) {
                throw new IllegalStateException("The module holds Clojure namespaces " + namespaces + " but no"
                        + " Clojure on its compile class path: declare a dependency on org.clojure:clojure, which"
                        + " compiles them and runs them, or set aot=false in clojure.properties to ship the"
                        + " sources alone");
            }
            for (String entry : classpath) {
                if (entry.indexOf(File.pathSeparatorChar) != -1) {
                    throw new IllegalArgumentException("Path entry contains separator '" + File.pathSeparator + "': "
                            + entry);
                }
            }
            List<String> commands = new ArrayList<>(List.of(
                    "-Dclojure.compile.path=" + target,
                    "-cp",
                    String.join(File.pathSeparator, classpath),
                    "clojure.lang.Compile"));
            commands.addAll(namespaces);
            return CompletableFuture.completedStage(commands);
        }

        private List<Path> classpath(SequencedMap<String, BuildStepArgument> arguments) throws IOException {
            List<Path> classpath = new ArrayList<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path classes = argument.folder().resolve(CLASSES);
                if (Files.exists(classes)) {
                    classpath.add(classes);
                }
                classpath.addAll(Dependencies.select(argument.folder(), group, "compile"));
            }
            return classpath;
        }
    }
}
