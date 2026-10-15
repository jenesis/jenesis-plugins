/**
 * Jenesis wsimport plugin
 *
 * Generates a Java web service client from the WSDL descriptions of a module with wsimport, resolving the tool from
 * the repositories of the build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/a3d9a8a361410748f3f02968ac31e8f432da7055bee68eaa030d87544f2891a3
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/a3d9a8a361410748f3f02968ac31e8f432da7055bee68eaa030d87544f2891a3
 */
module build.jenesis.plugin.wsimport {

    requires build.jenesis;

    exports build.jenesis.plugin.wsimport;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.wsimport.WsImportFactory;
}
