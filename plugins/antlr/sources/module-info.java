/**
 * Jenesis ANTLR plugin
 *
 * Generates a lexer and a parser from the ANTLR grammars of a module, resolving ANTLR from the repositories of the
 * build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/92c37bc23309f2e296b15311c5e54c5eea30803311342f64665d1abc9a1495b8
 */
module build.jenesis.plugin.antlr {

    requires build.jenesis;

    exports build.jenesis.plugin.antlr;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.antlr.AntlrFactory;
}
