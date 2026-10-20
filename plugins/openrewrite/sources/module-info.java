/**
 * Jenesis OpenRewrite plugin
 *
 * Verifies that the recipes a project names leave its Java sources unchanged, or applies them, with OpenRewrite,
 * resolving it from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 */
module build.jenesis.plugin.openrewrite {

    requires build.jenesis;

    exports build.jenesis.plugin.openrewrite;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openrewrite.OpenRewriteFactory;
}
