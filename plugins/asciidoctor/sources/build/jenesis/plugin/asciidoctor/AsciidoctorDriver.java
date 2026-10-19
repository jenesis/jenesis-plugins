package build.jenesis.plugin.asciidoctor;

import module java.base;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.Severity;

public final class AsciidoctorDriver {

    private AsciidoctorDriver() {
    }

    public static int main(String[] arguments) throws IOException {
        Path source = null, target = null;
        String backend = "html5";
        SequencedMap<String, String> attributes = new LinkedHashMap<>();
        for (int index = 0; index < arguments.length; index++) {
            switch (arguments[index]) {
                case "--source" -> source = Path.of(arguments[++index]).toAbsolutePath();
                case "--target" -> target = Path.of(arguments[++index]).toAbsolutePath();
                case "--backend" -> backend = arguments[++index];
                case "--attribute" -> {
                    String attribute = arguments[++index];
                    int equals = attribute.indexOf('=');
                    attributes.put(attribute.substring(0, equals), attribute.substring(equals + 1));
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + arguments[index]);
            }
        }
        if (source == null || target == null) {
            throw new IllegalArgumentException("Expected --source and --target");
        }
        List<Path> documents = new ArrayList<>(), resources = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path file : walk.filter(Files::isRegularFile).sorted().toList()) {
                Path relative = source.relativize(file);
                if (!file.getFileName().toString().endsWith(".adoc")) {
                    resources.add(relative);
                } else if (StreamSupport.stream(relative.spliterator(), false)
                        .noneMatch(segment -> segment.toString().startsWith("_"))) {
                    documents.add(relative);
                }
            }
        }
        for (Path resource : resources) {
            Path copy = target.resolve(resource);
            Files.createDirectories(copy.getParent());
            Files.copy(source.resolve(resource), copy, StandardCopyOption.REPLACE_EXISTING);
        }
        AttributesBuilder builder = Attributes.builder().attribute("reproducible");
        attributes.forEach(builder::attribute);
        Attributes resolved = builder.build();
        List<String> errors = new ArrayList<>();
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            asciidoctor.registerLogHandler(record -> {
                System.err.println(record.getSeverity() + ": " + record.getMessage());
                if (record.getSeverity() == Severity.ERROR || record.getSeverity() == Severity.FATAL) {
                    errors.add(record.getMessage());
                }
            });
            for (Path document : documents) {
                Path folder = document.getParent() == null ? target : target.resolve(document.getParent());
                asciidoctor.convertFile(source.resolve(document).toFile(), Options.builder()
                        .backend(backend)
                        .toDir(folder.toFile())
                        .mkDirs(true)
                        .safe(SafeMode.UNSAFE)
                        .baseDir(source.resolve(document).getParent().toFile())
                        .attributes(resolved)
                        .build());
            }
        }
        return errors.isEmpty() ? 0 : 1;
    }
}
