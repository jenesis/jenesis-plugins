package build.jenesis.plugin.groovy;

import module java.base;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;
import build.jenesis.SequencedProperties;

record GroovyRequires(Set<String> prefixes,
                      String tool,
                      String moduleName,
                      String mavenArtifact,
                      String purpose) implements BuildStep {

    private static final List<String> PREFERRED_PREFIXES = List.of("maven", "module");
    private static final String MAVEN_GROUP = "org.apache.groovy";

    @Override
    public boolean shouldRun(SequencedMap<String, BuildStepArgument> arguments) {
        return false;
    }

    @Override
    public CompletionStage<BuildStepResult> apply(Executor executor,
                                                  BuildStepContext context,
                                                  SequencedMap<String, BuildStepArgument> arguments)
            throws IOException {
        String prefix = PREFERRED_PREFIXES.stream()
                .filter(prefixes::contains)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No suitable resolver for " + purpose
                        + ". Available prefixes: " + new TreeSet<>(prefixes)
                        + ". Expected one of: " + PREFERRED_PREFIXES));
        String coordinate = prefix.equals("module")
                ? prefix + "/" + moduleName
                : prefix + "/" + MAVEN_GROUP + "/" + mavenArtifact + "/RELEASE";
        SequencedProperties requires = new SequencedProperties();
        requires.setProperty(tool + "/runtime/" + coordinate, "");
        requires.store(context.next().resolve(BuildStep.REQUIRES));
        return CompletableFuture.completedStage(new BuildStepResult(true));
    }
}
