/**
 * Jenesis Groovy plugin
 *
 * Compiles the Groovy sources of a module with groovyc, after javac and against the classes it wrote, and documents
 * them with Groovydoc, taking the place of the build's own Groovy support for every module that holds a .groovy
 * file, resolving Groovy from the repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d5f186f8f022d7f65ff1fb69eebf3020f57fd75d4ac24891a756ae27638fb483
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d5f186f8f022d7f65ff1fb69eebf3020f57fd75d4ac24891a756ae27638fb483
 */
module build.jenesis.plugin.groovy {

    requires build.jenesis;

    exports build.jenesis.plugin.groovy;

    provides build.jenesis.project.LanguageFactory with build.jenesis.plugin.groovy.GroovyFactory;
}
