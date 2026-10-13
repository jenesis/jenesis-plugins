/**
 * Jenesis ANTLR plugin
 *
 * Generates a lexer and a parser from the ANTLR grammars of a module, resolving ANTLR from the repositories of the
 * build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/10cbc591bcad8bbbfb17f267a18fc952a45d7189da6a6b9810995442945cfcd4
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/10cbc591bcad8bbbfb17f267a18fc952a45d7189da6a6b9810995442945cfcd4
 */
module build.jenesis.plugin.antlr {

    requires build.jenesis;

    exports build.jenesis.plugin.antlr;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.antlr.AntlrFactory;
}
