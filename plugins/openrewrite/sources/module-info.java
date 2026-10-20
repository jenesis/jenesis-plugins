/**
 * Jenesis OpenRewrite plugin
 *
 * Verifies that the recipes a project names leave its Java sources unchanged, or applies them, with OpenRewrite,
 * resolving it from the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/c73dbb0a30f1bae340e0c6adfa433c76980528cf45a7a8836119b038e710886f
 */
module build.jenesis.plugin.openrewrite {

    requires build.jenesis;

    exports build.jenesis.plugin.openrewrite;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.openrewrite.OpenRewriteFactory;
}
