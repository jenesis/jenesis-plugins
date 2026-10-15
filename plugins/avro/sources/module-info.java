/**
 * Jenesis Avro plugin
 *
 * Generates Java sources from the Avro schemas and protocols of a module, resolving the tool from the repositories
 * of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/a22c9db778650090c36c1c71f906d25be063e4253ab23a79c4451683467d4541
 */
module build.jenesis.plugin.avro {

    requires build.jenesis;

    exports build.jenesis.plugin.avro;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.avro.AvroFactory;
}
