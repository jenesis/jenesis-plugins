/**
 * Jenesis JaCoCo plugin
 *
 * Measures the code coverage of a module's tests with the JaCoCo agent, attached to the JVM the tests run in, and
 * reports it, failing the build where a limit of its plugin-jacoco.properties is not met, resolving JaCoCo from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.alias org.jacoco.cli org.jacoco/org.jacoco.cli
 * @jenesis.pin args4j/args4j 2.0.28 SHA-256/11b029a602e787e2bc08eb3b77eda1a4f5e8b263d22e3c5d6220cd5c51f30b18
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/d41b8394f702433a8ac7cc9ffae90b988da7fb336de7a6173395749c5154fd41
 * @jenesis.pin org.jacoco/org.jacoco.cli 0.8.15 SHA-256/be5e57a018398b0476e8a3837cc7a0a1e075259ba8a00f349d2b86f854230c23
 * @jenesis.pin org.jacoco/org.jacoco.core 0.8.15 SHA-256/ade1e0fdb4cd80d8537b57c6edb70a1360c47fdc518c20c65314517479bc4d0d
 * @jenesis.pin org.jacoco/org.jacoco.report 0.8.15 SHA-256/66a79e36edc77a1ebf6689e76a976e12ab4f1af515cbd09555840fba775714f4
 * @jenesis.pin org.ow2.asm/asm 9.10.1 SHA-256/ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb
 * @jenesis.pin org.ow2.asm/asm-commons 9.10.1 SHA-256/6d0abefb7cbf972ea16edb37ec14835372505063a45f976ab7ea889ed9497895
 * @jenesis.pin org.ow2.asm/asm-tree 9.10.1 SHA-256/3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b
 */
module build.jenesis.plugin.jacoco {

    requires build.jenesis;
    requires static org.jacoco.cli;
    requires java.xml;

    exports build.jenesis.plugin.jacoco;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.jacoco.JaCoCoFactory;
}
