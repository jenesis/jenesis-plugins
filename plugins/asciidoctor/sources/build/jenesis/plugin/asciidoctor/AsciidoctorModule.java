package build.jenesis.plugin.asciidoctor;

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

public class AsciidoctorModule implements BuildExecutorModule {

    public static final String RENDER = "render", DOCUMENTS = "../inputs/docs", PROJECT = "project";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String MAVEN_GROUP = "org.asciidoctor", MAVEN_ARTIFACT = "asciidoctorj";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String tool;
    private final String backend;
    private final String output;
    private final SequencedMap<String, String> attributes;
    private final ProcessBuildStep.Terms terms;

    public AsciidoctorModule(Map<String, Repository> repositories,
                             Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "asciidoctor",
             "html5",
             "docs",
             Collections.emptySortedMap(),
             new ProcessBuildStep.Terms());
    }

    public static AsciidoctorModule ofEnvironment(Environment environment,
                                                  Map<String, Repository> repositories,
                                                  Map<String, Resolver> resolvers) {
        return new AsciidoctorModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "asciidoctor",
                "html5",
                "docs",
                Collections.emptySortedMap(),
                ProcessBuildStep.Terms.ofEnvironment(environment, "asciidoctor"));
    }

    private AsciidoctorModule(Dependencies dependencies,
                              Pinning pinning,
                              String tool,
                              String backend,
                              String output,
                              SequencedMap<String, String> attributes,
                              ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.tool = tool;
        this.backend = backend;
        this.output = output;
        this.attributes = attributes;
        this.terms = terms;
    }

    public AsciidoctorModule pinning(Pinning pinning) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms);
    }

    public AsciidoctorModule tool(String tool) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms);
    }

    public AsciidoctorModule backend(String backend) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms);
    }

    public AsciidoctorModule output(String output) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms);
    }

    public AsciidoctorModule attributes(SequencedMap<String, String> attributes) {
        return new AsciidoctorModule(dependencies,
                pinning,
                tool,
                backend,
                output,
                Collections.unmodifiableSequencedMap(new LinkedHashMap<>(attributes)),
                terms);
    }

    public AsciidoctorModule printing(BiConsumer<Boolean, String> printing) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms.printing(printing));
    }

    public AsciidoctorModule permits(Permits permits) {
        return new AsciidoctorModule(dependencies, pinning, tool, backend, output, attributes, terms.permits(permits));
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
        SequencedSet<String> renderInputs = new LinkedHashSet<>();
        renderInputs.add(DEPENDENCIES);
        renderInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(RENDER,
                new Render(terms, tool, backend, output, new LinkedHashMap<>(attributes)),
                renderInputs);
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

    private static class Render extends ProcessBuildStep {

        private final String tool;
        private final String backend;
        private final String output;
        private final LinkedHashMap<String, String> attributes;

        private Render(ProcessBuildStep.Terms terms,
                       String tool,
                       String backend,
                       String output,
                       LinkedHashMap<String, String> attributes) {
            super("asciidoctor", ProcessHandler.OfProcess.ofJavaMain(), terms);
            this.tool = tool;
            this.backend = backend;
            this.output = output;
            this.attributes = attributes;
        }

        @Override
        public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
            return arguments.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(DOCUMENTS) || entry.getKey().equals(DEPENDENCIES))
                    .anyMatch(entry -> entry.getValue().hasChanged());
        }

        @Override
        protected CompletionStage<List<String>> process(Executor executor,
                                                        BuildStepContext context,
                                                        SequencedMap<String, BuildStepArgument> arguments,
                                                        SequencedMap<String, SequencedMap<String, String>> properties)
                throws IOException {
            BuildStepArgument documents = arguments.get(DOCUMENTS);
            if (documents == null || documents.removed()) {
                throw new IllegalStateException("The Asciidoctor plugin is handed no folder of documents - bind the"
                        + " folder that holds the .adoc files as its input docs, such as asciidoctor.@docs=docs");
            }
            List<String> jars = new ArrayList<>();
            for (BuildStepArgument argument : arguments.values()) {
                if (!argument.removed()) {
                    for (Path jar : Dependencies.select(argument.folder(), tool, "runtime")) {
                        jars.add(jar.toString());
                    }
                }
            }
            if (jars.isEmpty()) {
                throw new IllegalStateException("No Asciidoctor jars resolved upstream of the Asciidoctor step");
            }
            Path target = context.next().resolve(PROJECT).resolve(output);
            Files.createDirectories(target);
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(AsciidoctorDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    AsciidoctorDriver.class.getName(),
                    "--source",
                    documents.folder().toString(),
                    "--target",
                    target.toString(),
                    "--backend",
                    backend));
            attributes.forEach((name, value) -> {
                commands.add("--attribute");
                commands.add(name + "=" + value);
            });
            return CompletableFuture.completedStage(commands);
        }
    }
}
