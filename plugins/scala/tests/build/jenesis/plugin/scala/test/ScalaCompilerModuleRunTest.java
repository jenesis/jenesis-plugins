package build.jenesis.plugin.scala.test;

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
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.scala.ScalaCompilerModule;
import build.jenesis.step.Dependencies;
import build.jenesis.step.ProcessBuildStep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScalaCompilerModuleRunTest {

    @TempDir
    private Path root, project;

    @Test
    public void compiles_a_scala_source_against_a_downloaded_compiler() throws IOException, ReflectiveOperationException {
        Path sample = sources();
        Files.writeString(sample.resolve("Sample.scala"), """
                package sample
                class Sample:
                  def greet(): String = "Hello world!"
                """);

        compile(false);

        Path classes = classes();
        assertThat(classes.resolve("sample/Sample.class")).isNotEmptyFile();
        List<URL> urls = new ArrayList<>(List.of(classes.toUri().toURL()));
        for (Path jar : Dependencies.all(root.resolve("scala/dependencies/resolve/output"))) {
            urls.add(jar.toUri().toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), null)) {
            Class<?> type = loader.loadClass("sample.Sample");
            assertThat(type.getMethod("greet").invoke(type.getConstructor().newInstance()))
                    .as("the compiled class runs on the compiler's own Scala library")
                    .isEqualTo("Hello world!");
        }
    }

    @Test
    public void skips_module_info_so_scalac_does_not_parse_it() throws IOException {
        Files.writeString(sources().resolve("Sample.scala"), """
                package sample
                class Sample:
                  def greet(): String = "Hello world!"
                """);
        Files.writeString(project.resolve(BuildStep.SOURCES + "module-info.java"), """
                module sample {
                    exports sample;
                }
                """);

        compile(false);

        assertThat(classes().resolve("sample/Sample.class"))
                .as("scalac compiled the Scala source while ignoring module-info.java")
                .isNotEmptyFile();
        assertThat(classes().resolve("module-info.class"))
                .as("scalac must not parse or emit module-info.java; javac owns it")
                .doesNotExist();
    }

    @Test
    public void leaves_the_resources_of_a_module_to_the_build() throws IOException {
        Path sample = sources();
        Files.writeString(sample.resolve("Sample.scala"), """
                package sample
                class Sample
                """);
        Files.writeString(sample.resolve("app.properties"), "key=value");

        compile(false);

        assertThat(classes().resolve("sample/Sample.class")).isNotEmptyFile();
        assertThat(classes().resolve("sample/app.properties"))
                .as("the build copies the resources of a module that a language compiles")
                .doesNotExist();
    }

    @Test
    public void picks_up_release_from_upstream_javac_properties() throws IOException {
        Files.writeString(sources().resolve("Sample.scala"), """
                package sample
                class Sample
                """);
        SequencedProperties javac = new SequencedProperties();
        javac.setProperty("--release", "17");
        ProcessBuildStep.store(project, ProcessBuildStep.PROCESS, "javac", javac);

        compile(false);

        byte[] bytes = Files.readAllBytes(classes().resolve("sample/Sample.class"));
        assertThat(((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF))
                .as("--release=17 propagated to -release so the class file carries major version 61")
                .isEqualTo(61);
    }

    @Test
    public void scala_can_reference_java_sources_supplied_to_the_same_step() throws IOException {
        Path sample = sources();
        Files.writeString(sample.resolve("Greeter.java"), """
                package sample;
                public class Greeter {
                    public String hello() { return "Hello"; }
                }
                """);
        Files.writeString(sample.resolve("Sample.scala"), """
                package sample
                class Sample:
                  def greet(): String = Greeter().hello() + " from Scala"
                """);

        compile(false);

        assertThat(classes().resolve("sample/Sample.class"))
                .as("Sample.scala resolved the Greeter type from the .java source companion and compiled")
                .isNotEmptyFile();
        assertThat(classes().resolve("sample/Greeter.class"))
                .as("the Java source is read for its symbols and compiled by javac")
                .doesNotExist();
    }

    @Test
    public void downloads_the_full_compiler_dependency_set_from_the_scala_pom() throws IOException {
        Files.writeString(sources().resolve("Sample.scala"), "package sample\nclass Sample\n");

        compile(false);

        assertThat(SequencedProperties.ofFiles(root.resolve("scala/dependencies/resolve/output")
                        .resolve(BuildStep.DEPENDENCIES))
                .stringPropertyNames())
                .anyMatch(name -> name.contains("scala3-compiler_3"))
                .anyMatch(name -> name.contains("scala3-library_3"))
                .anyMatch(name -> name.contains("scala3-interfaces"))
                .anyMatch(name -> name.contains("tasty-core_3"))
                .anyMatch(name -> name.contains("scala-library"))
                .anyMatch(name -> name.contains("compiler-interface"));
    }

    @Test
    public void recompiles_incrementally_only_what_a_change_reaches() throws IOException {
        Path sample = sources();
        Files.writeString(sample.resolve("Greeter.java"), """
                package sample;
                public class Greeter {
                    public String hello() { return "Hello"; }
                }
                """);
        Files.writeString(sample.resolve("Untouched.scala"), """
                package sample
                class Untouched:
                  def name(): String = Greeter().hello()
                """);
        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): String = "first"
                """);
        Files.writeString(sample.resolve("User.scala"), """
                package sample
                class User:
                  def use(): String = "used " + Edited().value()
                """);
        Files.writeString(sample.resolve("Removed.scala"), """
                package sample
                class Removed
                """);
        compile(true);
        Path classes = classes();
        FileTime untouched = Files.getLastModifiedTime(classes.resolve("sample/Untouched.class")),
                user = Files.getLastModifiedTime(classes.resolve("sample/User.class"));
        assertThat(root.resolve("scala/classes/output/incremental/analysis.bin"))
                .as("Zinc records its analysis beside the classes it compiled")
                .isNotEmptyFile();
        assertThat(classes.resolve("sample/Removed.class")).isNotEmptyFile();
        assertThat(classes.resolve("sample/Edited.tasty")).isNotEmptyFile();
        assertThat(classes.resolve("sample/Greeter.class"))
                .as("a Java source is compiled by javac after the Scala compiler, not published by Zinc")
                .doesNotExist();

        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): String = "second"
                """);
        Files.delete(sample.resolve("Removed.scala"));
        compile(true);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/Untouched.class")))
                .as("a source the change does not reach keeps the class the previous run compiled")
                .isEqualTo(untouched);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/User.class")))
                .as("a body changed behind an unchanged signature leaves the class that calls it as it was")
                .isEqualTo(user);
        assertThat(classes.resolve("sample/Removed.class"))
                .as("the class of a removed source is removed with it")
                .doesNotExist();
        assertThat(new String(Files.readAllBytes(classes.resolve("sample/Edited.class")), StandardCharsets.ISO_8859_1))
                .as("the edited source is compiled again")
                .contains("second");

        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): Int = 2
                """);
        compile(true);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/User.class")))
                .as("a changed signature recompiles the source that uses it")
                .isNotEqualTo(user);
        assertThat(Files.getLastModifiedTime(classes.resolve("sample/Untouched.class"))).isEqualTo(untouched);
    }

    @Test
    public void compiles_incrementally_in_a_warm_jvm_that_outlives_a_failed_compile() throws IOException {
        Path sample = sources();
        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): String = "first"
                """);
        Environment warm = new Environment(Map.of("process.warm", "true"));
        compile(warm);
        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): String = undefined
                """);
        assertThatThrownBy(() -> compile(warm))
                .as("a source that does not compile fails the build")
                .isNotNull();
        Files.writeString(sample.resolve("Edited.scala"), """
                package sample
                class Edited:
                  def value(): String = "second"
                """);
        compile(warm);
        assertThat(new String(Files.readAllBytes(classes().resolve("sample/Edited.class")), StandardCharsets.ISO_8859_1))
                .as("the JVM that compiled before compiles again after a failure")
                .contains("second");
    }

    private Path sources() throws IOException {
        ScalaPins.write(project, "scalac", "zinc", "scaladoc");
        return Files.createDirectories(project.resolve(BuildStep.SOURCES + "sample"));
    }

    private Path classes() {
        return root.resolve("scala")
                .resolve(ScalaCompilerModule.COMPILED_CLASSES)
                .resolve("output")
                .resolve(BuildStep.CLASSES);
    }

    private void compile(boolean incremental) throws IOException {
        compile(new ScalaCompilerModule(Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE)))
                .incremental(incremental));
    }

    private void compile(Environment environment) throws IOException {
        compile(ScalaCompilerModule.ofEnvironment(environment,
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(environment)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(environment)))
                .incremental(true));
    }

    private void compile(ScalaCompilerModule module) throws IOException {
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
        executor.addModule("scala", module.pinning(Pinning.STRICT), "project");
        executor.execute();
    }
}
