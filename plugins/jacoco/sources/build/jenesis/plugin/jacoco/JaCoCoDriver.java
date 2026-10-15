package build.jenesis.plugin.jacoco;

import module java.base;
import org.jacoco.cli.internal.Main;

public final class JaCoCoDriver {

    private JaCoCoDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        Constructor<Main> constructor = Main.class.getDeclaredConstructor(String[].class);
        constructor.setAccessible(true);
        return constructor.newInstance((Object) arguments).execute(new PrintWriter(System.out, true),
                new PrintWriter(System.err, true));
    }
}
