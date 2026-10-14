/**
 * Jenesis Groovy plugin
 *
 * Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
 * them with Groovydoc, taking the place of the build's own Groovy support for every module that holds a .groovy
 * file, resolving Groovy from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/1040b44171cc040da654969e21e63e5a7c5487d7bc5adeb5b53f155c5110be6d
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/1040b44171cc040da654969e21e63e5a7c5487d7bc5adeb5b53f155c5110be6d
 */
module build.jenesis.plugin.groovy {

    requires build.jenesis;

    exports build.jenesis.plugin.groovy;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.groovy.GroovyFactory;
}
