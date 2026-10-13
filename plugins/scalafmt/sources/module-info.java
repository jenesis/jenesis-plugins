/**
 * Jenesis scalafmt plugin
 *
 * Checks and formats the Scala sources of a module with scalafmt, as its .scalafmt.conf says. The provider named
 * check reports what is not formatted and the one named format rewrites or verifies it; both resolve scalafmt from
 * the repositories of the build that loads the plugin and run it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/3ec1738a274f9fc65dc247dede756b533453e1c8d6cbac762effe4be7c48ce8a
 */
module build.jenesis.plugin.scalafmt {

    requires build.jenesis;

    exports build.jenesis.plugin.scalafmt;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.scalafmt.ScalafmtFactory,
            build.jenesis.plugin.scalafmt.ScalafmtFormatFactory;
}
