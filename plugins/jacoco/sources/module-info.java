/**
 * Jenesis JaCoCo plugin
 *
 * Measures the code coverage of a module's tests with the JaCoCo agent, attached to the JVM the tests run in, and
 * reports it, failing the build where a limit of its plugin-jacoco.properties is not met, resolving JaCoCo from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/937daedf21aebd722a4805a67494d4069daeeb711eb3f20e3fa55499dda60bf5
 */
module build.jenesis.plugin.jacoco {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.jacoco;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.jacoco.JaCoCoFactory;
}
