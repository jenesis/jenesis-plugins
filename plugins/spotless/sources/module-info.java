/**
 * Jenesis Spotless plugin
 *
 * Formats or verifies the files of a module that the sets a project names select, with the Spotless steps each set
 * names, resolving Spotless and the tools of its steps from the repositories of the build that loads the plugin and
 * running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.alias spotless.lib com.diffplug.spotless/spotless-lib
 * @jenesis.pin build.jenesis 0.18.2
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2
 * @jenesis.pin com.diffplug.spotless/spotless-lib 4.10.4 SHA-256/efd428f9e4fdf187dc0eca0aee020d639874cd839b458f39566bf105f61502aa
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 * @jenesis.pin maven/com.diffplug.spotless/spotless-lib/pom 4.10.4 SHA-256/75f6d72eb586e18b344c945fdecf90b977933a4818aff0a22f137dcd8bf38626
 */
module build.jenesis.plugin.spotless {

    requires build.jenesis;
    requires static spotless.lib;

    exports build.jenesis.plugin.spotless;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotless.SpotlessFactory;
}
