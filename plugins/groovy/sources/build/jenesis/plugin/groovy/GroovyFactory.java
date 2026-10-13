package build.jenesis.plugin.groovy;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.project.Language;
import build.jenesis.project.LanguageFactory;

public class GroovyFactory implements LanguageFactory {

    private static final String INCLUDE_JAVA = "include-java";

    @Override
    public Language create(Environment environment,
                           SequencedMap<String, String> properties,
                           Semaphore permits,
                           String group,
                           Map<String, Repository> repositories,
                           Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!entry.getKey().equals(INCLUDE_JAVA)) {
                throw new IllegalArgumentException("The Groovy plugin takes no value " + entry.getKey()
                        + ": name " + INCLUDE_JAVA + " to document the Java sources with Groovydoc as well");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return new Language(new LinkedHashSet<>(List.of(".groovy")),
                Language.Order.AFTER_JAVAC,
                GroovyCompilerModule.ofEnvironment(environment, repositories, resolvers)
                        .permits(permits)
                        .tool(group),
                GroovyDocumentationModule.ofEnvironment(environment, repositories, resolvers)
                        .permits(permits)
                        .tool(group)
                        .includeJava(values.flag(INCLUDE_JAVA, false)));
    }
}
