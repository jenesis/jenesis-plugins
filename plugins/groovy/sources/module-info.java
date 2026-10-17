/**
 * Jenesis Groovy plugin
 *
 * Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
 * them with Groovydoc, taking the place of the build's own Groovy support for every module that holds a .groovy
 * file, resolving Groovy from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 */
module build.jenesis.plugin.groovy {

    requires build.jenesis;

    exports build.jenesis.plugin.groovy;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.groovy.GroovyFactory;
}
