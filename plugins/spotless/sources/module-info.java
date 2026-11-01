/**
 * Jenesis Spotless plugin
 *
 * Formats or verifies the files of a module that the sets a project names select, with the Spotless steps each set
 * names, resolving Spotless and the tools of its steps from the repositories of the build that loads the plugin and
 * running them in a JVM of their own.
 *
 * @jenesis.release 25
 * @jenesis.alias spotless.lib com.diffplug.spotless/spotless-lib
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin com.diffplug.spotless/spotless-lib 4.10.4 SHA-256/efd428f9e4fdf187dc0eca0aee020d639874cd839b458f39566bf105f61502aa
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 * @jenesis.pin maven/com.diffplug.spotless/spotless-lib/pom 4.10.4 SHA-256/75f6d72eb586e18b344c945fdecf90b977933a4818aff0a22f137dcd8bf38626
 */
module build.jenesis.plugin.spotless {

    requires build.jenesis;
    requires static spotless.lib;

    exports build.jenesis.plugin.spotless;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.spotless.SpotlessFactory;
}
