/**
 * Jenesis JaCoCo plugin
 *
 * Measures the code coverage of a module's tests with the JaCoCo agent, attached to the JVM the tests run in, and
 * reports it, failing the build where a limit of its jacoco.properties is not met, resolving JaCoCo from the
 * repositories of the build that loads the plugin.
 *
 * @jenesis.release 25
 * @jenesis.alias org.jacoco.cli org.jacoco/org.jacoco.cli
 * @jenesis.pin args4j/args4j 2.0.28 SHA-256/11b029a602e787e2bc08eb3b77eda1a4f5e8b263d22e3c5d6220cd5c51f30b18
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/d3f83ea608953c2cb1745b75d34b66a0f0a89dc78944f11430f2cfc9bcbe6437
 * @jenesis.pin maven/args4j/args4j-site/pom 2.0.28 SHA-256/79e151fb939f6c9c8d8f9ae59e5b0e7a862dd5a699b4d4954704e7ce25d4fac8
 * @jenesis.pin maven/args4j/args4j/pom 2.0.28 SHA-256/1f821c93660b7fa573692c305ca97092eab647cefeccfcb3f54771117c3737d3
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.17.0 SHA-256/195b07a6b70f2d5088dc832758f0f84ad8c120928e9f3993ec8437c72f4fae01
 * @jenesis.pin maven/org.jacoco/org.jacoco.build/pom 0.8.15 SHA-256/3a2c89f80c2ab9caf1b403a62da4bcbd909ee52ea53747391267a5cdc968c123
 * @jenesis.pin maven/org.jacoco/org.jacoco.cli/pom 0.8.15 SHA-256/4ac5b19df40287e167afdc6dba956d7cf79a694d748c0b69b53ba115dd3be670
 * @jenesis.pin maven/org.jacoco/org.jacoco.core/pom 0.8.15 SHA-256/7b283b35bb2bda3b827b4da89ce968d257d1d98dd8594d7c8cb5b2f6d8cd9b15
 * @jenesis.pin maven/org.jacoco/org.jacoco.report/pom 0.8.15 SHA-256/a1ea901ca04547f1dc24e75dde104b3a7171ff9d0c2b8f43b3153be09ac3c08a
 * @jenesis.pin maven/org.kohsuke/pom/pom 8 SHA-256/1efa02d3c0f11024319c6e258bb9c76f6255a8fa302a0d8001a253a4be8d7ce3
 * @jenesis.pin maven/org.ow2.asm/asm-bom/pom 9.10.1 SHA-256/709472d69cf785d4b679eee0e6e0575d4b104476ddf35265ef1117e73c129cd4
 * @jenesis.pin maven/org.ow2.asm/asm-commons/pom 9.10.1 SHA-256/ceb2c43f3a273db88af8652d89ed6312f3e91a1a1e0ffc6f7823169278cf795c
 * @jenesis.pin maven/org.ow2.asm/asm-tree/pom 9.10.1 SHA-256/963eebdea6e2e1ff25c9068b64a238e1585c99f8acd455595ac159a7f52b6c41
 * @jenesis.pin maven/org.ow2.asm/asm/pom 9.10.1 SHA-256/e1b1c3832ca3ac0a9e563c405f4c760118b5556a4920daee14058886197e094d
 * @jenesis.pin maven/org.ow2/ow2/pom 1.5.1 SHA-256/321ddbb7ee6fe4f53dea6b4cd6db74154d6bfa42391c1f763b361b9f485acf05
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
