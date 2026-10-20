/**
 * Jenesis Avro plugin
 *
 * Generates Java sources from the Avro schemas and protocols of a module, resolving the tool from the repositories
 * of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 */
module build.jenesis.plugin.avro {

    requires build.jenesis;

    exports build.jenesis.plugin.avro;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.avro.AvroFactory;
}
