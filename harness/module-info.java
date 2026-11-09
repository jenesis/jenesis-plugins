/**
 * Jenesis plugin harness
 *
 * Builds an example project with jenesis-make against plugins installed into a module repository of its own, so that a
 * plugin is tested as the build of a project loads it.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 */
module build.jenesis.plugin.harness {

    requires build.jenesis;

    exports build.jenesis.plugin.harness;
}
