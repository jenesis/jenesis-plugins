package build.jenesis.plugin.gradleplugin;

import module java.base;
import module java.xml;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorModule;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepArgument;
import build.jenesis.BuildStepContext;
import build.jenesis.BuildStepResult;

public class GradlePluginModule implements BuildExecutorModule {

    public static final String MARKERS = "markers";
    private static final String MAVEN = "maven",
            DESCRIPTORS = "META-INF/gradle-plugins/",
            IMPLEMENTATION = "implementation-class",
            POM = "http://maven.apache.org/POM/4.0.0";
    private static final Pattern ID = Pattern.compile("[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*");
    private static final List<String> COPIED = List.of("name", "description", "url", "licenses", "developers", "scm");

    @Override
    public void accept(BuildExecutor buildExecutor, SequencedMap<String, Path> inherited) {
        buildExecutor.addStep(MARKERS, new Markers(), inherited.sequencedKeySet());
    }

    private record Markers() implements BuildStep {

        @Override
        public CompletionStage<BuildStepResult> apply(Executor executor,
                                                      BuildStepContext context,
                                                      SequencedMap<String, BuildStepArgument> arguments)
                throws IOException {
            SequencedMap<String, String> declared = new TreeMap<>();
            for (Map.Entry<String, BuildStepArgument> argument : arguments.entrySet()) {
                String tree = argument.getKey().substring(argument.getKey().lastIndexOf('/') + 1);
                if (!tree.equals(MAVEN) || argument.getValue().removed()) {
                    continue;
                }
                Path folder = argument.getValue().folder();
                List<Path> jars;
                try (Stream<Path> files = Files.walk(folder)) {
                    jars = files.filter(file -> file.getFileName().toString().endsWith(".jar")).sorted().toList();
                }
                for (Path jar : jars) {
                    Path versioned = jar.getParent(), named = versioned.getParent();
                    String version = versioned.getFileName().toString(), artifact = named.getFileName().toString();
                    if (!jar.getFileName().toString().equals(artifact + "-" + version + ".jar")) {
                        continue;
                    }
                    String group = folder.relativize(named.getParent()).toString().replace(File.separatorChar, '.');
                    try (JarFile file = new JarFile(jar.toFile())) {
                        for (JarEntry entry : Collections.list(file.entries())) {
                            String name = entry.getName();
                            if (!name.startsWith(DESCRIPTORS)
                                    || !name.endsWith(".properties")
                                    || name.indexOf('/', DESCRIPTORS.length()) != -1) {
                                continue;
                            }
                            String id = name.substring(DESCRIPTORS.length(), name.length() - ".properties".length());
                            String origin = "The Gradle plugin " + id + " of " + group + ":" + artifact + ":" + version;
                            if (!ID.matcher(id).matches()) {
                                throw new IllegalStateException(origin + " is no plugin ID - name it with letters,"
                                        + " digits and - in segments separated by single dots, as com.example.greet");
                            }
                            if (id.equals("org.gradle") || id.startsWith("org.gradle.")
                                    || id.equals("com.gradleware") || id.startsWith("com.gradleware.")) {
                                throw new IllegalStateException(origin + " takes a namespace Gradle reserves for its"
                                        + " own plugins - name it in a namespace of the project, as com.example.greet");
                            }
                            Properties descriptor = new Properties();
                            try (InputStream input = file.getInputStream(entry)) {
                                descriptor.load(input);
                            }
                            String implementation = descriptor.getProperty(IMPLEMENTATION, "").strip();
                            if (implementation.isEmpty()) {
                                throw new IllegalStateException(origin + " names no " + IMPLEMENTATION + " in " + name
                                        + " - name the class implementing org.gradle.api.Plugin");
                            }
                            if (file.getEntry(implementation.replace('.', '/') + ".class") == null) {
                                throw new IllegalStateException(origin + " names the " + IMPLEMENTATION + " "
                                        + implementation + ", which its jar does not hold - name a class of the jar");
                            }
                            String previous = declared.putIfAbsent(id, group + ":" + artifact + ":" + version);
                            if (previous != null) {
                                throw new IllegalStateException(origin + " is declared by " + previous + " as well -"
                                        + " give each plugin an ID of its own");
                            }
                            Path marker = context.next()
                                    .resolve(MAVEN)
                                    .resolve(id.replace('.', '/'))
                                    .resolve(id + ".gradle.plugin")
                                    .resolve(version)
                                    .resolve(id + ".gradle.plugin-" + version + ".pom");
                            Files.createDirectories(marker.getParent());
                            write(marker,
                                    id,
                                    group,
                                    artifact,
                                    version,
                                    versioned.resolve(artifact + "-" + version + ".pom"));
                        }
                    }
                }
            }
            return CompletableFuture.completedStage(new BuildStepResult(true));
        }

        private static void write(Path marker, String id, String group, String artifact, String version, Path pom)
                throws IOException {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.newDocument();
                Element project = document.createElementNS(POM, "project");
                document.appendChild(project);
                BiConsumer<Element, Map.Entry<String, String>> text = (parent, value) -> {
                    Element child = document.createElementNS(POM, value.getKey());
                    child.setTextContent(value.getValue());
                    parent.appendChild(child);
                };
                text.accept(project, Map.entry("modelVersion", "4.0.0"));
                text.accept(project, Map.entry("groupId", id));
                text.accept(project, Map.entry("artifactId", id + ".gradle.plugin"));
                text.accept(project, Map.entry("version", version));
                text.accept(project, Map.entry("packaging", "pom"));
                if (Files.isRegularFile(pom)) {
                    Element staged = builder.parse(pom.toFile()).getDocumentElement();
                    for (String copied : COPIED) {
                        for (Node child = staged.getFirstChild(); child != null; child = child.getNextSibling()) {
                            if (child instanceof Element element && copied.equals(element.getLocalName())) {
                                project.appendChild(document.importNode(element, true));
                            }
                        }
                    }
                }
                Element dependencies = document.createElementNS(POM, "dependencies"),
                        dependency = document.createElementNS(POM, "dependency");
                project.appendChild(dependencies);
                dependencies.appendChild(dependency);
                text.accept(dependency, Map.entry("groupId", group));
                text.accept(dependency, Map.entry("artifactId", artifact));
                text.accept(dependency, Map.entry("version", version));
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                try (Writer writer = Files.newBufferedWriter(marker)) {
                    transformer.transform(new DOMSource(document), new StreamResult(writer));
                }
            } catch (ParserConfigurationException | SAXException | TransformerException e) {
                throw new IOException("Cannot write the marker " + marker, e);
            }
        }
    }
}
