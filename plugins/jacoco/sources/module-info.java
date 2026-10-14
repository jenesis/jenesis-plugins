/**
 * Jenesis JaCoCo plugin
 *
 * Measures the code coverage of a module's tests with the JaCoCo agent, attached to the JVM the tests run in, and
 * reports it, failing the build where a limit of its plugin-jacoco.properties is not met, resolving JaCoCo from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/8fc831caa9caf8f1884f21ff95cc4c9c02885ab67ec00cb718bf054076bb69c3
 */
module build.jenesis.plugin.jacoco {

    requires build.jenesis;
    requires java.xml;

    exports build.jenesis.plugin.jacoco;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.jacoco.JaCoCoFactory;
}
