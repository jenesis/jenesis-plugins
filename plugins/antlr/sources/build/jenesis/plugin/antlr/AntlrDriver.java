package build.jenesis.plugin.antlr;

import module java.base;
import org.antlr.v4.Tool;

public final class AntlrDriver {

    private AntlrDriver() {
    }

    public static int main(String[] arguments) {
        Tool tool = new Tool(arguments);
        tool.processGrammarsOnCommandLine();
        return tool.getNumErrors() == 0 ? 0 : 1;
    }
}
