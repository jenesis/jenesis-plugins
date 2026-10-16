/**
 * Jenesis Groovy plugin
 *
 * Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
 * them with Groovydoc, taking the place of the build's own Groovy support for every module that holds a .groovy
 * file, resolving Groovy from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/bf8d90a8e3251fcb2519e11793ab87a68f37419f12a6b3275f9dd1d263149c98
 */
module build.jenesis.plugin.groovy {

    requires build.jenesis;

    exports build.jenesis.plugin.groovy;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.groovy.GroovyFactory;
}
