package build.jenesis.plugin.spotless.test;

import module java.base;
import module org.junit.jupiter.api;
import build.jenesis.BuildExecutor;
import build.jenesis.BuildExecutorCache;
import build.jenesis.BuildExecutorCallback;
import build.jenesis.BuildStep;
import build.jenesis.BuildStepHashFunction;
import build.jenesis.Environment;
import build.jenesis.HashDigestFunction;
import build.jenesis.Pinning;
import build.jenesis.SequencedProperties;
import build.jenesis.maven.MavenDefaultRepository;
import build.jenesis.maven.MavenPomResolver;
import build.jenesis.plugin.spotless.SpotlessModule;
import build.jenesis.plugin.spotless.SpotlessSet;
import build.jenesis.plugin.spotless.SpotlessStep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpotlessModuleRunTest {

    private static final String PINS = """
            spotless\\:flexmark/maven/com.ibm.icu/icu4j 72.1 SHA-256/3df572b240a68d13b5cd778ad2393e885d26411434cd8f098ac5987ea2e64ce3
            spotless\\:flexmark/maven/com.ibm.icu/icu4j/pom/72.1 72.1 SHA-256/3def2b29af4a19ad805cb153056925a89a903f92fbee1adee12ed2fc14c44ee8
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-core 1.0.10 SHA-256/3e6fd2250d833d500b7cd48b7a896700d0c33bd9f77a219e820493b01566eda3
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-core/pom/1.0.10 1.0.10 SHA-256/7c42b8cc584d912e59a445f3eb6d7bbee3381156119f68b04fcfa3cd0621f2e9
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-parent/pom/1.0.10 1.0.10 SHA-256/77822d6a90985343e4c790af95c12e9ab54a30238a54594e0b79dc811964ef69
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-pdfbox 1.0.10 SHA-256/7de90df1b3ecf84e6f0daf808d724c11142007a2f22bff1936479bf17251d31a
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-pdfbox/pom/1.0.10 1.0.10 SHA-256/b3511cd65cea17688cf13ff31ec570d7408b81bfddd52faeccaeed4550cc1715
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-rtl-support 1.0.10 SHA-256/5675c0b56d613b15446f9d0922d89781af48f05331326a87bdb3d1775c703215
            spotless\\:flexmark/maven/com.openhtmltopdf/openhtmltopdf-rtl-support/pom/1.0.10 1.0.10 SHA-256/c3a3f9ab51337a40a4086eafd4d23fc5a519359ae95eff696ae52abbe4f71cd2
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark 0.64.8 SHA-256/3133878d108f0e1964d7d427f379a7433e1b71a45f9f3c1fab7ed0ac0301d3b0
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-all 0.64.8 SHA-256/4b89d89af1251cb404e5f025714b3d52f60fb090fe546057e4640c7f30c05726
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-all/pom/0.64.8 0.64.8 SHA-256/030e992d3fd96c7e99d208988328e1157b8c6054863f03067e29445cf0b732dc
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-abbreviation 0.64.8 SHA-256/4f5d2dbac5775dcd32c1a6324618687883f0b287b3e243b836c7e8560be0bbea
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-abbreviation/pom/0.64.8 0.64.8 SHA-256/7fad85478225e271fb6b9b89b9288e08f450a6e1614616b81b4f4037f89043d8
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-admonition 0.64.8 SHA-256/a6823ea45a5810ac1e97f545bdec83710797e6fcf6ca4eeefbe930e57a9a043d
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-admonition/pom/0.64.8 0.64.8 SHA-256/fae547d66165a1bfd681b63ec21222ebbc48a80874cf62f912a06fc3805d21a7
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-anchorlink 0.64.8 SHA-256/36d6849df8548a71b0f87eb97d3690864b960a8e4979fedf865418c8903453a6
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-anchorlink/pom/0.64.8 0.64.8 SHA-256/a0b3de04e43620beb22f8d9e1b9cf244d3c68bd679e1aa3f80ae6332d36da8bb
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-aside 0.64.8 SHA-256/feba989fc8207b3d67df517feaedadb2496c35d45827b3f0ab6b49d887743884
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-aside/pom/0.64.8 0.64.8 SHA-256/8f9b024b8e03fd8042446b58a0593e445f4c3a69f3dfe5f333bd9d5a89046505
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-attributes 0.64.8 SHA-256/4db42f300ec5490ff79e7d4e44000088e08d83d14096e7b3a9fd63119589ed77
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-attributes/pom/0.64.8 0.64.8 SHA-256/156c8c851378311ca3778fa7c8012cf4940a8285362d47b418188632db85f25f
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-autolink 0.64.8 SHA-256/4850d6f895ec3f2932635829d74ff9a2fb00918965bb91c000ab497d603e11b8
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-autolink/pom/0.64.8 0.64.8 SHA-256/a4084cd7c370f2db566d182c5839dd910767011062eccc51cdbd07b5129bd64a
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-definition 0.64.8 SHA-256/d8dac5e0732c405702c932b3c495a0fc413224335333ce6009d830734e31e0e6
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-definition/pom/0.64.8 0.64.8 SHA-256/38b8cccfcb221788fea2306b54c71f1cfdfddeda4595450885f64fa75c4e4c00
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-emoji 0.64.8 SHA-256/012d2db51463afd84702627e493fedce42eb7deb1bae309ddcefd181ab283cbe
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-emoji/pom/0.64.8 0.64.8 SHA-256/375752600eca50639a7bd1f21672b6f36b0315b602a0e70a0f3a0645967140a9
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-enumerated-reference 0.64.8 SHA-256/e2f858bedf356e24db9d31c15cc57a2b14071074a850f8bbfdd8c632acf05942
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-enumerated-reference/pom/0.64.8 0.64.8 SHA-256/4f3d7d5bdb08eb31162a5c560c835e6d9ce2f005cac275758681071557f8368f
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-escaped-character 0.64.8 SHA-256/01009a3473c007bddecfe3cd3979282117b82d49812dcf347cebfcee47023712
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-escaped-character/pom/0.64.8 0.64.8 SHA-256/31f2d3b72f8dd4067e06a209cf1bb60779506ff72edeb12ffcd42c272d9152f0
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-footnotes 0.64.8 SHA-256/ae5244254e270ae6479803870fa791fe0ae12be242f72e47979412a8ae16df06
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-footnotes/pom/0.64.8 0.64.8 SHA-256/bd1857ff2dad1d927e0abba71be4b0a063af636c9aaea58619c3fe6390df59a2
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-issues 0.64.8 SHA-256/0c332dcbeb703179dc88df368cdb71b3a5f486ceb62488cf8a816f7bebe71558
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-issues/pom/0.64.8 0.64.8 SHA-256/07bd3c885e863e186b8a1577750c26bb27862f375beac26ec8359f1ce9587b27
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-strikethrough 0.64.8 SHA-256/fc6cad026fbd036b3ffd0448264360a5f1b572756ef3427ff022715b7fc5f1c3
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-strikethrough/pom/0.64.8 0.64.8 SHA-256/dbd892340d1637239a7f66c5e5219210b7cd417d1fd771ce93a965b03f718288
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-tasklist 0.64.8 SHA-256/c86754ba8f0ffe997fa448da2a6ab2cfdbea9b381884cfed6f74bf513b77f5a9
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-tasklist/pom/0.64.8 0.64.8 SHA-256/82b4ae194ebc72a0837c5859f65a9ba839b186fdfac56da898b4088e7ac3c8b8
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-users 0.64.8 SHA-256/996f2e361c5918a5724a66766fe803a14c397aa9f7d0f75220ec1b9cae0b0c06
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gfm-users/pom/0.64.8 0.64.8 SHA-256/b706691461a91be9d9950389a94ff33efb278d92ebb489e46a917f94f52bc5ab
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gitlab 0.64.8 SHA-256/36dbd3e3253bc16f09c9773bb4c9b4e3476769c0f53430380b12ac1a47d59983
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-gitlab/pom/0.64.8 0.64.8 SHA-256/d5735fd1057186541a02bebfa176f6cfc846595755b9dd4c70685c5d4e3be733
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-ins 0.64.8 SHA-256/5b61b83c31cb0bd3273cbf12aed3887b63b8bdc6dfa17b03ecf479cf1ce176bd
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-ins/pom/0.64.8 0.64.8 SHA-256/4cf640cc39e506dcc2bc5ed293464e89eb7dbb75fc72186582b7b633cce25b21
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-jekyll-front-matter 0.64.8 SHA-256/b11e4e61dbd4cb3e37ae325b2f9cd16dbf78f86bfd623d952bf0dfd6d685c46a
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-jekyll-front-matter/pom/0.64.8 0.64.8 SHA-256/2cf8014c2157f40803514cfe8865c146bd4e0036459414b808a2945d854c7547
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-jekyll-tag 0.64.8 SHA-256/234de845decddf253f7bebd5400e0c6c130ea5f89887c0f71958e033bcdc233e
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-jekyll-tag/pom/0.64.8 0.64.8 SHA-256/b8078686a0111c6f92f5d4c08619b89bd69973add99abc03584ad933d29762d1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-macros 0.64.8 SHA-256/bc846058d8a2bd66e7c4193fde8e194db089761e2534ce017ae99ac271fc8d3e
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-macros/pom/0.64.8 0.64.8 SHA-256/5bafcd94377a4793b108fc40168bd67f035cb3bdd0e16a4fb264e2c944d77728
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-media-tags 0.64.8 SHA-256/8f2e41be52f9633aeaf36ce8d0ed222e6cc37ad9e0e6c943a0d304b2d071f600
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-media-tags/pom/0.64.8 0.64.8 SHA-256/47ca20a5789a0bf07deadd3538d61e5d34b852a266a9197759f00faac8470269
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-resizable-image 0.64.8 SHA-256/e3af2fd6d82e4cd5adf66a1b5cd68ae0c2cef7c7bccb2f2010c2eabe88eafbee
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-resizable-image/pom/0.64.8 0.64.8 SHA-256/9d87b1b4ab5f9ef486648d8410db6947a56e238a8701456742507b78c8397db7
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-superscript 0.64.8 SHA-256/457933324d1ca9f283bc15a1842777c6df0385f318fcceade4de41621395e402
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-superscript/pom/0.64.8 0.64.8 SHA-256/f897ef3aeb6432c37fb8880172053116d11af38ae4709c908aa70154cd261a1e
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-tables 0.64.8 SHA-256/cdf82d26e112e96ade7e370138aeb16920b7e2d411d20c6c15cb1acfacd1ef1f
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-tables/pom/0.64.8 0.64.8 SHA-256/f8b9ee8741591312fc2353e47cc8cc6d922e7764489d83abad7d15f33537dcf5
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-toc 0.64.8 SHA-256/bf54f1bb7bca720341db0e322691725ab8e022b2ca269ad1e4f3705c16d37911
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-toc/pom/0.64.8 0.64.8 SHA-256/7f96484a9cefa5cf49ef1444052409978325d4a98b913ec47a11d8c4cd46f873
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-typographic 0.64.8 SHA-256/61561a811c716843fee3cb7f18f00783c3a01c30a6ce653446738bcc900aeb13
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-typographic/pom/0.64.8 0.64.8 SHA-256/de24bdcc73fb88eaedc4d5fd2eca93f00830297a870e799fa103192e13437c9c
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-wikilink 0.64.8 SHA-256/f32ef779eefb4c3b80d5bebf3c4b7ad7206fc46e083a5901cf51db7b4535ff79
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-wikilink/pom/0.64.8 0.64.8 SHA-256/56bfacea6133f64f85df422bea263ff5448a3560b17d2217c3c2d2bc944fb5ba
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-xwiki-macros 0.64.8 SHA-256/f986ce5aed53fa8db20fdf2a98e36aa5cafefb496516c8c031b84bfb081a5672
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-xwiki-macros/pom/0.64.8 0.64.8 SHA-256/c0f17fa1ca003da77b6c9458bf4c709618f64eec7ca06eb2164eb3a741454a07
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-yaml-front-matter 0.64.8 SHA-256/6c60b85db4cc851301c1a52d712dc0909c5d2da9bd17ebae4d97a3a03afe22ee
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-yaml-front-matter/pom/0.64.8 0.64.8 SHA-256/f11291fcb7db8222101ef8a9009e038b047a5d2fcf95674a51cfdc35111a6b98
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-youtube-embedded 0.64.8 SHA-256/6fe195afee752df502ad4295ab0c4ec8ed79f5b0fdc5ff85b98c8bc512f0f11a
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-ext-youtube-embedded/pom/0.64.8 0.64.8 SHA-256/84d108196bb45f84a02e429ddbc33fd8101c665346843c333e6508a2de252790
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-html2md-converter 0.64.8 SHA-256/3f24a90a8fedd6708f06be72bbc9f5f5211abc49c35ee95918ce7b0b41651e62
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-html2md-converter/pom/0.64.8 0.64.8 SHA-256/52baca4455c978f4ef0b434ada839f987432f49690152ef20e4c6f1e661bed30
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-java/pom/0.64.8 0.64.8 SHA-256/407bf752f3762aa908c5fc4d664b893994d1b783aa20a2966a43f86c155eb433
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-jira-converter 0.64.8 SHA-256/ccc1c3a9659a104b75a53a2321e9c1d4cedb05acd867d4366d8b8e1647bd37dd
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-jira-converter/pom/0.64.8 0.64.8 SHA-256/68eebaf15436edec7bf0330842070dd223aa71741c697d7bc9029511d946e082
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-pdf-converter 0.64.8 SHA-256/2efacba4c37d4736f00fc35cd5685f8f00b83db9b3c80ca5602eda2fb9d2511c
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-pdf-converter/pom/0.64.8 0.64.8 SHA-256/af4f482c08a27da98645228806ea7586e5457322c72ea8ad7069934f51fe40c1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-profile-pegdown 0.64.8 SHA-256/1ef21e536299c25c040f26ff9b229fdeef24c3661c6c371bc2b9462227652426
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-profile-pegdown/pom/0.64.8 0.64.8 SHA-256/7ed3387694c91f5f13c107bef9246bf1b1a791e96c15ab31fa43c7eaf1c50b91
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util 0.64.8 SHA-256/1e24c891dac9532bb722be7122c7026fb47868c3de36c02400eed8e9ad535727
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-ast 0.64.8 SHA-256/a5342f644c6a5f37d502f225c3a0cb699c263621c82e720d9c08eba5a9f3b8d3
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-ast/pom/0.64.8 0.64.8 SHA-256/33920f703f27690feaabb08a434ecef20d3bddba4c6dc17d9b51180025072133
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-builder 0.64.8 SHA-256/afb9c88d9f652451e75f1ab069ad7ddd791d712eb211c51d8d2a83494240875d
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-builder/pom/0.64.8 0.64.8 SHA-256/1112ad85d98b874b497954fa7b2491f405dbe01da4b28d31f5754ccc5b6a2fa1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-collection 0.64.8 SHA-256/031ff01408d0f54c6235db18d451dc6e6b15b859b0ec1e40ff3429a82375082a
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-collection/pom/0.64.8 0.64.8 SHA-256/c472c240dee615b92a21241c1b42f39bfe3865895c30286e95623cd9405e726b
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-data 0.64.8 SHA-256/83ceb5989ab0205054bf1b42446f55cb6da14c10aa7c2bda05d6a2504670f353
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-data/pom/0.64.8 0.64.8 SHA-256/dba97509af7abd6c1204dc0b3f62b4869cf1a8e37c9e9d66356e10ab24e485f1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-dependency 0.64.8 SHA-256/e41adb5e8144699b61b78d50eb004eeb7de84f90c4c63626609e6cee5ea28b42
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-dependency/pom/0.64.8 0.64.8 SHA-256/d0490ffb44edf34a50a6b9fa9eb5c3dd73d3f7078e1f4de8aee5219b22144556
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-format 0.64.8 SHA-256/6c9ac87686474edf0117b3f838d9dcb3b965fa72be4998d2177b598013679f73
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-format/pom/0.64.8 0.64.8 SHA-256/abe07a00ffeb6ebf8be83a02cce4d27691e7454951be76adb5e57daa626248e1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-html 0.64.8 SHA-256/5b19e6506aff7913c74ad725af7c2b8a9f23958e6324bf0710c5a645a4663017
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-html/pom/0.64.8 0.64.8 SHA-256/412393f0352df8699683829a6910ce217ed1b52c9ab9c619220d4e33d5b49fba
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-misc 0.64.8 SHA-256/74472ca81a9080e51ea1a4f11c270898e3d6c242c92275fab47eb993806c8f2b
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-misc/pom/0.64.8 0.64.8 SHA-256/ee4cc1699660a3433e609032db46abfb4c934e51a63f7a5839ec1f8b6a95c0e1
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-options 0.64.8 SHA-256/2489f41e9caf1d9f8061833b311b25403925cbb631fff2b8c069192804d6fc88
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-options/pom/0.64.8 0.64.8 SHA-256/180e43378771cdc56194386043c5d7f43b2aec45bd37d089c26615d00b398b60
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-sequence 0.64.8 SHA-256/6e22976fe4e9b8de94fd529991ecb2d79cc482aad94985367470a93be1d82631
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-sequence/pom/0.64.8 0.64.8 SHA-256/6ceab4120f6855bb7d21e5720e156ea20a9691e51137d6468f4b9067352f83ef
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-visitor 0.64.8 SHA-256/d9b3002a833a078c45e139568b306dab0ac117ff9046c6a1f9e180c0154563fc
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util-visitor/pom/0.64.8 0.64.8 SHA-256/fa45a2ca0fae47bfa7c3a197c22ba17b64223598594c38a6ce59273e5390c176
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-util/pom/0.64.8 0.64.8 SHA-256/838120f6a4e8cd0b9bec18867029d78f114d577cdcbcea6946beb97ec98ce7f8
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-youtrack-converter 0.64.8 SHA-256/06e556491d36876c1d7eb66b1300263a4e774c1541b9cffc6946f541df38a9b6
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark-youtrack-converter/pom/0.64.8 0.64.8 SHA-256/d2cea694659a6a2fb8623fa112333cca9b7cb5dbf95c0735c5564087ad3137b9
            spotless\\:flexmark/maven/com.vladsch.flexmark/flexmark/pom/0.64.8 0.64.8 SHA-256/c303bb71ca39b528806a79f8ef02da4a94f1925b75d41877a2344db1bc225a92
            spotless\\:flexmark/maven/commons-logging/commons-logging 1.2 SHA-256/daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636
            spotless\\:flexmark/maven/commons-logging/commons-logging/pom/1.2 1.2 SHA-256/c91ab5aa570d86f6fd07cc158ec6bc2c50080402972ee9179fe24100739fbb20
            spotless\\:flexmark/maven/de.rototor.pdfbox/graphics2d 0.32 SHA-256/37f8f387395f96c214ac44f7475c7a2e1f832dfc1de289a3610e0ffbf728f679
            spotless\\:flexmark/maven/de.rototor.pdfbox/graphics2d/pom/0.32 0.32 SHA-256/73591fccf3f53c0149d538ad438e3dd10cbdbb97a5f9389bfebc2d2263410714
            spotless\\:flexmark/maven/de.rototor.pdfbox/pdfboxgraphics2d-parent/pom/0.32 0.32 SHA-256/1e87b8be8ca2c7deed3acc14c4c97b634883fb1370cedc0c19a9fa6771316864
            spotless\\:flexmark/maven/org.apache.commons/commons-parent/pom/34 34 SHA-256/3a2e69d06d641d1f3b293126dc9e2e4ea6563bf8c36c87e0ab6fa4292d04b79c
            spotless\\:flexmark/maven/org.apache.pdfbox/fontbox 2.0.24 SHA-256/2e8c0a569a90b04734fbc0c805d77f4ec03f98c11f5705055ccd7718c1953d68
            spotless\\:flexmark/maven/org.apache.pdfbox/fontbox/pom/2.0.24 2.0.24 SHA-256/fa55f75da9a21d3f28b273aae4dd0196f8195897976f068906bb4bf133a43b2f
            spotless\\:flexmark/maven/org.apache.pdfbox/pdfbox 2.0.24 SHA-256/3c2c0553ec0e7533c490b4c952e1af113621de5275af6e380e11d0d9a0a4f3d6
            spotless\\:flexmark/maven/org.apache.pdfbox/pdfbox-parent/pom/2.0.24 2.0.24 SHA-256/d949c2e03f023763ff531271f972bcc0fb4b8e9067f79f95504e301a78913858
            spotless\\:flexmark/maven/org.apache.pdfbox/pdfbox/pom/2.0.24 2.0.24 SHA-256/2bf6d93c4c9e9220132bda81f603e469cb435829349c287ecf3a724c31092c3e
            spotless\\:flexmark/maven/org.apache.pdfbox/xmpbox 2.0.24 SHA-256/27383df2285b9e228c39f2c755adeebbef774793d33c4f20f0dc99e9ebaaf673
            spotless\\:flexmark/maven/org.apache.pdfbox/xmpbox/pom/2.0.24 2.0.24 SHA-256/f7347b57d9a7cb4546bd8e4fa8956e616fdecd46a4d2590d1a8beb847a54564c
            spotless\\:flexmark/maven/org.apache/apache/pom/13 13 SHA-256/ff513db0361fd41237bef4784968bc15aae478d4ec0a9496f811072ccaf3841d
            spotless\\:flexmark/maven/org.apache/apache/pom/19 19 SHA-256/91f7a33096ea69bac2cbaf6d01feb934cac002c48d8c8cfa9c240b40f1ec21df
            spotless\\:flexmark/maven/org.jetbrains/annotations 24.0.1 SHA-256/61666dbce7e42e6c85b43c04fcfb8293a21dcb55b3c80e869270ce42c01a6b35
            spotless\\:flexmark/maven/org.jetbrains/annotations/pom/24.0.1 24.0.1 SHA-256/99bede29c0331c1952eee04bf9978de535a90c97e2defffa5e009335171925b0
            spotless\\:flexmark/maven/org.jsoup/jsoup 1.15.4 SHA-256/006830d0797710408de5589c18d864343c0ef1a3a0c603c9cb138c943b67d9a4
            spotless\\:flexmark/maven/org.jsoup/jsoup/pom/1.15.4 1.15.4 SHA-256/7212890b72c0b860e0225856be7413e78ebd3ecd3ecb8567dde6aac6273dfb89
            spotless\\:flexmark/maven/org.nibor.autolink/autolink 0.6.0 SHA-256/a80be030f6386f18111cad9161c0b6983157352a1b59a59e6002172f0d321c04
            spotless\\:flexmark/maven/org.nibor.autolink/autolink/pom/0.6.0 0.6.0 SHA-256/916755647a34ccb367e11834d28380198c834adfcf660e0d983e375b8f5c28f2
            spotless\\:googlejavaformat/maven/com.google.errorprone/error_prone_annotations 2.47.0 SHA-256/5364bc6f22e72e98195e406a58d3ba1c09ffa11dea0729592cb870dc2de4056d
            spotless\\:googlejavaformat/maven/com.google.errorprone/error_prone_annotations/pom/2.47.0 2.47.0 SHA-256/d80c889a4a6f711f6945fbee79e05ec247b178a567e9d5abf58eb26ebf0a0752
            spotless\\:googlejavaformat/maven/com.google.errorprone/error_prone_parent/pom/2.47.0 2.47.0 SHA-256/2368a990c7a63095e1d0d44459d5a4092f0eb31f8562bd12cdf0e1c877b6a685
            spotless\\:googlejavaformat/maven/com.google.googlejavaformat/google-java-format 2.0.0 SHA-256/7a03f28f7379dcb32e5fc5a6ded62524c4a31e9616e3b477a2f71d7124c5ccad
            spotless\\:googlejavaformat/maven/com.google.googlejavaformat/google-java-format-parent/pom/2.0.0 2.0.0 SHA-256/d45988dad2407693183de037d752c1935fef017a5b81a0c330c55a1b2d7fb5c1
            spotless\\:googlejavaformat/maven/com.google.googlejavaformat/google-java-format/pom/2.0.0 2.0.0 SHA-256/9d0102894b3cd5faa580f1dddedfb22096facfb149a53eb615169e3e2a7ebbd6
            spotless\\:googlejavaformat/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            spotless\\:googlejavaformat/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            spotless\\:googlejavaformat/maven/com.google.guava/guava 33.6.0-jre SHA-256/dc573e1fca4fd5454f4a5fd3d7da2df03002876a4175bafc14a95980dd7713b3
            spotless\\:googlejavaformat/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            spotless\\:googlejavaformat/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            spotless\\:googlejavaformat/maven/com.google.guava/guava-parent/pom/33.6.0-jre 33.6.0-jre SHA-256/374bd31f61b1cf612bee9ab2e4d70bbdf77dd85a49b431f809d4fbdc901f2dd4
            spotless\\:googlejavaformat/maven/com.google.guava/guava/pom/33.6.0-jre 33.6.0-jre SHA-256/b45b78944280054311c53da9270809fb40dcabc408ce66ce55730099b4f57d32
            spotless\\:googlejavaformat/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            spotless\\:googlejavaformat/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            spotless\\:googlejavaformat/maven/com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
            spotless\\:googlejavaformat/maven/com.google.j2objc/j2objc-annotations/pom/3.1 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
            spotless\\:googlejavaformat/maven/org.commonmark/commonmark 0.29.0 SHA-256/4c9b0fa481e36d7176f38e572c134656424cf9601bc190855d0f89478df901c6
            spotless\\:googlejavaformat/maven/org.commonmark/commonmark-ext-gfm-tables 0.29.0 SHA-256/f263d284c0795b0ddd6071d4fa969f67d460644c20828e51957ad5aec0d75612
            spotless\\:googlejavaformat/maven/org.commonmark/commonmark-ext-gfm-tables/pom/0.29.0 0.29.0 SHA-256/20a67830ba96d37826eb7f3354a9f390caa27e1f66af67029ff69ef7c2f33bd1
            spotless\\:googlejavaformat/maven/org.commonmark/commonmark-parent/pom/0.29.0 0.29.0 SHA-256/118b96f673388d28bc6c33519bb9b19770be59f58935f230bca9eb0b082f6e19
            spotless\\:googlejavaformat/maven/org.commonmark/commonmark/pom/0.29.0 0.29.0 SHA-256/dbd3979a45e5c6b4dcb57c0e71961c3decdb7e089f5b8f5b93754c2c8e178973
            spotless\\:googlejavaformat/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            spotless\\:googlejavaformat/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            spotless\\:googlejavaformat/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-annotations 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.22 2.22 SHA-256/38c3ba52a0eade3e3eacc5b13499d56e1038ccbad8ec4bb79426dc21432b46d0
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-core 2.22.4 SHA-256/2d30c375cddea9657a1e1ef97547a2694b663b355facb4de53a097782b312d1a
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-core/pom/2.22.4 2.22.4 SHA-256/371b6859b58c3fc1c0823fe66e1e6d6ab251ba99b26f7dce883bbb7c43906c21
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-databind 2.22.4 SHA-256/d4cbf8947087f305869826801913abdc50735a1fab2074a1993e7bc67ca974a3
            spotless\\:jacksonjson/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.22.4 2.22.4 SHA-256/81867c3c99c0e014d133554f2d30cdad78ec58cf1b56087088204e1e5eb34baa
            spotless\\:jacksonjson/maven/com.fasterxml.jackson/jackson-base/pom/2.22.4 2.22.4 SHA-256/51b5cc57df78f50a43210e577075f68399e31dde7150455e179d249e637582a1
            spotless\\:jacksonjson/maven/com.fasterxml.jackson/jackson-bom/pom/2.22.4 2.22.4 SHA-256/a3c00688a7bf8e128451137b68f90d9137957a372756083d74555a47241d1636
            spotless\\:jacksonjson/maven/com.fasterxml.jackson/jackson-parent/pom/2.22 2.22 SHA-256/7d45a7efb69df207bed7cc54b87a0bb012f10952c095570a273b49485da0d129
            spotless\\:jacksonjson/maven/com.fasterxml/oss-parent/pom/79 79 SHA-256/9206b7dd67fb138036576c3dfca947a2a8f3e2c4d2d9c1f2d1dea519b3a4dae1
            spotless\\:jacksonjson/maven/org.junit/junit-bom/pom/5.14.3 5.14.3 SHA-256/08a02856e487c9357f9b29e38745f8ae805848111e72d15aad0352338f1632e1
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-annotations 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.22 2.22 SHA-256/38c3ba52a0eade3e3eacc5b13499d56e1038ccbad8ec4bb79426dc21432b46d0
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-core 2.22.4 SHA-256/2d30c375cddea9657a1e1ef97547a2694b663b355facb4de53a097782b312d1a
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-core/pom/2.22.4 2.22.4 SHA-256/371b6859b58c3fc1c0823fe66e1e6d6ab251ba99b26f7dce883bbb7c43906c21
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-databind 2.22.4 SHA-256/d4cbf8947087f305869826801913abdc50735a1fab2074a1993e7bc67ca974a3
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.22.4 2.22.4 SHA-256/81867c3c99c0e014d133554f2d30cdad78ec58cf1b56087088204e1e5eb34baa
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml 2.22.4 SHA-256/2f55e2b864f6354b24d4bcccd488f7c4a3fce4977f8c8dbad07242a68e11dde9
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml/pom/2.22.4 2.22.4 SHA-256/cb7695e43f99ae8bb240f1e4301357c7b1e7f55b0a76e99bc959d51aeded5e41
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson.dataformat/jackson-dataformats-text/pom/2.22.4 2.22.4 SHA-256/57ca2b45f3b4015ee42e8bcea4b886baa8569eab4bc34654cbf3410bcbb7a8fe
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson/jackson-base/pom/2.22.4 2.22.4 SHA-256/51b5cc57df78f50a43210e577075f68399e31dde7150455e179d249e637582a1
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson/jackson-bom/pom/2.22.4 2.22.4 SHA-256/a3c00688a7bf8e128451137b68f90d9137957a372756083d74555a47241d1636
            spotless\\:jacksonyaml/maven/com.fasterxml.jackson/jackson-parent/pom/2.22 2.22 SHA-256/7d45a7efb69df207bed7cc54b87a0bb012f10952c095570a273b49485da0d129
            spotless\\:jacksonyaml/maven/com.fasterxml/oss-parent/pom/79 79 SHA-256/9206b7dd67fb138036576c3dfca947a2a8f3e2c4d2d9c1f2d1dea519b3a4dae1
            spotless\\:jacksonyaml/maven/org.junit/junit-bom/pom/5.14.3 5.14.3 SHA-256/08a02856e487c9357f9b29e38745f8ae805848111e72d15aad0352338f1632e1
            spotless\\:jacksonyaml/maven/org.yaml/snakeyaml 2.5 SHA-256/e6682acf1ace77508ef13649cbf4f8d09d2cf5457bdb61d25ffb6ac0233d78dd
            spotless\\:jacksonyaml/maven/org.yaml/snakeyaml/pom/2.5 2.5 SHA-256/ba01ae7a744cb52fe8ecf3b023cbc32e0ccc8c6beef9f26de77a47808239447d
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-annotations 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-annotations/pom/2.22 2.22 SHA-256/38c3ba52a0eade3e3eacc5b13499d56e1038ccbad8ec4bb79426dc21432b46d0
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-core 2.22.4 SHA-256/2d30c375cddea9657a1e1ef97547a2694b663b355facb4de53a097782b312d1a
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-core/pom/2.22.4 2.22.4 SHA-256/371b6859b58c3fc1c0823fe66e1e6d6ab251ba99b26f7dce883bbb7c43906c21
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-databind 2.22.4 SHA-256/d4cbf8947087f305869826801913abdc50735a1fab2074a1993e7bc67ca974a3
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.core/jackson-databind/pom/2.22.4 2.22.4 SHA-256/81867c3c99c0e014d133554f2d30cdad78ec58cf1b56087088204e1e5eb34baa
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.datatype/jackson-datatype-guava 2.22.4 SHA-256/844d10482f84dc5a423124a766512a5dfe2ba20672343b64e884daf1afa9ee5a
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.datatype/jackson-datatype-guava/pom/2.22.4 2.22.4 SHA-256/03dabe989241bee5308fcb9be19b309cd631312023f79b3538047c43edf3879f
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8 2.22.4 SHA-256/2dea0c4f0a5dafaf1bd8b5e694090fe0043c29ba04367dbd38ec0869aca2bce9
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/pom/2.22.4 2.22.4 SHA-256/1f5a264adc752da74990cf86b0ff315c644679f0aafc958a5bdb7c5fe94bf027
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.datatype/jackson-datatypes-collections/pom/2.22.4 2.22.4 SHA-256/1ec2fb31c1f0c3e42a8dcfcca810c1df095700c542dea23d3ad6cae26e5795df
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.module/jackson-module-parameter-names 2.22.4 SHA-256/577cab851e0fd0e86efc51e63debf2155a8b7d0b5e177b64a4a12c89ccd4c328
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.module/jackson-module-parameter-names/pom/2.22.4 2.22.4 SHA-256/984478cee7b9e57ece8e017e4313edf2a2cb70fe6c0ba3da536152bf9d4fc796
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson.module/jackson-modules-java8/pom/2.22.4 2.22.4 SHA-256/20cdf333d224a91e6a4b548f8f4515790e34efc19cadc41f06173f51d97ac70d
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson/jackson-base/pom/2.22.4 2.22.4 SHA-256/51b5cc57df78f50a43210e577075f68399e31dde7150455e179d249e637582a1
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson/jackson-bom/pom/2.22.4 2.22.4 SHA-256/a3c00688a7bf8e128451137b68f90d9137957a372756083d74555a47241d1636
            spotless\\:palantirjavaformat/maven/com.fasterxml.jackson/jackson-parent/pom/2.22 2.22 SHA-256/7d45a7efb69df207bed7cc54b87a0bb012f10952c095570a273b49485da0d129
            spotless\\:palantirjavaformat/maven/com.fasterxml/oss-parent/pom/79 79 SHA-256/9206b7dd67fb138036576c3dfca947a2a8f3e2c4d2d9c1f2d1dea519b3a4dae1
            spotless\\:palantirjavaformat/maven/com.google.code.findbugs/jsr305 3.0.2 SHA-256/766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7
            spotless\\:palantirjavaformat/maven/com.google.code.findbugs/jsr305/pom/3.0.2 3.0.2 SHA-256/19889dbdf1b254b2601a5ee645b8147a974644882297684c798afe5d63d78dfe
            spotless\\:palantirjavaformat/maven/com.google.errorprone/error_prone_annotations 2.50.0 SHA-256/4667724877f1d37a689202da191e23efa7657c62eef93ccdac406eccfe5cdd0a
            spotless\\:palantirjavaformat/maven/com.google.errorprone/error_prone_annotations/pom/2.50.0 2.50.0 SHA-256/71a0680d277324bab46bec4b00f6f111c1b794a931d0d8cda79bcb9246982f92
            spotless\\:palantirjavaformat/maven/com.google.errorprone/error_prone_parent/pom/2.50.0 2.50.0 SHA-256/abf45a019db125b50ff55134ec2af94ae3519761038adf0a60b9bd7b4b8b6254
            spotless\\:palantirjavaformat/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            spotless\\:palantirjavaformat/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            spotless\\:palantirjavaformat/maven/com.google.guava/guava 33.7.2-jre SHA-256/b530942257fb935f8b2cfaa5f8eb5bd59c555fd8e8d01b8ce98912e077ea606c
            spotless\\:palantirjavaformat/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            spotless\\:palantirjavaformat/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            spotless\\:palantirjavaformat/maven/com.google.guava/guava-parent/pom/33.7.2-jre 33.7.2-jre SHA-256/a81570d53c8710355e0216a4405afa93834b17c4cdd0ed9aa8beede3a132bbbe
            spotless\\:palantirjavaformat/maven/com.google.guava/guava/pom/33.7.2-jre 33.7.2-jre SHA-256/5d1501a97230f90df54ea33164831a61c6714a8b2a4c6a04e90bb729581bbabc
            spotless\\:palantirjavaformat/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            spotless\\:palantirjavaformat/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            spotless\\:palantirjavaformat/maven/com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
            spotless\\:palantirjavaformat/maven/com.google.j2objc/j2objc-annotations/pom/3.1 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
            spotless\\:palantirjavaformat/maven/com.palantir.javaformat/palantir-java-format 2.103.0 SHA-256/19efbe85c7c51676f5b9b939762baacb3752eb9c39af1a2b38243acc41d1f552
            spotless\\:palantirjavaformat/maven/com.palantir.javaformat/palantir-java-format-spi 2.103.0 SHA-256/16f8d1c4b9d9d4a9faf93c27254249d65899b30a311493d2469f65ea784636ca
            spotless\\:palantirjavaformat/maven/com.palantir.javaformat/palantir-java-format-spi/pom/2.103.0 2.103.0 SHA-256/0a565834636d3660834508e869a84d1bee32402dbb1ec61adbfbee3776c8494c
            spotless\\:palantirjavaformat/maven/com.palantir.javaformat/palantir-java-format/pom/2.103.0 2.103.0 SHA-256/68dd9cde7aecd71693f6e3cdd4e656e7289cfec79729e08a422085c8a9a4606e
            spotless\\:palantirjavaformat/maven/org.functionaljava/functionaljava 5.0 SHA-256/377ad140e7d26ba04fadf219b09d7e1c74bc0232fa4010b20c1c79db11f9670e
            spotless\\:palantirjavaformat/maven/org.functionaljava/functionaljava/pom/5.0 5.0 SHA-256/47d380d98553daf742739b510bfdc4948a741877edcb2a5396fc29199689ce09
            spotless\\:palantirjavaformat/maven/org.jspecify/jspecify 1.0.1 SHA-256/070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66
            spotless\\:palantirjavaformat/maven/org.jspecify/jspecify/pom/1.0.1 1.0.1 SHA-256/55b38bf1a7b1d8ea518cf2f4f3ae9aaa236e61bbf0b6bd31ffb49753a059dd5e
            spotless\\:palantirjavaformat/maven/org.junit/junit-bom/pom/5.14.3 5.14.3 SHA-256/08a02856e487c9357f9b29e38745f8ae805848111e72d15aad0352338f1632e1
            spotless\\:palantirjavaformat/maven/org.sonatype.oss/oss-parent/pom/7 7 SHA-256/b51f8867c92b6a722499557fc3a1fdea77bdf9ef574722fe90ce436a29559454
            spotless\\:palantirjavaformat/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            spotless\\:removeunusedimports/maven/com.google.errorprone/error_prone_annotations 2.47.0 SHA-256/5364bc6f22e72e98195e406a58d3ba1c09ffa11dea0729592cb870dc2de4056d
            spotless\\:removeunusedimports/maven/com.google.errorprone/error_prone_annotations/pom/2.47.0 2.47.0 SHA-256/d80c889a4a6f711f6945fbee79e05ec247b178a567e9d5abf58eb26ebf0a0752
            spotless\\:removeunusedimports/maven/com.google.errorprone/error_prone_parent/pom/2.47.0 2.47.0 SHA-256/2368a990c7a63095e1d0d44459d5a4092f0eb31f8562bd12cdf0e1c877b6a685
            spotless\\:removeunusedimports/maven/com.google.googlejavaformat/google-java-format 2.0.0 SHA-256/7a03f28f7379dcb32e5fc5a6ded62524c4a31e9616e3b477a2f71d7124c5ccad
            spotless\\:removeunusedimports/maven/com.google.googlejavaformat/google-java-format-parent/pom/2.0.0 2.0.0 SHA-256/d45988dad2407693183de037d752c1935fef017a5b81a0c330c55a1b2d7fb5c1
            spotless\\:removeunusedimports/maven/com.google.googlejavaformat/google-java-format/pom/2.0.0 2.0.0 SHA-256/9d0102894b3cd5faa580f1dddedfb22096facfb149a53eb615169e3e2a7ebbd6
            spotless\\:removeunusedimports/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            spotless\\:removeunusedimports/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            spotless\\:removeunusedimports/maven/com.google.guava/guava 33.6.0-jre SHA-256/dc573e1fca4fd5454f4a5fd3d7da2df03002876a4175bafc14a95980dd7713b3
            spotless\\:removeunusedimports/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            spotless\\:removeunusedimports/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            spotless\\:removeunusedimports/maven/com.google.guava/guava-parent/pom/33.6.0-jre 33.6.0-jre SHA-256/374bd31f61b1cf612bee9ab2e4d70bbdf77dd85a49b431f809d4fbdc901f2dd4
            spotless\\:removeunusedimports/maven/com.google.guava/guava/pom/33.6.0-jre 33.6.0-jre SHA-256/b45b78944280054311c53da9270809fb40dcabc408ce66ce55730099b4f57d32
            spotless\\:removeunusedimports/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            spotless\\:removeunusedimports/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            spotless\\:removeunusedimports/maven/com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
            spotless\\:removeunusedimports/maven/com.google.j2objc/j2objc-annotations/pom/3.1 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
            spotless\\:removeunusedimports/maven/org.commonmark/commonmark 0.29.0 SHA-256/4c9b0fa481e36d7176f38e572c134656424cf9601bc190855d0f89478df901c6
            spotless\\:removeunusedimports/maven/org.commonmark/commonmark-ext-gfm-tables 0.29.0 SHA-256/f263d284c0795b0ddd6071d4fa969f67d460644c20828e51957ad5aec0d75612
            spotless\\:removeunusedimports/maven/org.commonmark/commonmark-ext-gfm-tables/pom/0.29.0 0.29.0 SHA-256/20a67830ba96d37826eb7f3354a9f390caa27e1f66af67029ff69ef7c2f33bd1
            spotless\\:removeunusedimports/maven/org.commonmark/commonmark-parent/pom/0.29.0 0.29.0 SHA-256/118b96f673388d28bc6c33519bb9b19770be59f58935f230bca9eb0b082f6e19
            spotless\\:removeunusedimports/maven/org.commonmark/commonmark/pom/0.29.0 0.29.0 SHA-256/dbd3979a45e5c6b4dcb57c0e71961c3decdb7e089f5b8f5b93754c2c8e178973
            spotless\\:removeunusedimports/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            spotless\\:removeunusedimports/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            spotless\\:removeunusedimports/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            spotless\\:tool/maven/com.diffplug.spotless/spotless-lib 4.10.4 SHA-256/efd428f9e4fdf187dc0eca0aee020d639874cd839b458f39566bf105f61502aa
            spotless\\:tool/maven/com.diffplug.spotless/spotless-lib/pom/4.10.4 4.10.4 SHA-256/75f6d72eb586e18b344c945fdecf90b977933a4818aff0a22f137dcd8bf38626
            spotless\\:tool/maven/org.slf4j/slf4j-api 2.0.20 SHA-256/7e1446499b359675d8aabe6af2de86b85e72ae8a2a932c0b40d0e5ba57097439
            spotless\\:tool/maven/org.slf4j/slf4j-api/pom/2.0.20 2.0.20 SHA-256/2e625c3f553cddd49f8554cea9926cbf70673b3d6833464f0c5a02f4d9130594
            spotless\\:tool/maven/org.slf4j/slf4j-bom/pom/2.0.20 2.0.20 SHA-256/351a3d34f0886942268efcf291708c48dfadce7f2643837efdb4a6954d4e6c62
            spotless\\:tool/maven/org.slf4j/slf4j-parent/pom/2.0.20 2.0.20 SHA-256/d931cf99cfd3554d322809df03c43ef897082cd480d6ff91b44d8f067ca324c8
            """;

    @TempDir
    private Path root, project;

    @Test
    public void formats_json_yaml_and_markdown_each_by_the_steps_of_its_set_then_skips_them_unchanged()
            throws IOException {
        Path json = source("config/settings.json", "{\"name\":\"demo\",\"tags\":[1,2]}\n"),
                yaml = source("config/settings.yaml", "name:   demo\ntags:\n    - one\n"),
                markdown = source("NOTES.md", "Notes\n=====\n\n* one   \n* two");
        UnaryOperator<SpotlessModule> configuration = module -> module.verify(false).sets(List.of(
                new SpotlessSet("json", List.of("*.json"), List.of(new SpotlessStep.JacksonJson(null),
                        new SpotlessStep.EndWithNewline())),
                new SpotlessSet("yaml", List.of("*.yaml"), List.of(new SpotlessStep.JacksonYaml(null))),
                new SpotlessSet("markdown", List.of("*.md"), List.of(new SpotlessStep.Flexmark(null),
                        new SpotlessStep.TrimTrailingWhitespace(),
                        new SpotlessStep.EndWithNewline()))));

        newExecutor(configuration).execute();

        assertThat(json).as("Jackson prints the JSON with an indentation, and the next step ends it with a newline")
                .content()
                .isEqualTo("{\n  \"name\": \"demo\",\n  \"tags\": [ 1, 2 ]\n}\n");
        assertThat(yaml).as("Jackson writes the YAML document again").content()
                .isEqualTo("---\nname: \"demo\"\ntags:\n- \"one\"\n");
        assertThat(markdown).as("flexmark formats the Markdown, the later steps trim it and end it with a newline")
                .content()
                .isEqualTo("Notes\n=====\n\n* one\n* two\n");
        Path command = root.resolve("spotless").resolve("format").resolve("supplement").resolve("command");
        assertThat(command).as("the first run forks the formatter").exists();

        newExecutor(configuration).execute();

        assertThat(command).as("the formatter is not forked again when nothing changed").doesNotExist();
    }

    @Test
    public void applies_the_general_steps_to_the_files_a_set_targets_and_no_others() throws IOException {
        Path script = source("scripts/run.txt", "first\t \n\tsecond"),
                other = source("scripts/run.text", "first\t \n\tsecond");

        newExecutor(module -> module.verify(false).sets(List.of(new SpotlessSet("text",
                List.of("scripts/*.txt"),
                List.of(new SpotlessStep.TrimTrailingWhitespace(),
                        new SpotlessStep.LeadingTabsToSpaces(2),
                        new SpotlessStep.EndWithNewline()))))).execute();

        assertThat(script).as("trailing whitespace goes, a leading tab becomes two spaces, a newline ends the file")
                .content()
                .isEqualTo("first\n  second\n");
        assertThat(other).as("a file no target matches is left as it is").content()
                .isEqualTo("first\t \n\tsecond");
    }

    @Test
    public void formats_java_with_the_formatter_a_set_names_resolving_each_formatter_in_a_group_of_its_own()
            throws IOException {
        Path google = source("google/Sample.java", "package google;\nimport java.util.List;\nclass Sample{int f(){return 1;}}\n"),
                palantir = source("palantir/Sample.java", "package palantir;\nclass Sample{int f(){return 1;}}\n");

        newExecutor(module -> module.verify(false).sets(List.of(
                new SpotlessSet("google", List.of("google/*.java"), List.of(
                        new SpotlessStep.RemoveUnusedImports(null),
                        new SpotlessStep.GoogleJavaFormat(null, null, null, null, null))),
                new SpotlessSet("palantir", List.of("palantir/*.java"), List.of(
                        new SpotlessStep.PalantirJavaFormat(null, null, null)))))).execute();

        assertThat(google).as("the unused import goes and google-java-format indents by two spaces").content()
                .isEqualTo("package google;\n\nclass Sample {\n  int f() {\n    return 1;\n  }\n}\n");
        assertThat(palantir).as("Palantir Java Format indents by four spaces").content()
                .isEqualTo("package palantir;\n\nclass Sample {\n    int f() {\n        return 1;\n    }\n}\n");
        assertThat(root.resolve("spotless").resolve("googlejavaformat").resolve("dependencies").resolve("resolve")
                .resolve("output").resolve("resolved"))
                .as("google-java-format resolves in a group of its own, apart from Palantir and its Guava")
                .isDirectoryContaining(path -> path.getFileName().toString().contains("google-java-format"));
    }

    @Test
    public void fails_verification_of_a_file_it_would_change_and_leaves_it_as_it_was() throws IOException {
        Path json = source("settings.json", "{\"name\":\"demo\"}");

        assertThatThrownBy(() -> newExecutor(module -> module.verify(true).sets(List.of(new SpotlessSet("json",
                List.of("*.json"),
                List.of(new SpotlessStep.JacksonJson(null)))))).execute())
                .as("verification fails on a file the steps would change");
        assertThat(json).as("verification writes nothing").content().isEqualTo("{\"name\":\"demo\"}");
    }

    private Path source(String path, String content) throws IOException {
        SequencedProperties versions = new SequencedProperties();
        versions.load(new StringReader(PINS));
        versions.store(project.resolve(BuildStep.VERSIONS));
        Path file = project.resolve(BuildStep.SOURCES + path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }

    private BuildExecutor newExecutor(UnaryOperator<SpotlessModule> configuration) throws IOException {
        BuildExecutor executor = BuildExecutor.of(root,
                Duration.ZERO,
                new HashDigestFunction("MD5"),
                BuildStepHashFunction.ofSerializationDigest("MD5"),
                BuildExecutorCallback.nop(),
                BuildExecutorCache.nop(),
                false,
                false,
                0);
        executor.addSource("project", project);
        executor.addModule("spotless",
                configuration.apply(new SpotlessModule(
                        Map.of("maven", MavenDefaultRepository.ofEnvironment(Environment.NONE)),
                        Map.of("maven", MavenPomResolver.ofEnvironment(Environment.NONE))).pinning(Pinning.STRICT)),
                "project");
        return executor;
    }
}
