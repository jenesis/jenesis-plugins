package build.jenesis.plugin.clojure.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Pinning;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.clojure.ClojureCompilerModule;
import build.jenesis.step.Dependencies;

import static org.assertj.core.api.Assertions.assertThat;

public class ClojureModuleRunTest {

    private static final String PINS = """
            main/maven/org.clojure/clojure 1.12.6 SHA-256/21b578db389a8e74b286ce79c4a7f86ec641e26d88b12f18d69030916b0e18ca
            main/maven/org.clojure/clojure/pom/1.12.6 1.12.6 SHA-256/6b85dea75a09f1eb08c8cc18a1b3120a3fd048a6343757a1b6be979ab6f910cf
            main/maven/org.clojure/core.specs.alpha 0.4.74 SHA-256/eb73ac08cf49ba840c88ba67beef11336ca554333d9408808d78946e0feb9ddb
            main/maven/org.clojure/core.specs.alpha/pom/0.4.74 0.4.74 SHA-256/33410eb8aa73d52d957b3dc6e0a65f3998ac0623cbd813d90c33e6e689c42429
            main/maven/org.clojure/data.json 2.5.2 SHA-256/0938c730961d32b331ea7e5cd7c6a822c0a889def79d1c3f2af4dd201fdddeb3
            main/maven/org.clojure/data.json/pom/2.5.2 2.5.2 SHA-256/818c87780270288abaa7239becdd06d1965ef38475e6438466ec67fcfc117f9a
            main/maven/org.clojure/pom.contrib/pom/1.2.0 1.2.0 SHA-256/0916d7a41558b9500a427c886fa29827ace5259206be3ad33e64e296fc1a6111
            main/maven/org.clojure/pom.contrib/pom/1.4.0 1.4.0 SHA-256/0993676876081f491ba5af82fa6b5a037a3bee3a125a02d6d5e27aa29f356787
            main/maven/org.clojure/spec.alpha 0.5.238 SHA-256/94cd99b6ea639641f37af4860a643b6ed399ee5a8be5d717cff0b663c8d75077
            main/maven/org.clojure/spec.alpha/pom/0.5.238 0.5.238 SHA-256/3cba7e0dcc085c4ce92dddffea226124ffac178be79bd0379b5b2e30a969cbea
            """;

    @TempDir
    private Path root, project, required;

    @Test
    public void compiles_the_namespaces_of_a_module_and_keeps_those_of_its_dependencies_out() throws Exception {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(required.resolve(BuildStep.VERSIONS));
        SequencedProperties requires = new SequencedProperties();
        requires.setProperty("main/compile/maven/org.clojure/clojure/1.12.6", "");
        requires.setProperty("main/compile/maven/org.clojure/data.json/2.5.2", "");
        requires.store(required.resolve(BuildStep.REQUIRES));
        Files.writeString(Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample")).resolve("core.clj"), """
                (ns sample.core
                  (:require [clojure.data.json :as json]))

                (defn greet []
                  (json/write-str {:greeting "Hello from Clojure"}))
                """);

        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        executor.addSource("project", project);
        executor.addSource("required", required);
        executor.addModule("dependencies",
                new Dependencies(repositories(), resolvers()).pinning(Pinning.STRICT).group("main"),
                "required");
        executor.addModule("clojure", new ClojureCompilerModule(), "project", "dependencies");
        executor.execute();

        Path classes = root.resolve("clojure").resolve(ClojureCompilerModule.VERSIONED).resolve("output")
                .resolve(BuildStep.CLASSES);
        assertThat(classes.resolve("sample/core__init.class")).isNotEmptyFile();
        assertThat(classes.resolve("sample/core.clj")).exists();
        try (Stream<Path> files = Files.walk(classes)) {
            assertThat(files.map(file -> classes.relativize(file).toString().replace(File.separatorChar, '/')))
                    .as("the namespaces of a dependency, compiled as the module's required them, stay in its own jar")
                    .noneMatch(name -> name.startsWith("clojure/data/json"));
        }
        List<URL> urls = new ArrayList<>(List.of(classes.toUri().toURL()));
        for (Path jar : Dependencies.all(root.resolve("dependencies").resolve("resolve").resolve("output"))) {
            urls.add(jar.toUri().toURL());
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader())) {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> clojure = loader.loadClass("clojure.java.api.Clojure"), function = loader.loadClass("clojure.lang.IFn");
            Method var = clojure.getMethod("var", Object.class, Object.class), read = clojure.getMethod("read", String.class);
            function.getMethod("invoke", Object.class).invoke(var.invoke(null, "clojure.core", "require"),
                    read.invoke(null, "sample.core"));
            assertThat(function.getMethod("invoke").invoke(var.invoke(null, "sample.core", "greet")))
                    .as("the compiled namespace runs on the Clojure it was compiled with, its dependency from source")
                    .isEqualTo("{\"greeting\":\"Hello from Clojure\"}");
        } finally {
            Thread.currentThread().setContextClassLoader(context);
        }
    }

    private static Map<String, Repository> repositories() {
        return Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE));
    }

    private static Map<String, Resolver> resolvers() {
        return Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE));
    }
}
