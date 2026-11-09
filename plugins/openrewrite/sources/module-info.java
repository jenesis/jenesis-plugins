/**
 * Jenesis OpenRewrite plugin
 *
 * Verifies that the recipes a project names leave its Java sources unchanged, or applies them, with OpenRewrite,
 * resolving it from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.openrewrite {

    requires build.jenesis;

    exports build.jenesis.plugin.openrewrite;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openrewrite.OpenRewriteFactory;
}
