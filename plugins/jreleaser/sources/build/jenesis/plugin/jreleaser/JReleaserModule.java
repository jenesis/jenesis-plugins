package build.jenesis.plugin.jreleaser;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;
import build.jenesis.Environment;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.project.ProjectPlugins;
import build.jenesis.step.Dependencies;

public class JReleaserModule implements BuildExecutorModule {

    public static final String RELEASE = "release", VERSION = "JRELEASER_PROJECT_VERSION";
    public static final List<String> CONFIGURATIONS = List.of(
            "jreleaser.yml", "jreleaser.yaml", "jreleaser.toml", "jreleaser.json");
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies", SETTINGS = "settings.properties";
    private static final String MAVEN_GROUP = "org.jreleaser", MAVEN_ARTIFACT = "jreleaser-tool-provider",
            NAME = "jreleaser", PREFIX = "jenesis";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String command;
    private final boolean publish;
    private final SequencedMap<String, String> settings;
    private final Semaphore permits;
    private final Consumer<String> out;
    private final Consumer<String> err;

    public JReleaserModule(Map<String, Repository> repositories,
                           Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
                null,
                NAME,
                "full-release",
                false,
                Collections.emptyNavigableMap(),
                null,
                null,
                null);
    }

    public static JReleaserModule ofEnvironment(Environment environment,
                                                Map<String, Repository> repositories,
                                                Map<String, Resolver> resolvers) {
        return new JReleaserModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                NAME,
                "full-release",
                environment.flag("release.publish"),
                Collections.emptyNavigableMap(),
                null,
                environment.out(),
                environment.err());
    }

    private JReleaserModule(Dependencies dependencies,
                            Pinning pinning,
                            String tool,
                            String command,
                            boolean publish,
                            SequencedMap<String, String> settings,
                            Semaphore permits,
                            Consumer<String> out,
                            Consumer<String> err) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.command = command;
        this.publish = publish;
        this.settings = settings;
        this.permits = permits;
        this.out = out;
        this.err = err;
    }

    public JReleaserModule pinning(Pinning pinning) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    public JReleaserModule tool(String tool) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    public JReleaserModule command(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("JReleaser runs no command '" + command + "' - name one of its"
                    + " commands, such as full-release, release, deploy or config");
        }
        return new JReleaserModule(dependencies, pinning, tool, command.strip(), publish, settings, permits, out, err);
    }

    public JReleaserModule publish(boolean publish) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    public JReleaserModule settings(SequencedMap<String, String> settings) {
        for (String key : settings.sequencedKeySet()) {
            if (!key.startsWith("JRELEASER_")) {
                throw new IllegalArgumentException("JReleaser takes no setting " + key + " - name one of its"
                        + " JRELEASER_* settings");
            }
        }
        return new JReleaserModule(dependencies, pinning, tool, command, publish,
                Collections.unmodifiableSequencedMap(new LinkedHashMap<>(settings)), permits, out, err);
    }

    public JReleaserModule permits(Semaphore permits) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    public JReleaserModule out(Consumer<String> out) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    public JReleaserModule err(Consumer<String> err) {
        return new JReleaserModule(dependencies, pinning, tool, command, publish, settings, permits, out, err);
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(REQUIRED, new Required(tool), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES, dependencies.pinning(pinning).group(tool), resolveInputs);
        SequencedSet<String> releaseInputs = new LinkedHashSet<>();
        releaseInputs.add(DEPENDENCIES);
        releaseInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(RELEASE,
                new Release(tool, command, publish, new LinkedHashMap<>(settings), permits, out, err),
                releaseInputs);
    }

    private record Required(String tool) implements BuildStep {

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

    private static class Release implements BuildStep {

        private final String tool;
        private final String command;
        private final boolean publish;
        private final LinkedHashMap<String, String> settings;
        private final transient Semaphore permits;
        private final transient Consumer<String> out;
        private final transient Consumer<String> err;

        private Release(String tool,
                        String command,
                        boolean publish,
                        LinkedHashMap<String, String> settings,
                        Semaphore permits,
                        Consumer<String> out,
                        Consumer<String> err) {
            this.tool = tool;
            this.command = command;
            this.publish = publish;
            this.settings = settings;
            this.permits = permits;
            this.out = out;
            this.err = err;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
            return true;
        }

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            List<Path> jars = new ArrayList<>();
            Path configuration = null;
            SequencedProperties root = null, metadata = new SequencedProperties();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                jars.addAll(Dependencies.select(argument.folder(), tool, "runtime"));
                Path located = argument.folder().resolve(ProjectPlugins.ROOT);
                if (Files.isRegularFile(located)) {
                    root = SequencedProperties.ofFiles(located);
                    Path resolved = argument.folder().resolve(BuildStep.METADATA);
                    if (Files.isRegularFile(resolved)) {
                        metadata = SequencedProperties.ofFiles(resolved);
                    }
                }
                for (String name : CONFIGURATIONS) {
                    Path candidate = argument.folder().resolve(name);
                    if (!Files.isRegularFile(candidate)) {
                        continue;
                    }
                    if (configuration != null) {
                        throw new IllegalArgumentException("JReleaser is handed both " + configuration + " and "
                                + candidate + " - bind one configuration");
                    }
                    configuration = candidate;
                }
            }
            if (root == null || root.value(ProjectPlugins.ROOT_KEY) == null) {
                throw new IllegalStateException("The JReleaser step is handed no " + ProjectPlugins.ROOT
                        + " - name the plugin at the hook point release, which hands its plugins the project's root");
            }
            if (configuration == null) {
                throw new IllegalStateException("The JReleaser step is handed no configuration - bind one of "
                        + CONFIGURATIONS + " in plugin-<name>.properties, as @jreleaser=jreleaser.yml");
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No JReleaser jar resolved upstream of the JReleaser step");
            }
            List<String> commands = new ArrayList<>(List.of(command,
                    "--basedir", Path.of(root.value(ProjectPlugins.ROOT_KEY)).toString(),
                    "--config-file", configuration.toAbsolutePath().toString(),
                    "--output-directory", context.next().toAbsolutePath().toString()));
            if (!publish) {
                commands.add("--dry-run");
            }
            SequencedMap<String, String> properties = new TreeMap<>();
            metadata.forEachProperty((key, value) -> properties.put(template(key), value));
            root.forEachProperty((key, value) -> {
                if (!key.equals(ProjectPlugins.ROOT_KEY)) {
                    properties.put(template(key), value);
                }
            });
            properties.forEach((key, value) -> {
                commands.add("--set-property");
                commands.add(key + "=" + value);
            });
            Set<Integer> lengths = properties.sequencedKeySet().stream().map(String::length).collect(Collectors.toSet());
            for (int count = properties.size(); lengths.contains(count - 1); count++) {
                commands.add("--set-property");
                commands.add(PREFIX);
            }
            SequencedProperties settled = new SequencedProperties();
            String version = metadata.value("version");
            if (version != null) {
                settled.setProperty(VERSION, version);
            }
            settings.forEach(settled::setProperty);
            Path file = context.supplement().resolve(SETTINGS);
            if (!settled.isEmpty()) {
                settled.store(file);
                commands.add("--settings-file");
                commands.add(file.toAbsolutePath().toString());
            }
            int code;
            try (URLClassLoader loader = new URLClassLoader(jars.stream().map(jar -> {
                try {
                    return jar.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Cannot load " + jar, e);
                }
            }).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
                 PrintWriter output = new PrintWriter(new Lines(out), true);
                 PrintWriter error = new PrintWriter(new Lines(err), true)) {
                ToolProvider provider = ServiceLoader.load(ToolProvider.class, loader).stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(candidate -> candidate.name().equals(NAME) && candidate.getClass().getClassLoader() == loader)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No tool named " + NAME + " among " + jars
                                + " - resolve " + MAVEN_GROUP + "/" + MAVEN_ARTIFACT));
                Thread thread = Thread.currentThread();
                ClassLoader previous = thread.getContextClassLoader();
                if (permits != null) {
                    permits.acquireUninterruptibly();
                }
                thread.setContextClassLoader(loader);
                try {
                    code = provider.run(output, error, commands.toArray(String[]::new));
                } finally {
                    thread.setContextClassLoader(previous);
                    if (permits != null) {
                        permits.release();
                    }
                }
            } finally {
                Files.deleteIfExists(file);
            }
            if (code != 0) {
                throw new IllegalStateException("JReleaser " + command + " failed with exit code " + code
                        + " - its log is " + NAME + "/trace.log in the output of the step");
            }
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }

        private static String template(String key) {
            StringBuilder name = new StringBuilder(PREFIX);
            for (String part : key.split("[^A-Za-z0-9]+")) {
                if (!part.isEmpty()) {
                    name.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
                }
            }
            return name.toString();
        }
    }

    private static class Lines extends Writer {

        private final Consumer<String> consumer;
        private final StringBuilder line = new StringBuilder();

        private Lines(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void write(char[] buffer, int offset, int length) {
            for (int index = offset; index < offset + length; index++) {
                char character = buffer[index];
                if (character == '\n') {
                    emit();
                } else if (character != '\r') {
                    line.append(character);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            if (!line.isEmpty()) {
                emit();
            }
        }

        private void emit() {
            if (consumer != null) {
                consumer.accept(line.toString());
            }
            line.setLength(0);
        }
    }
}
