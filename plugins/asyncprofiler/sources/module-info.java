/**
 * Jenesis async-profiler plugin
 *
 * Profiles a module's tests with async-profiler, attached as an agent to the JVM the tests run in, and writes what it
 * sampled to the reports of the test step, resolving async-profiler from the repositories of the build that loads the
 * plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 * @jenesis.pin build.jenesis/build.jenesis 0.17.0 SHA-256/a875ab473238e7191332698fab64db7b4a4d7a484da6beb313a4a4beb10c60fc
 */
module build.jenesis.plugin.asyncprofiler {

    requires build.jenesis;

    exports build.jenesis.plugin.asyncprofiler;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;
}
