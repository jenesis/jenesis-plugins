package build.jenesis.plugin.mavenplugin;

import module java.base;
import module java.xml;
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

public class MavenPluginModule implements BuildExecutorModule {

    public static final String DESCRIPTOR = "descriptor";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies", POM = "pom.xml";
    private static final String MAVEN = "3.9.11", TOOLS = "3.15.1";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String group;
    private final String maven;
    private final String tools;
    private final String goalPrefix;
    private final ProcessBuildStep.Terms terms;

    public MavenPluginModule(Map<String, Repository> repositories, Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
                null,
                "mavenplugin",
                "main",
                MAVEN,
                TOOLS,
                null,
                new ProcessBuildStep.Terms());
    }

    public static MavenPluginModule ofEnvironment(Environment environment,
                                                  Map<String, Repository> repositories,
                                                  Map<String, Resolver> resolvers) {
        return new MavenPluginModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "mavenplugin",
                "main",
                MAVEN,
                TOOLS,
                null,
                ProcessBuildStep.Terms.ofEnvironment(environment, "mavenplugin"));
    }

    private MavenPluginModule(Dependencies dependencies,
                              Pinning pinning,
                              String tool,
                              String group,
                              String maven,
                              String tools,
                              String goalPrefix,
                              ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.group = group;
        this.maven = maven;
        this.tools = tools;
        this.goalPrefix = goalPrefix;
        this.terms = terms;
    }

    public MavenPluginModule pinning(Pinning pinning) {
        return new MavenPluginModule(dependencies, pinning, tool, group, maven, tools, goalPrefix, terms);
    }

    public MavenPluginModule tool(String tool) {
        return new MavenPluginModule(dependencies, pinning, tool, group, maven, tools, goalPrefix, terms);
    }

    public MavenPluginModule group(String group) {
        return new MavenPluginModule(dependencies, pinning, tool, group, maven, tools, goalPrefix, terms);
    }

    public MavenPluginModule maven(String maven) {
        return new MavenPluginModule(dependencies,
                pinning,
                tool,
                group,
                version("maven", maven),
                tools,
                goalPrefix,
                terms);
    }

    public MavenPluginModule tools(String tools) {
        return new MavenPluginModule(dependencies,
                pinning,
                tool,
                group,
                maven,
                version("tools", tools),
                goalPrefix,
                terms);
    }

    public MavenPluginModule goalPrefix(String goalPrefix) {
        if (goalPrefix != null && !goalPrefix.matches("[a-zA-Z0-9.\\-]+")) {
            throw new IllegalArgumentException("The goal prefix " + goalPrefix + " holds more than letters, digits,"
                    + " . and - - name the prefix a goal is called by, as greet in greet:hello");
        }
        return new MavenPluginModule(dependencies, pinning, tool, group, maven, tools, goalPrefix, terms);
    }

    public MavenPluginModule printing(BiConsumer<Boolean, String> printing) {
        return new MavenPluginModule(dependencies,
                pinning,
                tool,
                group,
                maven,
                tools,
                goalPrefix,
                terms.printing(printing));
    }

    public MavenPluginModule permits(Permits permits) {
        return new MavenPluginModule(dependencies,
                pinning,
                tool,
                group,
                maven,
                tools,
                goalPrefix,
                terms.permits(permits));
    }

    private static String version(String name, String version) {
        if (version == null || !version.matches("[0-9][a-zA-Z0-9.\\-]*")) {
            throw new IllegalArgumentException("The " + name + " version " + version + " is not a version - name a"
                    + " release, as " + (name.equals("maven") ? MAVEN : TOOLS));
        }
        return version;
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(REQUIRED, new Requires(tool, maven, tools), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES, dependencies.pinning(pinning).group(tool), resolveInputs);
        SequencedSet<String> descriptorInputs = new LinkedHashSet<>();
        descriptorInputs.add(DEPENDENCIES);
        descriptorInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(DESCRIPTOR, new Descriptor(terms, tool, group, goalPrefix), descriptorInputs);
    }

    private record Requires(String tool, String maven, String tools) implements BuildStep {

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
            requires.setProperty(tool + "/runtime/maven/org.apache.maven/maven-core/" + maven, "");
            requires.setProperty(tool + "/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-annotations/"
                    + tools, "");
            requires.setProperty(tool + "/runtime/maven/org.apache.maven.plugin-tools/maven-plugin-tools-generators/"
                    + tools, "");
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Descriptor extends ProcessBuildStep {

        private final String tool;
        private final String group;
        private final String goalPrefix;

        private Descriptor(ProcessBuildStep.Terms terms, String tool, String group, String goalPrefix) {
            super("mavenplugin", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.group = group;
            this.goalPrefix = goalPrefix;
        }

        @Override
        public CompletionStage<List<String>> process(Executor executor,
                                                     BuildStepContext context,
                                                     SequencedMap<String, BuildStepArgument> arguments,
                                                     SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            List<String> jars = new ArrayList<>(), sources = new ArrayList<>();
            Path classes = null, pom = null;
            SequencedMap<String, String> linked = new LinkedHashMap<>();
            Set<String> runtime = new HashSet<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (argument.removed()) {
                    continue;
                }
                Path folder = argument.folder();
                for (Path jar : Dependencies.select(folder, tool, "runtime")) {
                    jars.add(jar.toString());
                }
                if (classes == null && Files.isDirectory(folder.resolve(BuildStep.CLASSES))) {
                    classes = folder.resolve(BuildStep.CLASSES);
                }
                if (Files.isDirectory(folder.resolve(BuildStep.SOURCES))) {
                    sources.add(folder.resolve(BuildStep.SOURCES).toString());
                }
                if (pom == null && Files.isRegularFile(folder.resolve(POM))) {
                    pom = folder.resolve(POM);
                }
                Path resolution = folder.resolve(BuildStep.DEPENDENCIES);
                if (Files.isRegularFile(resolution)) {
                    SequencedProperties resolved = SequencedProperties.ofFiles(resolution);
                    for (String key : resolved.stringPropertyNames()) {
                        String[] segments = key.split("/");
                        if (segments.length == 6 && segments[0].equals(group) && segments[2].equals("maven")) {
                            String coordinate = segments[3] + ":" + segments[4] + ":" + segments[5];
                            linked.putIfAbsent(coordinate, folder.resolve(resolved.getProperty(key)).toString());
                            if (segments[1].equals("runtime")) {
                                runtime.add(coordinate);
                            }
                        }
                    }
                }
            }
            if (classes == null) {
                return CompletableFuture.completedStage(null);
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No Maven plugin tools resolved upstream of the descriptor step");
            }
            if (pom == null) {
                throw new IllegalStateException("No " + POM + " is handed to the descriptor step, which reads the"
                        + " plugin's coordinate from it - stage the module, so that the build describes it");
            }
            Element project;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                project = factory.newDocumentBuilder().parse(pom.toFile()).getDocumentElement();
            } catch (ParserConfigurationException | SAXException e) {
                throw new IOException("Cannot read " + pom, e);
            }
            Properties request = new Properties();
            for (String element : List.of("groupId", "artifactId", "version", "name", "description")) {
                for (Node child = project.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element value && value.getLocalName().equals(element)) {
                        request.setProperty(switch (element) {
                            case "groupId" -> "group";
                            case "artifactId" -> "artifact";
                            default -> element;
                        }, value.getTextContent().strip());
                    }
                }
            }
            request.setProperty("classes", classes.toString());
            request.setProperty("encoding", "UTF-8");
            request.setProperty("output", Files.createDirectories(context.next()
                    .resolve(BuildStep.CLASSES)
                    .resolve("META-INF/maven")).toString());
            request.setProperty("work", Files.createDirectories(context.supplement().resolve("work")).toString());
            if (goalPrefix != null) {
                request.setProperty("goalprefix", goalPrefix);
            }
            for (int index = 0; index < sources.size(); index++) {
                request.setProperty("sources." + index, sources.get(index));
            }
            int index = 0;
            for (Map.Entry<String, String> entry : linked.entrySet()) {
                request.setProperty("dependency." + index, entry.getKey() + ":"
                        + (runtime.contains(entry.getKey()) ? "compile" : "provided"));
                request.setProperty("dependency." + index++ + ".file", entry.getValue());
            }
            Path file = context.supplement().resolve("request.properties");
            try (Writer writer = Files.newBufferedWriter(file)) {
                request.store(writer, null);
            }
            return CompletableFuture.completedStage(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(MavenPluginDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    MavenPluginDriver.class.getName(),
                    file.toString()));
        }
    }
}
