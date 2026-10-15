package build.jenesis.plugin.ksp;

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
import build.jenesis.step.ProcessBuildStep;
import build.jenesis.step.ProcessHandler;

public class KspModule implements BuildExecutorModule {

    public static final String GENERATE = "generate";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies", PROCESSORS = "processors",
            INCREMENTAL = "incremental", STATE = "state.properties", LISTING = "listing.properties",
            CLASSES = "classes.properties";
    private static final String MAVEN_GROUP = "com.google.devtools.ksp", MAVEN_ARTIFACT = "symbol-processing-aa-embeddable";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String processors;
    private final String group;
    private final SequencedMap<String, String> options;
    private final ProcessBuildStep.Terms terms;

    public KspModule(Map<String, Repository> repositories,
                     Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "ksp:tool",
             "ksp:processor",
             "main",
             Collections.emptyNavigableMap(),
             new ProcessBuildStep.Terms());
    }

    public static KspModule ofEnvironment(Environment environment,
                                          Map<String, Repository> repositories,
                                          Map<String, Resolver> resolvers) {
        return new KspModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "ksp:tool",
                "ksp:processor",
                "main",
                Collections.emptyNavigableMap(),
                ProcessBuildStep.Terms.ofEnvironment(environment, "ksp"));
    }

    private KspModule(Dependencies dependencies,
                      Pinning pinning,
                      String tool,
                      String processors,
                      String group,
                      SequencedMap<String, String> options,
                      ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.processors = processors;
        this.group = group;
        this.options = options;
        this.terms = terms;
    }

    public KspModule pinning(Pinning pinning) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms);
    }

    public KspModule tool(String tool) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms);
    }

    public KspModule processors(String processors) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms);
    }

    public KspModule group(String group) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms);
    }

    public KspModule options(SequencedMap<String, String> options) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms);
    }

    public KspModule printing(BiConsumer<Boolean, String> printing) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms.printing(printing));
    }

    public KspModule permits(Permits permits) {
        return new KspModule(dependencies, pinning, tool, processors, group, options, terms.permits(permits));
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
        buildExecutor.addModule(PROCESSORS,
                dependencies.pinning(pinning).group(processors),
                inherited.sequencedKeySet());
        SequencedSet<String> generateInputs = new LinkedHashSet<>();
        generateInputs.add(DEPENDENCIES);
        generateInputs.add(PROCESSORS);
        generateInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(GENERATE,
                new Generate(terms, tool, processors, group, new TreeMap<>(options)),
                generateInputs);
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

    private static class Generate extends ProcessBuildStep {

        private final String tool;
        private final String processors;
        private final String group;
        private final TreeMap<String, String> options;

        private Generate(ProcessBuildStep.Terms terms,
                         String tool,
                         String processors,
                         String group,
                         TreeMap<String, String> options) {
            super("ksp", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.processors = processors;
            this.group = group;
            this.options = options;
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            SequencedSet<String> jars = new LinkedHashSet<>(),
                    processorPath = new LinkedHashSet<>(),
                    libraries = new LinkedHashSet<>(),
                    roots = new LinkedHashSet<>();
            String release = null, module = null;
            boolean sources = false;
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path folder = argument.folder();
                Dependencies.select(folder, tool, "runtime").forEach(jar -> jars.add(jar.toString()));
                Dependencies.select(folder, processors, "plugin").forEach(jar -> processorPath.add(jar.toString()));
                Dependencies.select(folder, group, "compile").forEach(jar -> libraries.add(jar.toString()));
                Path javac = ProcessBuildStep.locate(folder, ProcessBuildStep.PROCESS, "javac");
                if (javac != null) {
                    String value = SequencedProperties.ofFiles(javac).value("--release");
                    if (value != null) {
                        release = value;
                    }
                }
                Path described = folder.resolve(BuildStep.MODULE);
                if (Files.isRegularFile(described)) {
                    module = SequencedProperties.ofFiles(described).value("module", module);
                }
                Path metadata = folder.resolve(BuildStep.METADATA);
                if (module == null && Files.isRegularFile(metadata)) {
                    module = SequencedProperties.ofFiles(metadata).value("artifact");
                }
                Path root = folder.resolve(BuildStep.SOURCES).toAbsolutePath();
                if (Files.isDirectory(root)) {
                    roots.add(root.toString());
                    try (Stream<Path> files = Files.walk(root)) {
                        sources |= files.anyMatch(file -> file.toString().endsWith(".kt")
                                || file.toString().endsWith(".java"));
                    }
                }
            }
            if (!sources) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No KSP jars resolved upstream of the KSP step");
            }
            if (processorPath.isEmpty()) {
                throw new IllegalStateException("KSP runs over the sources of the module, but no symbol processor is"
                        + " declared: name one with @jenesis.plugin " + processors + " <coordinate> in"
                        + " module-info.java, or <!--jenesis.plugin " + processors + " maven/<groupId>/<artifactId>-->"
                        + " in pom.xml");
            }
            ForkedOptions forked = forked(properties);
            List<String> commands = new ArrayList<>(forked.jvm()), declared = forked.program();
            Path next = context.next().toAbsolutePath(),
                    supplement = context.supplement().toAbsolutePath(),
                    working = Files.createDirectories(next.resolve(INCREMENTAL)),
                    generated = Files.createDirectories(next.resolve(BuildStep.SOURCES)),
                    resources = Files.createDirectories(next.resolve(BuildStep.RESOURCES));
            String sourceRoots = String.join(File.pathSeparator, roots);
            SequencedProperties state = new SequencedProperties(), listing = new SequencedProperties(), classes = new SequencedProperties();
            state.setProperty("configuration", String.join("\n", jars) + "\n" + String.join("\n", processorPath)
                    + "\n" + options + "\n" + release + "\n" + module + "\n" + declared + "\n" + sourceRoots);
            state.setProperty("destination", next.toString());
            for (String root : roots) {
                try (Stream<Path> files = Files.walk(Path.of(root))) {
                    for (Path file : files.filter(file -> file.toString().endsWith(".kt") || file.toString().endsWith(".java")).sorted().toList()) {
                        listing.setProperty(file.toAbsolutePath().toString(), digest(Files.readAllBytes(file)));
                    }
                }
            }
            for (String library : libraries) {
                Path path = Path.of(library);
                if (Files.isDirectory(path)) {
                    try (Stream<Path> files = Files.walk(path)) {
                        for (Path file : files.filter(file -> file.toString().endsWith(".class")).sorted().toList()) {
                            CRC32 crc = new CRC32();
                            crc.update(Files.readAllBytes(file));
                            classes.putIfAbsent(className(path.relativize(file).toString()), Long.toHexString(crc.getValue()));
                        }
                    }
                } else if (Files.isRegularFile(path)) {
                    try (JarFile jar = new JarFile(path.toFile())) {
                        jar.stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry ->
                                classes.putIfAbsent(className(entry.getName()), Long.toHexString(entry.getCrc())));
                    }
                }
            }
            Path previous = context.previous() == null ? null : context.previous().resolve(INCREMENTAL);
            boolean incremental = previous != null
                    && Files.isRegularFile(previous.resolve(STATE))
                    && Files.isRegularFile(previous.resolve(LISTING))
                    && Files.isRegularFile(previous.resolve(CLASSES))
                    && SequencedProperties.ofFiles(previous.resolve(STATE)).equals(state);
            List<String> modified = new ArrayList<>(), removed = new ArrayList<>(), changed = new ArrayList<>();
            if (incremental) {
                Path caches = previous.resolve("caches");
                if (Files.isDirectory(caches)) {
                    try (Stream<Path> files = Files.walk(caches)) {
                        for (Path file : files.toList()) {
                            Path copied = working.resolve("caches").resolve(caches.relativize(file).toString());
                            if (Files.isDirectory(file)) {
                                Files.createDirectories(copied);
                            } else {
                                Files.copy(file, copied, StandardCopyOption.COPY_ATTRIBUTES);
                            }
                        }
                    }
                }
                SequencedProperties recorded = SequencedProperties.ofFiles(previous.resolve(LISTING)),
                        compiled = SequencedProperties.ofFiles(previous.resolve(CLASSES));
                for (String file : listing.stringPropertyNames()) {
                    if (!listing.getProperty(file).equals(recorded.getProperty(file))) {
                        modified.add(file);
                    }
                }
                for (String file : recorded.stringPropertyNames()) {
                    if (listing.getProperty(file) == null) {
                        removed.add(file);
                    }
                }
                for (String name : classes.stringPropertyNames()) {
                    if (!classes.getProperty(name).equals(compiled.getProperty(name))) {
                        changed.add(name);
                    }
                }
                for (String name : compiled.stringPropertyNames()) {
                    if (classes.getProperty(name) == null) {
                        changed.add(name);
                    }
                }
            }
            state.store(working.resolve(STATE));
            listing.store(working.resolve(LISTING));
            classes.store(working.resolve(CLASSES));
            commands.addAll(List.of(
                    "-cp", ProcessHandler.OfProcess.driver(KspDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    KspDriver.class.getName(),
                    "-module-name=" + (module == null ? "main" : module),
                    "-jvm-target=" + (release == null
                            ? Integer.toString(Runtime.version().feature())
                            : Integer.parseInt(release) < 9 ? "1." + release : release),
                    "-source-roots=" + sourceRoots,
                    "-java-source-roots=" + sourceRoots,
                    "-project-base-dir=" + supplement,
                    "-output-base-dir=" + next,
                    "-caches-dir=" + working.resolve("caches"),
                    "-class-output-dir=" + working.resolve("classes"),
                    "-kotlin-output-dir=" + generated,
                    "-java-output-dir=" + generated,
                    "-resource-output-dir=" + resources,
                    "-incremental=true"));
            if (!modified.isEmpty()) {
                commands.add("-modified-sources=" + String.join(File.pathSeparator, modified));
            }
            if (!removed.isEmpty()) {
                commands.add("-removed-sources=" + String.join(File.pathSeparator, removed));
            }
            if (!changed.isEmpty()) {
                commands.add("-changed-classes=" + String.join(File.pathSeparator, changed));
            }
            if (declared.stream().noneMatch(option -> option.startsWith("-language-version"))) {
                String version = kotlinVersion(jars);
                commands.add("-language-version=" + version);
                if (declared.stream().noneMatch(option -> option.startsWith("-api-version"))) {
                    commands.add("-api-version=" + version);
                }
            }
            if (!libraries.isEmpty()) {
                commands.add("-libraries=" + String.join(File.pathSeparator, libraries));
            }
            if (!options.isEmpty()) {
                commands.add("-processor-options=" + options.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(File.pathSeparator)));
            }
            commands.addAll(declared);
            commands.add(String.join(File.pathSeparator, processorPath));
            return CompletableFuture.completedStage(commands);
        }

        private static String className(String entry) {
            return entry.substring(0, entry.length() - ".class".length()).replace('\\', '/').replace('/', '.').replace('$', '.');
        }

        private static String digest(byte[] bytes) {
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        private static String kotlinVersion(Collection<String> jars) throws IOException {
            for (String jar : jars) {
                try (JarFile file = new JarFile(jar)) {
                    Manifest manifest = file.getManifest();
                    if (manifest != null
                            && "kotlin-stdlib".equals(manifest.getMainAttributes().getValue("Implementation-Title"))) {
                        String version = manifest.getMainAttributes().getValue("Kotlin-Version");
                        if (version != null) {
                            return version;
                        }
                    }
                }
            }
            throw new IllegalStateException("The KSP jars hold no kotlin-stdlib to take the Kotlin language version"
                    + " from: name it with -language-version=<version> in process-ksp.properties");
        }
    }
}
