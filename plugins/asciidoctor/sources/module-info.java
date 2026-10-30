/**
 * Jenesis Asciidoctor plugin
 *
 * Converts the AsciiDoc documents of a project to HTML with AsciidoctorJ, staged with the files that belong to no
 * module, resolving it from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/org.asciidoctor/asciidoctorj-api/pom 3.0.1 SHA-256/86ac8d45809911785b090f9cce6cb008193e91852da7fe68e32b54708715fc43
 * @jenesis.pin org.asciidoctor.asciidoctorj.api 3.0.1 SHA-256/6b5d6dc7fc43c5620751dfb880f70822db313067dfa1005f80abd3a29686d8af
 * @jenesis.pin org.asciidoctor/asciidoctorj-api 3.0.1 SHA-256/6b5d6dc7fc43c5620751dfb880f70822db313067dfa1005f80abd3a29686d8af
 */
module build.jenesis.plugin.asciidoctor {

    requires build.jenesis;
    requires static org.asciidoctor.asciidoctorj.api;

    exports build.jenesis.plugin.asciidoctor;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.asciidoctor.AsciidoctorFactory;
}
