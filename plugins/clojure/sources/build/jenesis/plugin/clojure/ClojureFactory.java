package build.jenesis.plugin.clojure;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.Language;
import build.jenesis.LanguageFactory;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class ClojureFactory implements LanguageFactory {

    private static final String AOT = "aot";

    @Override
    public Language create(Environment environment,
                           SequencedMap<String, String> properties,
                           Permits permits,
                           String group,
                           Map<String, Repository> repositories,
                           Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!entry.getKey().equals(AOT)) {
                throw new IllegalArgumentException("The Clojure plugin takes no value " + entry.getKey()
                        + ": name " + AOT + "=false to ship the sources of a module without compiling them");
            }
            values.setProperty(entry.getKey(), entry.getValue());
        }
        return new Language(new LinkedHashSet<>(ClojureCompilerModule.EXTENSIONS),
                Language.Order.AFTER_JAVAC,
                ClojureCompilerModule.ofEnvironment(environment)
                        .permits(permits)
                        .aot(values.flag(AOT, true)),
                null);
    }
}
