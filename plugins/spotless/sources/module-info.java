/**
 * Jenesis Spotless plugin
 *
 * Formats or verifies the files of a module that the sets a project names select, with the Spotless steps each set
 * names, resolving Spotless and the tools of its steps from the repositories of the build that loads the plugin and
 * running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.alias spotless.lib com.diffplug.spotless/spotless-lib
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin com.diffplug.spotless/spotless-lib 4.10.4 SHA-256/efd428f9e4fdf187dc0eca0aee020d639874cd839b458f39566bf105f61502aa
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 * @jenesis.pin maven/com.diffplug.spotless/spotless-lib/pom 4.10.4 SHA-256/75f6d72eb586e18b344c945fdecf90b977933a4818aff0a22f137dcd8bf38626
 */
module build.jenesis.plugin.spotless {

    requires build.jenesis;
    requires static spotless.lib;

    exports build.jenesis.plugin.spotless;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotless.SpotlessFactory;
}
