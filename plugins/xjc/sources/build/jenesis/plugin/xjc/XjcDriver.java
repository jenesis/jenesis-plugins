package build.jenesis.plugin.xjc;

import module java.base;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Driver;

public final class XjcDriver {

    private XjcDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        try {
            return Driver.run(arguments, System.out, System.out);
        } catch (BadCommandLineException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
}
