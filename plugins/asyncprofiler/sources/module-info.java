/**
 * Jenesis async-profiler plugin
 *
 * Profiles a module's tests with async-profiler, attached as an agent to the JVM the tests run in, and writes what it
 * sampled to the reports of the test step, resolving async-profiler from the repositories of the build that loads the
 * plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 */
module build.jenesis.plugin.asyncprofiler {

    requires build.jenesis;

    exports build.jenesis.plugin.asyncprofiler;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;
}
