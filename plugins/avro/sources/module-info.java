/**
 * Jenesis Avro plugin
 *
 * Generates Java sources from the Avro schemas and protocols of a module, resolving the tool from the repositories
 * of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 */
module build.jenesis.plugin.avro {

    requires build.jenesis;

    exports build.jenesis.plugin.avro;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.avro.AvroFactory;
}
