/**
 * Jenesis async-profiler plugin
 *
 * Profiles a module's tests with async-profiler, attached as an agent to the JVM the tests run in, and writes what it
 * sampled to the reports of the test step, resolving async-profiler from the repositories of the build that loads the
 * plugin.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin build.jenesis/build.jenesis 0.18.1 SHA-256/ec0151718921a0755d2461aec78cc58ef0e4bbaa4d69fc056507b28e3a98a5ac
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.asyncprofiler {

    requires build.jenesis;

    exports build.jenesis.plugin.asyncprofiler;

    provides build.jenesis.ObservabilityFactory with build.jenesis.plugin.asyncprofiler.AsyncProfilerFactory;
}
