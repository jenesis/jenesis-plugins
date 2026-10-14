package build.jenesis.plugin.scala;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.project.Language;
import build.jenesis.project.LanguageFactory;

public class ScalaFactory implements LanguageFactory {

    @Override
    public Language create(Environment environment,
                           SequencedMap<String, String> properties,
                           Semaphore permits,
                           String group,
                           Map<String, Repository> repositories,
                           Map<String, Resolver> resolvers) {
        if (!properties.isEmpty()) {
            throw new IllegalArgumentException("The Scala plugin takes no values, but was handed "
                    + properties.sequencedKeySet()
                    + ": set jenesis.scala.incremental to compile incrementally");
        }
        return new Language(new LinkedHashSet<>(List.of(".scala")),
                Language.Order.BEFORE_JAVAC,
                ScalaCompilerModule.ofEnvironment(environment, repositories, resolvers)
                        .permits(permits)
                        .tool(group)
                        .zinc(group + ":zinc"),
                ScalaDocumentationModule.ofEnvironment(environment, repositories, resolvers)
                        .permits(permits)
                        .tool(group + ":scaladoc"));
    }
}
