/**
 * Jenesis Asciidoctor plugin
 *
 * Converts the AsciiDoc documents of a project to HTML with AsciidoctorJ, staged with the files that belong to no
 * module, resolving it from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin org.asciidoctor.asciidoctorj.api 3.0.1 SHA-256/6b5d6dc7fc43c5620751dfb880f70822db313067dfa1005f80abd3a29686d8af
 * @jenesis.pin org.asciidoctor/asciidoctorj-api 3.0.1 SHA-256/6b5d6dc7fc43c5620751dfb880f70822db313067dfa1005f80abd3a29686d8af
 */
module build.jenesis.plugin.asciidoctor {

    requires build.jenesis;
    requires static org.asciidoctor.asciidoctorj.api;

    exports build.jenesis.plugin.asciidoctor;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.asciidoctor.AsciidoctorFactory;
}
