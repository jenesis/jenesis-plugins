/**
 * Jenesis wsimport plugin
 *
 * Generates a Java web service client from the WSDL descriptions of a module with wsimport, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/fdb94aa305444af31847583b1322105e684b16cee486fd5f1d676302e7bcc495
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/fdb94aa305444af31847583b1322105e684b16cee486fd5f1d676302e7bcc495
 */
module build.jenesis.plugin.wsimport {

    requires build.jenesis;

    exports build.jenesis.plugin.wsimport;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.wsimport.WsImportFactory;
}
