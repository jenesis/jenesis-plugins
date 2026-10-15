/**
 * Jenesis Avro plugin
 *
 * Generates Java sources from the Avro schemas and protocols of a module, resolving the tool from the repositories
 * of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/9cacdde12663a948b1b5512c35e598b6c6b65e6646b0ba1ff46292b756966330
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/9cacdde12663a948b1b5512c35e598b6c6b65e6646b0ba1ff46292b756966330
 */
module build.jenesis.plugin.avro {

    requires build.jenesis;

    exports build.jenesis.plugin.avro;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.avro.AvroFactory;
}
