package build.jenesis.plugin.wsimport;

import module java.base;
import com.sun.tools.ws.WsImport;

public final class WsImportDriver {

    private WsImportDriver() {
    }

    public static int main(String[] arguments) throws Throwable {
        return WsImport.doMain(arguments);
    }
}
