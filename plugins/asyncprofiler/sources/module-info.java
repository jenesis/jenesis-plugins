/**
 * Jenesis async-profiler plugin
 *
 * Profiles a module's tests with async-profiler, attached as an agent to the JVM the tests run in, and writes what it
 * sampled to the reports of the test step, resolving async-profiler from the repositories of the build that loads the
 * plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin build.jenesis/build.jenesis 0.18.0 SHA-256/35a94fc0761110ebcd2fa51b2226a3e7e0a942b14cd69d67cc584bf5f3519f70
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.0 SHA-256/1b46b6334674d86f9ab07933724988c5c4033216b7029534124bff2a0390f5bb
 */
module build.jenesis.plugin.asyncprofiler {

    requires build.jenesis;

    exports build.jenesis.plugin.asyncprofiler;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;
}
