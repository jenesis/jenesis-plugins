package build.jenesis.plugin.eclipse;

import module java.base;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildModuleFactory;
import build.jenesis.Environment;
import build.jenesis.Repository;
import build.jenesis.Resolver;

public class EclipseJavaFormatFactory implements BuildModuleFactory {

    private static final String PROFILE = "profile";

    @Override
    public BuildExecutorModule create(Environment environment,
                                      SequencedMap<String, String> properties,
                                      Path configuration,
                                      Semaphore permits,
                                      String group,
                                      Map<String, Repository> repositories,
                                      Map<String, Resolver> resolvers) {
        for (String key : properties.sequencedKeySet()) {
            if (!key.equals(PROFILE)) {
                throw new IllegalArgumentException("The Eclipse formatter plugin takes no value " + key
                        + ": name profile, the formatter profile exported from Eclipse");
            }
        }
        EclipseJavaFormatModule module = EclipseJavaFormatModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .group(group);
        String profile = properties.get(PROFILE);
        if (profile == null || profile.isBlank()) {
            return module;
        }
        if (configuration == null) {
            throw new IllegalStateException("The Eclipse formatter plugin names the profile " + profile
                    + " but was activated by no file to resolve it against - activate it by the file beside the"
                    + " profile");
        }
        Path relative = Path.of(profile.strip()).normalize();
        if (relative.isAbsolute() || relative.startsWith("..")) {
            throw new IllegalArgumentException("The Eclipse formatter plugin names the profile " + profile
                    + ", which is not beside or below " + configuration + " - place the profile in its folder");
        }
        Path resolved = configuration.resolveSibling(relative);
        if (!Files.isRegularFile(resolved)) {
            throw new IllegalArgumentException("The Eclipse formatter plugin names the profile " + profile
                    + ", but " + resolved + " is no file - export the formatter profile from Eclipse there");
        }
        return module.profile(resolved);
    }
}
