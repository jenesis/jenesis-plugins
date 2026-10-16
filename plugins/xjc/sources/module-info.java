/**
 * Jenesis XJC plugin
 *
 * Generates Java sources from the XML schemas of a module with XJC, resolving the tool from the repositories of the
 * build that loads the plugin and running it in a JVM of its own.
 *
 * @jenesis.release 25
 * @jenesis.pin build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 * @jenesis.pin build.jenesis/build.jenesis 0.16.0 SHA-256/e40b9f6e68188ee3944e15847498c57383c0bedf689b721e6de44c21fd27c45a
 * @jenesis.pin com.sun.istack/istack-commons-runtime 4.1.2 SHA-256/7fd6792361f4dd00f8c56af4a20cecc0066deea4a8f3dec38348af23fc2296ee
 * @jenesis.pin com.sun.istack/istack-commons-tools 4.1.2 SHA-256/85b4fe7ad6fdfc64a586133f039d3de7b51db2c8111a1aa98a267891e27f386f
 * @jenesis.pin com.sun.xml.bind.external/relaxng-datatype 4.0.9 SHA-256/89933721c2dc654049ebea84a33e2f3d36e918e5871ccb6f08df1960779c5cb9
 * @jenesis.pin com.sun.xml.bind.external/rngom 4.0.9 SHA-256/b3daa8902535f33ff90d66c2c327b95b3827f0b36a31f9a2c1094d7b82263e1b
 * @jenesis.pin com.sun.xml.dtd-parser/dtd-parser 1.5.1 SHA-256/fd07c1b528e8649e956bd4c759a63badb79e8076260b5c23bdf37f2b11d06711
 * @jenesis.pin jakarta.activation/jakarta.activation-api 2.1.4 SHA-256/c9db52100ce6c8aac95cc39075f95720d2e561b11f8051b81c121ad4effd7004
 * @jenesis.pin jakarta.xml.bind/jakarta.xml.bind-api 4.0.5 SHA-256/5e489b6c874c4119e003ff1403db523ee3a8959ec499f3de29e77245efccf216
 * @jenesis.pin org.eclipse.angus/angus-activation 2.0.3 SHA-256/a6bd35c538cf90fff941ad6258c40c08fca0b5c9c3f536c657114f27ce0527a7
 * @jenesis.pin org.glassfish.jaxb.xjc 4.0.9 SHA-256/5556b3d06a4af1bbcd9d68ef80dbaa04bdc686c246c89b3d98db4acf7f8faf90
 * @jenesis.pin org.glassfish.jaxb/codemodel 4.0.9 SHA-256/12c9ba587b85af187a71b25c531c9929bd1a53da334d102e485468a493a1f1d7
 * @jenesis.pin org.glassfish.jaxb/jaxb-core 4.0.9 SHA-256/6b014df25d22c8446cc2df0a378c5b2137747f197a731d99eb24c756b26f7e8d
 * @jenesis.pin org.glassfish.jaxb/jaxb-xjc 4.0.9 SHA-256/5556b3d06a4af1bbcd9d68ef80dbaa04bdc686c246c89b3d98db4acf7f8faf90
 * @jenesis.pin org.glassfish.jaxb/txw2 4.0.9 SHA-256/d6343e5945d87266ca4e8974a0a2a876e1bd147a00fb65ed3f2e1a3a00f3a82a
 * @jenesis.pin org.glassfish.jaxb/xsom 4.0.9 SHA-256/be584192033acb7424e0edb283e18f83ae681a46c9e3218fb4377f4df870ed07
 */
module build.jenesis.plugin.xjc {

    requires build.jenesis;
    requires static org.glassfish.jaxb.xjc;

    exports build.jenesis.plugin.xjc;

    provides build.jenesis.BuildModuleFactory with build.jenesis.plugin.xjc.XjcFactory;
}
