package build.jenesis.plugin.forbiddenapis;

import module java.base;
import de.thetaphi.forbiddenapis.cli.CliMain;

public final class ForbiddenApisDriver {

    private ForbiddenApisDriver() {
    }

    public static int main(String[] arguments) throws Exception {
        try {
            new CliMain(arguments).run();
            return 0;
        } catch (Exception e) {
            if (!e.getClass().getName().equals("de.thetaphi.forbiddenapis.cli.ExitException")) {
                throw e;
            }
            if (e.getMessage() != null) {
                System.err.println("ERROR: " + e.getMessage());
            }
            Field code = e.getClass().getField("exitCode");
            code.setAccessible(true);
            return code.getInt(e);
        }
    }
}
