package build.jenesis.plugin.avro;

import module java.base;

public final class AvroDriver {

    private AvroDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Object tool = Class.forName("org.apache.avro.tool.SpecificCompilerTool").getConstructor().newInstance();
        try {
            return (int) tool.getClass()
                    .getMethod("run", InputStream.class, PrintStream.class, PrintStream.class, List.class)
                    .invoke(tool, System.in, System.out, System.err, List.of(arguments));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
