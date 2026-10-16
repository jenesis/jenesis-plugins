package build.jenesis.plugin.eclipse;

import module java.base;
import module java.xml;

public final class EclipseJavaFormatDriver {

    private EclipseJavaFormatDriver() {
    }

    @SuppressWarnings("unchecked")
    public static int main(String[] arguments) throws Exception {
        Class<?> javaCore = Class.forName("org.eclipse.jdt.core.JavaCore"),
                toolFactory = Class.forName("org.eclipse.jdt.core.ToolFactory"),
                codeFormatter = Class.forName("org.eclipse.jdt.core.formatter.CodeFormatter"),
                constants = Class.forName("org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants"),
                document = Class.forName("org.eclipse.jface.text.Document"),
                textEdit = Class.forName("org.eclipse.text.edits.TextEdit");
        try {
            Map<String, String> options = new HashMap<>((Map<String, String>) constants
                    .getMethod("getEclipseDefaultSettings")
                    .invoke(null));
            if (!arguments[1].isEmpty()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                NodeList profiles = factory.newDocumentBuilder()
                        .parse(Path.of(arguments[1]).toFile())
                        .getElementsByTagName("profile");
                if (profiles.getLength() != 1) {
                    throw new IllegalArgumentException("The Eclipse formatter profile holds " + profiles.getLength()
                            + " profiles rather than one - export only the profile to format with");
                }
                NodeList settings = ((Element) profiles.item(0)).getElementsByTagName("setting");
                for (int index = 0; index < settings.getLength(); index++) {
                    Element setting = (Element) settings.item(index);
                    options.put(setting.getAttribute("id"), setting.getAttribute("value"));
                }
            }
            String release = (String) javaCore.getMethod("latestSupportedJavaVersion").invoke(null);
            for (String option : List.of("COMPILER_SOURCE", "COMPILER_COMPLIANCE", "COMPILER_CODEGEN_TARGET_PLATFORM")) {
                options.put((String) javaCore.getField(option).get(null), release);
            }
            Object formatter = toolFactory.getMethod("createCodeFormatter", Map.class, int.class)
                    .invoke(null, options, toolFactory.getField("M_FORMAT_EXISTING").getInt(null));
            int kind = codeFormatter.getField("K_COMPILATION_UNIT").getInt(null)
                    | codeFormatter.getField("F_INCLUDE_COMMENTS").getInt(null);
            Method format = codeFormatter.getMethod("format",
                    int.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class,
                    String.class),
                    apply = textEdit.getMethod("apply", Class.forName("org.eclipse.jface.text.IDocument")),
                    get = document.getMethod("get");
            Constructor<?> create = document.getConstructor(String.class);
            boolean verify = arguments[0].equals("verify");
            List<String> unformatted = new ArrayList<>();
            for (String name : List.of(arguments).subList(2, arguments.length)) {
                Path file = Path.of(name);
                String source = Files.readString(file);
                Object edit = format.invoke(formatter,
                        kind,
                        source,
                        0,
                        source.length(),
                        0,
                        source.contains("\r\n") ? "\r\n" : "\n");
                if (edit == null) {
                    throw new IllegalStateException("The Eclipse formatter cannot parse " + name);
                }
                Object formatting = create.newInstance(source);
                apply.invoke(edit, formatting);
                String formatted = (String) get.invoke(formatting);
                if (!formatted.equals(source)) {
                    unformatted.add(name);
                    if (!verify) {
                        Files.writeString(file, formatted);
                    }
                }
            }
            if (verify && !unformatted.isEmpty()) {
                unformatted.forEach(name -> System.out.println(name + " is not formatted as the profile says"));
                return 1;
            }
            return 0;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
