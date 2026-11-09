/**
 * Jenesis Clojure plugin
 *
 * Compiles the Clojure namespaces of a module ahead of time with the Clojure the module depends on, after javac and
 * against the classes it wrote, and ships their sources beside the classes, for every module that holds a .clj or a
 * .cljc file.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin build.jenesis/build.jenesis 0.18.2 SHA-256/18494e5406c1a6da0718318cc4037b98ba2c28ce1b1b250580e1b33afd6db7a9
 * @jenesis.pin maven/build.jenesis/build.jenesis/pom 0.18.2 SHA-256/87abf28d19723e17aa24fa0f87aef0a82b9b0e0174cc0d9747a483a6927308a9
 */
module build.jenesis.plugin.clojure {

    requires build.jenesis;

    exports build.jenesis.plugin.clojure;

    provides build.jenesis.LanguageFactory with build.jenesis.plugin.clojure.ClojureFactory;
}
