package build.jenesis.plugin.jreleaser.test;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.HashDigestFunction;
import build.jenesis.Repository;
import build.jenesis.RepositoryItem;
import build.jenesis.SequencedProperties;
import build.jenesis.project.ProjectPlugins;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

record FakeTool(Path jar) {

    static FakeTool of(Path folder) throws IOException {
        Path sources = Files.createDirectories(folder.resolve("sources"));
        Files.writeString(sources.resolve("module-info.java"), """
                module org.jreleaser.tool {
                    provides java.util.spi.ToolProvider with org.jreleaser.tool.JReleaser;
                }
                """);
        Path type = Files.createDirectories(sources.resolve("org/jreleaser/tool")).resolve("JReleaser.java");
        Files.writeString(type, """
                package org.jreleaser.tool;

                import java.io.PrintWriter;
                import java.nio.file.Files;
                import java.nio.file.Path;
                import java.util.List;

                public class JReleaser implements java.util.spi.ToolProvider {

                    public String name() {
                        return "jreleaser";
                    }

                    public int run(PrintWriter out, PrintWriter err, String... args) {
                        List<String> arguments = List.of(args);
                        Path output = Path.of(arguments.get(arguments.indexOf("--output-directory") + 1));
                        try {
                            Files.write(output.resolve("arguments.txt"), arguments);
                            int settings = arguments.indexOf("--settings-file");
                            if (settings != -1) {
                                Files.copy(Path.of(arguments.get(settings + 1)), output.resolve("settings.txt"));
                            }
                            Files.writeString(output.resolve("loader.txt"),
                                    String.valueOf(Thread.currentThread().getContextClassLoader() == getClass().getClassLoader()));
                        } catch (java.io.IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                        out.println("released " + arguments.getFirst());
                        return arguments.getFirst().equals("fail") ? 3 : 0;
                    }
                }
                """);
        Path classes = Files.createDirectories(folder.resolve("classes"));
        Files.writeString(Files.createDirectories(classes.resolve("META-INF/services")).resolve("java.util.spi.ToolProvider"),
                "org.jreleaser.tool.JReleaser\n");
        if (javax.tools.ToolProvider.getSystemJavaCompiler().run(null,
                null,
                null,
                "-d",
                classes.toString(),
                sources.resolve("module-info.java").toString(),
                type.toString()) != 0) {
            throw new IllegalStateException("Failed to compile the JReleaser double");
        }
        Path jar = folder.resolve("jreleaser.jar");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jar));
             Stream<Path> files = Files.walk(classes)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                stream.putNextEntry(new JarEntry(classes.relativize(file).toString().replace(File.separatorChar, '/')));
                stream.write(Files.readAllBytes(file));
                stream.closeEntry();
            }
        }
        return new FakeTool(jar);
    }

    Repository repository() {
        return (_, _, _) -> Optional.of(RepositoryItem.ofFile(jar));
    }

    static Path project(Path folder, Path root, String version) throws IOException {
        SequencedProperties located = new SequencedProperties();
        located.setProperty(ProjectPlugins.ROOT_KEY, root.toString());
        located.setProperty("stage.maven", root.resolve("staged/maven").toString());
        located.store(folder.resolve(ProjectPlugins.ROOT));
        SequencedProperties metadata = new SequencedProperties();
        if (version != null) {
            metadata.setProperty("version", version);
        }
        metadata.setProperty("scm.url", "https://example.org/sample");
        metadata.store(folder.resolve(build.jenesis.BuildStep.METADATA));
        Files.writeString(folder.resolve("jreleaser.yml"), "project:\n  name: sample\n");
        return folder;
    }

    static BuildExecutor executor(Path root) throws IOException {
        return BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
    }
}
