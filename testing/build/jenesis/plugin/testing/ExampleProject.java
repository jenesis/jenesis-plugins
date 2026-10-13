package build.jenesis.plugin.testing;

import module java.base;
import build.jenesis.BuildExecutor;
import build.jenesis.MakeTool;

public record ExampleProject(Path root, Path repository) {

    public static ExampleProject of(Path folder, Class<?> plugin) throws IOException {
        Path repository = folder.resolve("repository");
        install(repository, plugin);
        install(repository, BuildExecutor.class);
        return new ExampleProject(Files.createDirectories(folder.resolve("project")), repository);
    }

    private static void install(Path repository, Class<?> type) throws IOException {
        String module = type.getModule().getName();
        if (module == null) {
            throw new IllegalStateException(type + " is not in a named module, so it cannot be installed as one");
        }
        Path location;
        try {
            location = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot locate the code of " + module, e);
        }
        Path target = Files.createDirectories(repository.resolve(module)).resolve(module + ".jar");
        if (Files.isDirectory(location)) {
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(target));
                 Stream<Path> files = Files.walk(location)) {
                for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                    String name = location.relativize(file).toString().replace(File.separatorChar, '/');
                    out.putNextEntry(new JarEntry(name));
                    Files.copy(file, out);
                    out.closeEntry();
                }
            }
        } else {
            Files.copy(location, target);
        }
        Optional<String> version = type.getModule().getDescriptor().rawVersion();
        if (version.isPresent()) {
            Files.copy(target, Files.createDirectories(repository.resolve(module).resolve(version.get()))
                    .resolve(module + ".jar"));
        }
    }

    public ExampleProject write(String file, String content) throws IOException {
        Path target = root.resolve(file);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
        return this;
    }

    public String make(String... selectors) {
        StringWriter out = new StringWriter(), err = new StringWriter();
        List<String> arguments = new ArrayList<>(List.of(
                "-Djenesis.make.root=" + root,
                "-Djenesis.module.local=" + repository,
                "-Djenesis.palette.colors=none"));
        arguments.addAll(List.of(selectors));
        int code = new MakeTool().run(new PrintWriter(out, true),
                new PrintWriter(err, true),
                arguments.toArray(String[]::new));
        if (code != 0) {
            throw new IllegalStateException("The example build failed with exit code " + code + ":\n" + out + err);
        }
        return out.toString();
    }

    public Path find(String name) throws IOException {
        try (Stream<Path> files = Files.walk(root.resolve("target"))) {
            return files.filter(file -> file.getFileName().toString().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The example build wrote no " + name));
        }
    }
}
