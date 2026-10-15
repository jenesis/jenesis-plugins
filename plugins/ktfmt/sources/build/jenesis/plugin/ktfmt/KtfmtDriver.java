package build.jenesis.plugin.ktfmt;

import module java.base;
import com.facebook.ktfmt.cli.Main;

public final class KtfmtDriver {

    private KtfmtDriver() {
    }

    public static int main(String[] arguments) {
        return new Main(System.in, System.out, System.err, arguments).run();
    }
}
