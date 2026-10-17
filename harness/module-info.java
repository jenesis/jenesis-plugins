/**
 * Jenesis plugin harness
 *
 * Builds an example project with jenesis-make against plugins installed into a module repository of its own, so that a
 * plugin is tested as the build of a project loads it.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/f3cec5e4a8926976e7497a290146be1a571399b4601297821e450f2e44e3d0dd
 */
module build.jenesis.plugin.harness {

    requires build.jenesis;

    exports build.jenesis.plugin.harness;
}
