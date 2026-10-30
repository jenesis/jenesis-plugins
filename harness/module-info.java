/**
 * Jenesis plugin harness
 *
 * Builds an example project with jenesis-make against plugins installed into a module repository of its own, so that a
 * plugin is tested as the build of a project loads it.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 */
module build.jenesis.plugin.harness {

    requires build.jenesis;

    exports build.jenesis.plugin.harness;
}
