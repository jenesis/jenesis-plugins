package build.jenesis.plugin.princeofspace;

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

public class PrinceOfSpaceModule implements BuildExecutorModule {

    public static final String FORMAT = "format";
    private static final String REQUIRED = "required", DEPENDENCIES = "dependencies";
    private static final String MAVEN_GROUP = "io.github.agustafson.princeofspace",
            MAVEN_ARTIFACT = "prince-of-space-core";

    private final Dependencies dependencies;
    private final Pinning pinning;
    private final String group;
    private final boolean verify;
    private final String wrapStyle;
    private final String indentStyle;
    private final int indentSize;
    private final int lineLength;
    private final boolean closingParenOnNewLine;
    private final boolean trailingCommas;
    private final int javaVersion;
    private final ProcessBuildStep.Terms terms;

    public PrinceOfSpaceModule(Map<String, Repository> repositories,
                               Map<String, Resolver> resolvers) {
        this(new Dependencies(repositories, resolvers),
             null,
             "prince-of-space",
             true,
             "balanced",
             "spaces",
             4,
             120,
             true,
             false,
             17,
             new ProcessBuildStep.Terms());
    }

    public static PrinceOfSpaceModule ofEnvironment(Environment environment,
                                                    Map<String, Repository> repositories,
                                                    Map<String, Resolver> resolvers) {
        return new PrinceOfSpaceModule(Dependencies.ofEnvironment(environment, repositories, resolvers),
                Pinning.ofEnvironment(environment),
                "prince-of-space",
                !environment.flag("format.rewrite", false, Setting.PROJECT),
                "balanced",
                "spaces",
                4,
                120,
                true,
                false,
                17,
                ProcessBuildStep.Terms.ofEnvironment(environment, "prince-of-space"));
    }

    private PrinceOfSpaceModule(Dependencies dependencies,
                                Pinning pinning,
                                String group,
                                boolean verify,
                                String wrapStyle,
                                String indentStyle,
                                int indentSize,
                                int lineLength,
                                boolean closingParenOnNewLine,
                                boolean trailingCommas,
                                int javaVersion,
                                ProcessBuildStep.Terms terms) {
        this.dependencies = dependencies;
        this.pinning = pinning;
        this.group = group;
        this.verify = verify;
        this.wrapStyle = wrapStyle;
        this.indentStyle = indentStyle;
        this.indentSize = indentSize;
        this.lineLength = lineLength;
        this.closingParenOnNewLine = closingParenOnNewLine;
        this.trailingCommas = trailingCommas;
        this.javaVersion = javaVersion;
        this.terms = terms;
    }

    public PrinceOfSpaceModule pinning(Pinning pinning) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule group(String group) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule verify(boolean verify) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule wrapStyle(String wrapStyle) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule indentStyle(String indentStyle) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule indentSize(int indentSize) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule lineLength(int lineLength) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule closingParenOnNewLine(boolean closingParenOnNewLine) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule trailingCommas(boolean trailingCommas) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule javaVersion(int javaVersion) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms);
    }

    public PrinceOfSpaceModule printing(BiConsumer<Boolean, String> printing) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms.printing(printing));
    }

    public PrinceOfSpaceModule permits(Permits permits) {
        return new PrinceOfSpaceModule(dependencies,
                pinning,
                group,
                verify,
                wrapStyle,
                indentStyle,
                indentSize,
                lineLength,
                closingParenOnNewLine,
                trailingCommas,
                javaVersion,
                terms.permits(permits));
    }

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(REQUIRED, new Requires(group), inherited.sequencedKeySet());
        SequencedSet<String> resolveInputs = new LinkedHashSet<>();
        resolveInputs.add(REQUIRED);
        resolveInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addModule(DEPENDENCIES,
                dependencies.pinning(pinning).group(group),
                resolveInputs);
        SequencedSet<String> formatInputs = new LinkedHashSet<>();
        formatInputs.add(DEPENDENCIES);
        formatInputs.addAll(inherited.sequencedKeySet());
        buildExecutor.addStep(FORMAT,
                new Format(terms,
                        group,
                        verify,
                        wrapStyle,
                        indentStyle,
                        indentSize,
                        lineLength,
                        closingParenOnNewLine,
                        trailingCommas,
                        javaVersion),
                formatInputs);
    }

    private record Requires(String group) implements BuildStep {

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
            requires.setProperty(group + "/runtime/maven/" + MAVEN_GROUP + "/" + MAVEN_ARTIFACT + "/RELEASE", "");
            requires.store(context.next().resolve(BuildStep.REQUIRES));
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }
    }

    private static class Format extends FormatBuildStep {

        private final String wrapStyle, indentStyle;
        private final int indentSize, lineLength, javaVersion;
        private final boolean closingParenOnNewLine, trailingCommas;

        private Format(ProcessBuildStep.Terms terms,
                       String group,
                       boolean verify,
                       String wrapStyle,
                       String indentStyle,
                       int indentSize,
                       int lineLength,
                       boolean closingParenOnNewLine,
                       boolean trailingCommas,
                       int javaVersion) {
            super("prince-of-space", group, verify, terms);
            this.wrapStyle = wrapStyle;
            this.indentStyle = indentStyle;
            this.indentSize = indentSize;
            this.lineLength = lineLength;
            this.closingParenOnNewLine = closingParenOnNewLine;
            this.trailingCommas = trailingCommas;
            this.javaVersion = javaVersion;
        }

        @Override
        protected boolean isFormattable(Path file) {
            return file.toString().endsWith(".java")
                    && !file.getFileName().toString().equals("module-info.java");
        }

        @Override
        protected List<String> command(BuildStepContext context,
                                       List<String> jars,
                                       Path config,
                                       List<String> files,
                                       boolean verify) throws IOException {
            List<String> commands = new ArrayList<>(List.of(
                    "-cp",
                    ProcessHandler.OfProcess.driver(PrinceOfSpaceDriver.class) + File.pathSeparator
                            + String.join(File.pathSeparator, jars),
                    PrinceOfSpaceDriver.class.getName()));
            commands.add("--wrap-style=" + wrapStyle);
            commands.add("--indent-style=" + indentStyle);
            commands.add("--indent-size=" + indentSize);
            commands.add("--line-length=" + lineLength);
            commands.add("--closing-paren-on-new-line=" + closingParenOnNewLine);
            commands.add("--trailing-commas=" + trailingCommas);
            commands.add("--java-version=" + javaVersion);
            if (verify) {
                commands.add("--check");
            }
            commands.addAll(files);
            return commands;
        }
    }
}
