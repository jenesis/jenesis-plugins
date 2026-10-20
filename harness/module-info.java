/**
 * Jenesis plugin harness
 *
 * Builds an example project with jenesis-make against plugins installed into a module repository of its own, so that a
 * plugin is tested as the build of a project loads it.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 */
module build.jenesis.plugin.harness {

    requires build.jenesis;

    exports build.jenesis.plugin.harness;
}
