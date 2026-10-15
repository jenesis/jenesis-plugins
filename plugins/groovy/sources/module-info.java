/**
 * Jenesis Groovy plugin
 *
 * Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
 * them with Groovydoc, taking the place of the build's own Groovy support for every module that holds a .groovy
 * file, resolving Groovy from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d7b8ad68b8233d0771d269cd4e18133f8d51b62d1f6b71d50b3973c1cd9b2f9b
 */
module build.jenesis.plugin.groovy {

    requires build.jenesis;

    exports build.jenesis.plugin.groovy;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.groovy.GroovyFactory;
}
