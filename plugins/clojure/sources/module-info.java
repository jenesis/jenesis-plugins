/**
 * Jenesis Clojure plugin
 *
 * Compiles the Clojure namespaces of a module ahead of time with the Clojure the module depends on, after javac and
 * against the classes it wrote, and ships their sources beside the classes, for every module that holds a .clj or a
 * .cljc file.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.1 SHA-256/047a160f2aca68c669157aad74a27a28c3c7c1acf696dd672e163706e8f477e0
 */
module build.jenesis.plugin.clojure {

    requires build.jenesis;

    exports build.jenesis.plugin.clojure;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.clojure.ClojureFactory;
}
