/**
 * Jenesis Avro plugin
 *
 * Generates Java sources from the Avro schemas and protocols of a module, resolving the tool from the repositories
 * of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/66f7e019964cccb9e3fb6b8d2a3734ab32b9a2e53825292da2cf8008f9ff82dd
 */
module build.jenesis.plugin.avro {

    requires build.jenesis;

    exports build.jenesis.plugin.avro;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.avro.AvroFactory;
}
