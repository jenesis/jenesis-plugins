/**
 * Jenesis ANTLR plugin
 *
 * Generates a lexer and a parser from the ANTLR grammars of a module, resolving ANTLR from the repositories of the
 * build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/65037e467548677cd8969732ab34d91f763cd7b10175ba14b6ebf7b36be3dee7
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/65037e467548677cd8969732ab34d91f763cd7b10175ba14b6ebf7b36be3dee7
 */
module build.jenesis.plugin.antlr {

    requires build.jenesis;

    exports build.jenesis.plugin.antlr;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.antlr.AntlrFactory;
}
