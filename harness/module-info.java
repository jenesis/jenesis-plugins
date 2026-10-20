/**
 * Jenesis plugin harness
 *
 * Builds an example project with jenesis-make against plugins installed into a module repository of its own, so that a
 * plugin is tested as the build of a project loads it.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/0dfd1715ecb35e8dd398b5fc9ceb49c120aa59e3d6063606882717f41d84b284
 */
module build.jenesis.plugin.harness {

    requires build.jenesis;

    exports build.jenesis.plugin.harness;
}
