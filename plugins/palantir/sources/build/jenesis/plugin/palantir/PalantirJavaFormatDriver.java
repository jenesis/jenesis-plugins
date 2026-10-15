package build.jenesis.plugin.palantir;

import module java.base;
import com.palantir.javaformat.java.Main;

public final class PalantirJavaFormatDriver {

    private PalantirJavaFormatDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)),
                err = new PrintWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8));
        try {
            return new Main(out, err, System.in).format(arguments);
        } finally {
            out.flush();
            err.flush();
        }
    }
}
