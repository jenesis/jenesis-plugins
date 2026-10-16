package build.jenesis.plugin.scala;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.Language;
import build.jenesis.LanguageFactory;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class ScalaFactory implements LanguageFactory {

    @Override
    public Language create(Environment environment,
                           SequencedMap<String, String> properties,
                           Permits permits,
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
                        .tool(group + ":tool")
                        .zinc(group + ":zinc"),
                ScalaDocumentationModule.ofEnvironment(environment, repositories, resolvers)
                        .permits(permits)
                        .tool(group + ":scaladoc"));
    }
}
