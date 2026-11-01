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
            spotless\\:princeofspace/maven/com.github.javaparser/javaparser-core 3.28.1 SHA-256/113742383cca4a46931008a961fbea54b2aa3a49ce8f0c54110930eac65f1876
            spotless\\:princeofspace/maven/com.github.javaparser/javaparser-core/pom/3.28.1 3.28.1 SHA-256/6628c981c21965a6d7d31938d94795468a4c231f3d56a3e5e78011288f7db977
            spotless\\:princeofspace/maven/com.github.javaparser/javaparser-parent/pom/3.28.1 3.28.1 SHA-256/d56320b5b14ea794167b6ca20feaa6eed7ab54d7faae35c2dbb101a92cdd5970
            spotless\\:princeofspace/maven/io.github.agustafson.princeofspace/prince-of-space-core 2.2.0 SHA-256/23fde12715a9403c525d557e6b02e02d2dfd5573d946f89164e5aee2aea6a022
            spotless\\:princeofspace/maven/io.github.agustafson.princeofspace/prince-of-space-core/pom/2.2.0 2.2.0 SHA-256/7bc6efd4395c76bdf45e8bf2a43acfedbda12b1cbbd5fe0145e8eb544cdd03b7
            spotless\\:princeofspace/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            spotless\\:princeofspace/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            spotless\\:princeofspace/maven/org.junit/junit-bom/pom/5.14.4 5.14.4 SHA-256/5706e8f29a0a07f56efbbea4a0670793414194bb8d24d8143ba1e787a2f32856
            spotless\\:princeofspace/maven/org.slf4j/slf4j-api 2.0.18 SHA-256/44508fd1576500688c790b190acdd16fec4f8c79a3e0b900afd70503cf055f55
            spotless\\:princeofspace/maven/org.slf4j/slf4j-api/pom/2.0.18 2.0.18 SHA-256/6c2c7f2c02774cc2b7b619fbd2df7873cdad29718926e44754b87f70d1eb43cb
            spotless\\:princeofspace/maven/org.slf4j/slf4j-bom/pom/2.0.18 2.0.18 SHA-256/9219aab601575126c4e66e1331eb2b0c6c17a330ac4d5a07e3cd11d580a0c12d
            spotless\\:princeofspace/maven/org.slf4j/slf4j-parent/pom/2.0.18 2.0.18 SHA-256/0b3896bedad2c9ed9697edcbea1da0c54cd238a723583a81358a83bfb782e9fa
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
            """, KOTLIN_AND_SCALA_PINS = """
            spotless\\:ktfmt/maven/com.google.errorprone/error_prone_annotations 2.41.0 SHA-256/a56e782b5b50811ac204073a355a21d915a2107fce13ec711331ad036f660fcc
            spotless\\:ktfmt/maven/com.google.errorprone/error_prone_annotations/pom/2.41.0 2.41.0 SHA-256/a151df1e2e0b48618d8b06a180748a29b3abb39b1b2396f6a1c879a727488c6e
            spotless\\:ktfmt/maven/com.google.errorprone/error_prone_parent/pom/2.41.0 2.41.0 SHA-256/c538388d760a5c1c98dcf06f6ed3cfe5f11a651827db5cbd2ed8288c795cad42
            spotless\\:ktfmt/maven/com.google.googlejavaformat/google-java-format 1.23.0 SHA-256/f9c5f181faee5c7b380feb96c1a94bec0c55859baeb2d14e9a47d92d40bef021
            spotless\\:ktfmt/maven/com.google.googlejavaformat/google-java-format-parent/pom/1.23.0 1.23.0 SHA-256/56091d29a0599cd00a9579ebcd3d1d9d185f5f76f4437df30f40b85fe2c852a4
            spotless\\:ktfmt/maven/com.google.googlejavaformat/google-java-format/pom/1.23.0 1.23.0 SHA-256/df28c5bcd9ae2ec973177cbb5aa57b732276c98c5ed67f91ef93a37e61b5f6a0
            spotless\\:ktfmt/maven/com.google.guava/failureaccess 1.0.3 SHA-256/cbfc3906b19b8f55dd7cfd6dfe0aa4532e834250d7f080bd8d211a3e246b59cb
            spotless\\:ktfmt/maven/com.google.guava/failureaccess/pom/1.0.3 1.0.3 SHA-256/c54beff37f6d42d43e14722d54a522c9ad51ef97fc5b79277e62a3ebf882f3bb
            spotless\\:ktfmt/maven/com.google.guava/guava 33.5.0-jre SHA-256/1e301f0c52ac248b0b14fdc3d12283c77252d4d6f48521d572e7d8c4c2cc4ac7
            spotless\\:ktfmt/maven/com.google.guava/guava-parent/pom/26.0-android 26.0-android SHA-256/f8698ab46ca996ce889c1afc8ca4f25eb8ac6b034dc898d4583742360016cc04
            spotless\\:ktfmt/maven/com.google.guava/guava-parent/pom/33.4.0-android 33.4.0-android SHA-256/7220ede61026596fbc720ee4b93246fa2d14f328058532b59ef053de397c7d83
            spotless\\:ktfmt/maven/com.google.guava/guava-parent/pom/33.5.0-jre 33.5.0-jre SHA-256/68719e687c6e4c9ff3e0fecbef7bd20896f0f4f7b314743ed33c72f962568215
            spotless\\:ktfmt/maven/com.google.guava/guava/pom/33.5.0-jre 33.5.0-jre SHA-256/0478fa78a908b3c31fe6d77be9978aaa621e2a181db84007e3d3b37424a2ac61
            spotless\\:ktfmt/maven/com.google.guava/listenablefuture 9999.0-empty-to-avoid-conflict-with-guava SHA-256/b372a037d4230aa57fbeffdef30fd6123f9c0c2db85d0aced00c91b974f33f99
            spotless\\:ktfmt/maven/com.google.guava/listenablefuture/pom/9999.0-empty-to-avoid-conflict-with-guava 9999.0-empty-to-avoid-conflict-with-guava SHA-256/18d4b1db26153d4e55079ce1f76bb1fe05cdb862ef9954a88cbcc4ff38b8679b
            spotless\\:ktfmt/maven/com.google.j2objc/j2objc-annotations 3.1 SHA-256/84d3a150518485f8140ea99b8a985656749629f6433c92b80c75b36aba3b099b
            spotless\\:ktfmt/maven/com.google.j2objc/j2objc-annotations/pom/3.1 3.1 SHA-256/14570838500034fc1b47c8205ce1c9d6b255c1ccdd2d1afdcdcbf75e89b24a33
            spotless\\:ktfmt/maven/org.ec4j.core/ec4j-core 1.2.0 SHA-256/7517c08e91ba0925f9470b21e538bc0feac48398fc650eb1bae8af7c92492117
            spotless\\:ktfmt/maven/org.ec4j.core/ec4j-core-parent/pom/1.2.0 1.2.0 SHA-256/7b599ea029e118faf94e9846558330867cd2f0a0612f06b83aa0c22b1861b584
            spotless\\:ktfmt/maven/org.ec4j.core/ec4j-core/pom/1.2.0 1.2.0 SHA-256/d9cbe9e89e889d6ee2b03e4df50e3b1cc21688a0d3de04fea24e656bd8f6434b
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-build-tools-api 2.4.10 SHA-256/3953d283e7710c990672e403a87df393d9726a9bf3e172194ebb5c33e062fcb0
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-build-tools-api/pom/2.4.10 2.4.10 SHA-256/78242a9440f92400eaf3df8d2108ad706f669a53125845a7d31d3df7c4de4439
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable 2.4.10 SHA-256/9309638a2ee03e6bde9ef4b7444055a94b84ab906563675d71ff9aecb64da913
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom/2.4.10 2.4.10 SHA-256/c3adb4f66fbfaa843c019b402d2c7e5f0880d32956bd8dd84022946177251102
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable 2.4.10 SHA-256/547d969a2eab602fe0bdfd9fcb1b563a5c6d30099d3351394d6427eba8288467
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom/2.4.10 2.4.10 SHA-256/44ed1a490a4b646313fc0bc2683897a1eac4d8c6b57c0825d3e81fa7ca376d35
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-reflect/pom/1.6.10 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-script-runtime 2.4.10 SHA-256/f4a5b30c2fdfbe386b097101b7989dbeea3743c06396136664784e08e15c6899
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-script-runtime/pom/2.4.10 2.4.10 SHA-256/edcf0e2cd709646c4e5945be0f0a4920a0f3f417f328756746a29d92b23b1593
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-stdlib 2.4.10 SHA-256/4ec0293bc3751423b203f1d8493251c57c42e73eb6377a6b8560d0974ff0a6df
            spotless\\:ktfmt/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.4.10 2.4.10 SHA-256/9f81f25e96b919153392ded53bd33216e76624909cfd080a3edb278d44fc34f0
            spotless\\:ktfmt/maven/org.jetbrains.kotlinx/kotlinx-coroutines-bom/pom/1.8.0 1.8.0 SHA-256/1239e9dbe1397cd5971342956b2511bc3ace7b641842e4372a088dcfa8b9ad55
            spotless\\:ktfmt/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm 1.8.0 SHA-256/9860906a1937490bf5f3b06d2f0e10ef451e65b95b269f22daf68a3d1f5065c5
            spotless\\:ktfmt/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/pom/1.8.0 1.8.0 SHA-256/a5633abd53467ceb91622d81f2e982080877145e0b76e1b75788715434885d0b
            spotless\\:ktfmt/maven/org.jetbrains.kotlinx/ktfmt 0.65 SHA-256/fbe750c85291dd2f67f9af912482a624f8ca87797bcedd44341e882f6e79cf55
            spotless\\:ktfmt/maven/org.jetbrains.kotlinx/ktfmt/pom/0.65 0.65 SHA-256/daa5ac9a0b084383af01cf52e38b2d6b78e022f1a6587f31476f8624dec9ef67
            spotless\\:ktfmt/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            spotless\\:ktfmt/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            spotless\\:ktfmt/maven/org.jspecify/jspecify 1.0.0 SHA-256/1fad6e6be7557781e4d33729d49ae1cdc8fdda6fe477bb0cc68ce351eafdfbab
            spotless\\:ktfmt/maven/org.jspecify/jspecify/pom/1.0.0 1.0.0 SHA-256/cdab929a3b95211f43d2090c5e2d0dfe8465960e378bc32b35841dab324433a6
            spotless\\:ktfmt/maven/org.sonatype.oss/oss-parent/pom/9 9 SHA-256/fb40265f982548212ff82e362e59732b2187ec6f0d80182885c14ef1f982827a
            spotless\\:ktlint/maven/ch.qos.logback/logback-classic 1.3.16 SHA-256/3f63cbc593b91670f75890ab256aa47c123a9b125185939a272c720ca1417c2c
            spotless\\:ktlint/maven/ch.qos.logback/logback-classic/pom/1.3.16 1.3.16 SHA-256/ce1b4f5896e3e5cc747e1673a6efc937544d429cf264ac445ea2ca89d76fb32c
            spotless\\:ktlint/maven/ch.qos.logback/logback-core 1.3.16 SHA-256/f153c7d582ad19219c906dd728df335232790663f5b3d2db187ac83d9e5a3b8b
            spotless\\:ktlint/maven/ch.qos.logback/logback-core/pom/1.3.16 1.3.16 SHA-256/d6c710b1f4812bf5ea77fcbe228ce07bd1a2ee80306814d79bd93366b22e4153
            spotless\\:ktlint/maven/ch.qos.logback/logback-parent/pom/1.3.16 1.3.16 SHA-256/8c4379286e75397fdce84170066a8670a94748ce2d8b99e90f5860fee7c27173
            spotless\\:ktlint/maven/com.github.ajalt.clikt/clikt-core-jvm 5.0.3 SHA-256/69cba94a2291f74b78c88239cd8b67eaf3f08e0dad297749abe6477b3e9b81b1
            spotless\\:ktlint/maven/com.github.ajalt.clikt/clikt-core-jvm/pom/5.0.3 5.0.3 SHA-256/08fa428333156308bd28b63236287a3172152dcbbf71c831e0b3ac8623e5e171
            spotless\\:ktlint/maven/com.github.ajalt.clikt/clikt-jvm 5.0.3 SHA-256/9cd19e04782f461dffeb183d17e576984febb7934932cb406646ac41e428863b
            spotless\\:ktlint/maven/com.github.ajalt.clikt/clikt-jvm/pom/5.0.3 5.0.3 SHA-256/09c01bb12d1fa17fe129ca1282f1e99a908011b183039a6d4fccf6b5c95a4e8b
            spotless\\:ktlint/maven/com.github.ajalt.colormath/colormath-jvm 3.6.0 SHA-256/59f741adfe62053066782d8b1a45afd06685a4bc64b33277e54876b993ed885c
            spotless\\:ktlint/maven/com.github.ajalt.colormath/colormath-jvm/pom/3.6.0 3.6.0 SHA-256/f03c35d505110d0673345f4740e55bcd976a9a9fa5a1b13caa2ad398f3bc1e5d
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-core-jvm 3.0.1 SHA-256/9cf9b46d1f49f2d6cf2635462b29dc59e0c29b1fb2f085b3312888bbe9c7cd31
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-core-jvm/pom/3.0.1 3.0.1 SHA-256/d5896de5e34a9d56ab2786398b260724b181f39cc0508cbb2a1f7e886cd25d87
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm 3.0.1 SHA-256/9ed3b976fcccc78da746d49866fa8ebb8f10530a93c544ea0420259a607dd95e
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-ffm-jvm 3.0.1 SHA-256/2041c2f5f7b87095b115cb19155ee9257451854d08e5b8671165b43bc7e114cf
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-ffm-jvm/pom/3.0.1 3.0.1 SHA-256/6b36e7661ad828de03a28992d8ae96256ceadf3fda128f8ec489a8d65bbbac53
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-graal-ffi-jvm 3.0.1 SHA-256/6dd4bebc164aeacddacc8f98e8f871e00fd21ce7bc2eb0d18230ea83bddfc86a
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-graal-ffi-jvm/pom/3.0.1 3.0.1 SHA-256/0a6f792f1c936095f8746991d64da8b3ff8409acde3948abe5de2fd56888783a
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-jna-jvm 3.0.1 SHA-256/41063442c8891b2774536a9b87a5062a7fd20e6f1949974c6da72f49472d6f4d
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm-jna-jvm/pom/3.0.1 3.0.1 SHA-256/fe15a8c64b47e07f2f99d883286e4efb14746245658746b2550f6f9688645f80
            spotless\\:ktlint/maven/com.github.ajalt.mordant/mordant-jvm/pom/3.0.1 3.0.1 SHA-256/b009da4c37db8e1736b8482b44f209d82771ff1c8d3bdf946eb684d3b9ce9965
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli 1.8.0 SHA-256/eb540f5ad442ba0b3392c7947c87929dab709ac056fdde7e300beaffcb86e243
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-baseline 1.8.0 SHA-256/be11607d9cebcc1456ab6ccb2578484c0f63c0288ad0fff278813d7251b8f6ed
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-baseline/pom/1.8.0 1.8.0 SHA-256/28af4bad0337acdba02e7682c218fa1fd3c2293abc054d0b319877821cd0ca58
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-checkstyle 1.8.0 SHA-256/c8c0789cf5e146d9bf8f57b1e8d1af928f1eac5653a4fbc0ccfcbe5d7906e427
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-checkstyle/pom/1.8.0 1.8.0 SHA-256/95b7ea0c0724310cc2421871e51cb0a76a63f40d4df7318a002867efe04312ca
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-core 1.8.0 SHA-256/554d980c1e8b0afd6404dcc77e6022e0466273d1300fda5a6d25a85759cbafaa
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-core/pom/1.8.0 1.8.0 SHA-256/1aeb0b3d4a6360e8302137a31b07dc4552be38f4a66aa0e7ad3975fd85639a81
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-format 1.8.0 SHA-256/0f6639379e3341ffea2ca6aff2b72e904e778cdcba31f885edb412270e591bb8
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-format/pom/1.8.0 1.8.0 SHA-256/48b7b361a66f77b08f3a3f0e0164994530ed82519a703d7ca2f84848d4f13f2f
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-html 1.8.0 SHA-256/9c45e72d24ae81b2b1c028fcf95c8d8f914a050fcf6029e2d4c2cd9c55340d72
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-html/pom/1.8.0 1.8.0 SHA-256/be3902538f6c77caf298f2a046603b4faf043ff872c7df67f32b55368c973110
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-json 1.8.0 SHA-256/115db6ad3a6eb18b33bb44a0f7b8dc7639b429ba1fa4fc39863d163a3003efb5
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-json/pom/1.8.0 1.8.0 SHA-256/86e9f1a53d73d73c86932c0228cd804797c0d5cfca719a4a50821985678d763f
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain 1.8.0 SHA-256/05bd03b465fe6bfe885777adf8c251bfe6ddc004086dbfcdfacb0061936b111d
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain-summary 1.8.0 SHA-256/003eb83d33467f43bf32b6cb7390d387a823301344fd0705750f9cf4fa42fb28
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain-summary/pom/1.8.0 1.8.0 SHA-256/c69859289478e00b5b4c1424e02d31a85f2b78305716113e494f2f80ae15e268
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-plain/pom/1.8.0 1.8.0 SHA-256/6b041349d486513c7d872065689b1238e4c54cdd2f8aee294a769cc11a3c13f7
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-sarif 1.8.0 SHA-256/8dd41428e258947f9731e7c14f80b795b7d19321c3b4f8eac7549dc335824f35
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-reporter-sarif/pom/1.8.0 1.8.0 SHA-256/f54790496035867a1f1a19b062ee4a54f9c8dfab11edb0a16f80007a16dd860b
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-ruleset-core 1.8.0 SHA-256/3cde07dd36b3d2b0ad9bf6320a15d74a1d16999361a2943ec89a77035f88cef8
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli-ruleset-core/pom/1.8.0 1.8.0 SHA-256/098563d4ba79ab5868f5a11c4bfaed3cc8f1f3ed808110b663c9c7c6e128e705
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-cli/pom/1.8.0 1.8.0 SHA-256/8ef948a60ef1441e6ecc5928c216685b70cb8dd79eb897372289db0b9e36cf09
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-logger 1.8.0 SHA-256/323a2587bc5d658acac2ab84081be1a91ec272447ddae23e889ffeeea03a6a19
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-logger/pom/1.8.0 1.8.0 SHA-256/b0cb9da9dce8dffc0eb78d82d895961b6516f833d8123671946e93e0aa84194b
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-rule-engine 1.8.0 SHA-256/26b2bc7b840dad219d56a276f11fe4a39c35e567a061d719a50914b30f403772
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-rule-engine-core 1.8.0 SHA-256/bd99dbc888be66aea31db45f4dce5ca56f465afbf02fee639bd2346d2cd864ec
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-rule-engine-core/pom/1.8.0 1.8.0 SHA-256/e132020f6c3205fa3ddf17dd572bb5de0002e1de3085f5770fec3db2f5bb5ee4
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-rule-engine/pom/1.8.0 1.8.0 SHA-256/1b15fbb3cf02d8dddb6e2a1a75b85d81335fa2651a6c54a71a4a1eee7ce54ade
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-ruleset-standard 1.8.0 SHA-256/82972b4cb2e9cd682840061b2de9d342044846f0d816ecc58f50958824760d69
            spotless\\:ktlint/maven/com.pinterest.ktlint/ktlint-ruleset-standard/pom/1.8.0 1.8.0 SHA-256/6bf5b99e59c08f1841622661824201e4805891a5c8c69fc407cf4a3e68080eeb
            spotless\\:ktlint/maven/dev.drewhamilton.poko/poko-annotations-jvm 0.20.1 SHA-256/173fc443f30b8185295c9c9b9401272be84f553e5835e3767c7daafe5be606c9
            spotless\\:ktlint/maven/dev.drewhamilton.poko/poko-annotations-jvm/pom/0.20.1 0.20.1 SHA-256/eef578f5793b9c8f103b232beeca259316eb0f3abfdc46a40026aca2dd30ed21
            spotless\\:ktlint/maven/io.github.detekt.sarif4k/sarif4k-jvm 0.6.0 SHA-256/b3ac96dd97acba8318dbe26f6a432d6c6db91c46c780805e8928b8103e5763dc
            spotless\\:ktlint/maven/io.github.detekt.sarif4k/sarif4k-jvm/pom/0.6.0 0.6.0 SHA-256/8b90fc3745f008f510886ce707539f6bb71bbdd1b7c9db8fd1db0ae566a1c0af
            spotless\\:ktlint/maven/io.github.oshai/kotlin-logging-jvm 7.0.13 SHA-256/c373f7f4c9efc85c4f70f11519f2a1b7878bc0ed75a325364d034af3fa21f776
            spotless\\:ktlint/maven/io.github.oshai/kotlin-logging-jvm/pom/7.0.13 7.0.13 SHA-256/d8adba1021851c4797139ae0d27736131547ef63001881838d21b159e2811d21
            spotless\\:ktlint/maven/net.java.dev.jna/jna 5.14.0 SHA-256/34ed1e1f27fa896bca50dbc4e99cf3732967cec387a7a0d5e3486c09673fe8c6
            spotless\\:ktlint/maven/net.java.dev.jna/jna/pom/5.14.0 5.14.0 SHA-256/e04e25951501df25adc7b1dcdb6c53373c94897b84d3e14921292763ee07ae3d
            spotless\\:ktlint/maven/org.ec4j.core/ec4j-core 1.1.1 SHA-256/d73dee44fefb725dfc658267465fe5a768a666313d8acd701209f5836f1431d8
            spotless\\:ktlint/maven/org.ec4j.core/ec4j-core-parent/pom/1.1.1 1.1.1 SHA-256/21ddde91eb897c03775c1015320767e260968ee1919ba6b8a9802e2ba197d953
            spotless\\:ktlint/maven/org.ec4j.core/ec4j-core/pom/1.1.1 1.1.1 SHA-256/9acd3bb3c0689e6095698bf66e64312646917880adff61d3492810126129b93a
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable 2.2.21 SHA-256/9588fa696ac5507af764fd5660df6894185e12704ec1bc3f5cb772af4987f295
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-compiler-embeddable/pom/2.2.21 2.2.21 SHA-256/0b33c64f2622ba3bc8283378bdb7505f2be330a2275807038ad54359b33ddf9b
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable 2.2.21 SHA-256/91c0673c22e44b054ae0314d323860ebe1f3aff3f0ebc74fff82c4733dde09be
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-daemon-embeddable/pom/2.2.21 2.2.21 SHA-256/170bdbecba57ff00a2b8d17ef2c79e08b85f67df69d853ba0a259a4183bfb923
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-reflect 1.6.10 SHA-256/3277ac102ae17aad10a55abec75ff5696c8d109790396434b496e75087854203
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-reflect/pom/1.6.10 1.6.10 SHA-256/57905524274a00ae028aaccc27283f6bc5925a934a046c1cc5d06c8ee4d6d5a9
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-script-runtime 2.2.21 SHA-256/2b1519b427b514d1536c1b425674b03fe914af637924028e9959c6625442df51
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-script-runtime/pom/2.2.21 2.2.21 SHA-256/8767a540fc5dbe20b7e1283a95f510297e6c0c98b2eac2edc4d0612a2d0f2eaf
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib 2.2.21 SHA-256/6558a3d233da56a20934b32159f9db5f86ed5816ef098f78a2c223dc6abb79dd
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-common 1.7.20 SHA-256/e0e91962bc0007338bf5b1739f62927ac32d14ba3d827fa608ab4e5351729d5d
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-common/pom/1.7.20 1.7.20 SHA-256/1862fe818a7e7bb8d0a7c031aea238b59d549453b10859ce07638c9f96ff4885
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7 1.7.20 SHA-256/524da3c1a2ad56fd52c4ae2272ef3de421de8d2047ab1c51fc306d351243f2f5
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk7/pom/1.7.20 1.7.20 SHA-256/3b91f670de0b0f4c78549a2f5e60b57548a33ab6416b1054ef63865d6c62ae95
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8 1.7.20 SHA-256/1da0d306c995945e1f807240ef64b5cd2dd5ac58612afb1a8596143d10b7ded5
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib-jdk8/pom/1.7.20 1.7.20 SHA-256/491ffbc948a3477491a7fbd43a368c5731cfafe6432af80783b2bbfe67e09fa7
            spotless\\:ktlint/maven/org.jetbrains.kotlin/kotlin-stdlib/pom/2.2.21 2.2.21 SHA-256/cf31f923196c63aedecd0a57c24269bd3702a4e47c0bf0bd64b5ada5de522272
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-coroutines-bom/pom/1.8.0 1.8.0 SHA-256/1239e9dbe1397cd5971342956b2511bc3ace7b641842e4372a088dcfa8b9ad55
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm 1.8.0 SHA-256/9860906a1937490bf5f3b06d2f0e10ef451e65b95b269f22daf68a3d1f5065c5
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/pom/1.8.0 1.8.0 SHA-256/a5633abd53467ceb91622d81f2e982080877145e0b76e1b75788715434885d0b
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm 1.4.1 SHA-256/eba7f1c854296e4ce1418fb01360f8f10c5683e7c45aa3472018417a067636f3
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm/pom/1.4.1 1.4.1 SHA-256/6e522e0a863ead95d7f2732e00f6f45bf302ea32e09be88c64d060c07655f832
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm 1.4.1 SHA-256/af604c46737121d4225fdb60ef0e17766a3c94b7c1c9ef76b4e3a5c7733d557e
            spotless\\:ktlint/maven/org.jetbrains.kotlinx/kotlinx-serialization-json-jvm/pom/1.4.1 1.4.1 SHA-256/32d9347c710135fbe70a70192857bc73b06959962edecfff31926039be24b861
            spotless\\:ktlint/maven/org.jetbrains/annotations 13.0 SHA-256/ace2a10dc8e2d5fd34925ecac03e4988b2c0f851650c94b8cef49ba1bd111478
            spotless\\:ktlint/maven/org.jetbrains/annotations/pom/13.0 13.0 SHA-256/965aeb2bedff369819bdde1bf7a0b3b89b8247dd69c88b86375d76163bb8c397
            spotless\\:ktlint/maven/org.slf4j/slf4j-api 2.0.7 SHA-256/5d6298b93a1905c32cda6478808ac14c2d4a47e91535e53c41f7feeb85d946f4
            spotless\\:ktlint/maven/org.slf4j/slf4j-api/pom/2.0.7 2.0.7 SHA-256/2d403ccf0e0a02d5c1a8667b0e2a33c8dfc6038ab287b9671dd681c205267981
            spotless\\:ktlint/maven/org.slf4j/slf4j-parent/pom/2.0.7 2.0.7 SHA-256/c182bb36cd3af1c93c1603cdfefe7889157db30ba8b584f4a44535fcd22e45e7
            spotless\\:scalafmt/maven/com.lihaoyi/fansi_2.13 0.5.1 SHA-256/e50796c69261fac857469122ab75f5aab4aeef855ca414f184cb132b318c2d9d
            spotless\\:scalafmt/maven/com.lihaoyi/fansi_2.13/pom/0.5.1 0.5.1 SHA-256/bbe8185f4ecc1e3eb875d15f966aa0a5a3614eec23dbc7054c5b814712d1ecbe
            spotless\\:scalafmt/maven/com.lihaoyi/sourcecode_2.13 0.4.0 SHA-256/7ce9b8c285a04280374d739d7b799408b2cfa40cc3173960848aec9ee8450fed
            spotless\\:scalafmt/maven/com.lihaoyi/sourcecode_2.13/pom/0.4.0 0.4.0 SHA-256/8008680d7b43fd4697f814156b100af95ccb239ba16841d0a5125734dfef82ce
            spotless\\:scalafmt/maven/com.typesafe/config 1.4.9 SHA-256/47d13e3196c84f2a8ccd014ae563738c3da5b7406b38e03505ec0601a2f4fc37
            spotless\\:scalafmt/maven/com.typesafe/config/pom/1.4.9 1.4.9 SHA-256/c19fb035bf6e545651b567c72feb0108cc4e57100791473a0498a9689468126e
            spotless\\:scalafmt/maven/io.github.java-diff-utils/java-diff-utils 4.16 SHA-256/620403030d676a4a27f780a3acec7438dee1b1651a1c804fa6bb11bb07399a6f
            spotless\\:scalafmt/maven/io.github.java-diff-utils/java-diff-utils-parent/pom/4.16 4.16 SHA-256/a4344da58e53a8e8c57be19e923badf24b27dd42b07de6d446d2a659a456509a
            spotless\\:scalafmt/maven/io.github.java-diff-utils/java-diff-utils/pom/4.16 4.16 SHA-256/434d598a1e1813111385235161627a4c4ebd62134f7054428d3e202b32cf39de
            spotless\\:scalafmt/maven/org.jline/jline-parent/pom/3.29.0 3.29.0 SHA-256/7922efd3d870a7d7ae03b196c300e35c5de0228e59a5b012aea641e36d79928e
            spotless\\:scalafmt/maven/org.jline/jline/jar/jdk8 3.29.0 SHA-256/ed2680487642df95379f220c09c0f77e25095713387e2bc00c2d6581bb79c804
            spotless\\:scalafmt/maven/org.jline/jline/pom/3.29.0 3.29.0 SHA-256/de40390a98007a1fa5c353d6d4f613280e095d3038558ecb5e7c231dbd126a70
            spotless\\:scalafmt/maven/org.scala-lang.modules/scala-collection-compat_2.13 2.14.0 SHA-256/95986ac32df70c9ebdd96edfb276cdc038deedbe600177a45f6584022f34a13f
            spotless\\:scalafmt/maven/org.scala-lang.modules/scala-collection-compat_2.13/pom/2.14.0 2.14.0 SHA-256/96f60b6eaec03738935227ea27227f991d47db10db65afacaa5a9594d5122ac7
            spotless\\:scalafmt/maven/org.scala-lang/scala-compiler 2.13.18 SHA-256/2f15891fcae7aad30a3892194fb2abb6224cf7ce5d2bd90fba7f1c48682fca21
            spotless\\:scalafmt/maven/org.scala-lang/scala-compiler/pom/2.13.18 2.13.18 SHA-256/eb02cef7faa0c4dee7be6a983e8ee93f7fe16ffc3e7c0163c597f1f5c441486a
            spotless\\:scalafmt/maven/org.scala-lang/scala-library 2.13.18 SHA-256/4e85d96ff7bc7dc627985523c3541b9917aaa08e956391380c42db21a2c4e5a0
            spotless\\:scalafmt/maven/org.scala-lang/scala-library/pom/2.13.18 2.13.18 SHA-256/c3fb118214e12399857f4aeaa3033ab8b06f6edbeec9bfe4cb5f0101c034c21d
            spotless\\:scalafmt/maven/org.scala-lang/scala-reflect 2.13.18 SHA-256/6935ff1982b2ac93d695f15aa66921be2f602921277afe002f018fd8c7d6e29b
            spotless\\:scalafmt/maven/org.scala-lang/scala-reflect/pom/2.13.18 2.13.18 SHA-256/039a53586af54d820caaaf916fa6223c1ae5b6ae04202c8a3bc982f8efaba75f
            spotless\\:scalafmt/maven/org.scalameta/common2_2.13 4.17.3 SHA-256/d16050f3557e472ac3489501edd4d941d975bc0a6f72a54363b6598e29a972d0
            spotless\\:scalafmt/maven/org.scalameta/common2_2.13/pom/4.17.3 4.17.3 SHA-256/3d40376210ae6db0059c9abb99a2295f958d1b3549b60668ce79e0cedb19d300
            spotless\\:scalafmt/maven/org.scalameta/common_2.13 4.17.3 SHA-256/e15f1eca7e1d855daf0fd62874b944272b84d2bbee96f3adfaef0cdd34ec91e4
            spotless\\:scalafmt/maven/org.scalameta/common_2.13/pom/4.17.3 4.17.3 SHA-256/b1b7d4fc4ef9baf196d4eb35139f8a855d060d7e188acde545cb985a6e903587
            spotless\\:scalafmt/maven/org.scalameta/io_2.13 4.17.3 SHA-256/c9aa7493cf90a1acafcc96dad84670409aeb3b5e280a38ce40332eb6a21cf49f
            spotless\\:scalafmt/maven/org.scalameta/io_2.13/pom/4.17.3 4.17.3 SHA-256/e74b463dee39b3532a40b9b82c406bf98291e7ef9a1e9745c5918f78227dfb58
            spotless\\:scalafmt/maven/org.scalameta/mdoc-parser_2.13 2.9.1 SHA-256/4ce63efb015b6ea92bd0bcf107c8153b3e2857495821a9a59dc41424b4015831
            spotless\\:scalafmt/maven/org.scalameta/mdoc-parser_2.13/pom/2.9.1 2.9.1 SHA-256/a5e86c6655fce32233d2f936f2ae31ae60a7adde3a4fe5da729b34f3cdd21a3d
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-core_2.13 0.18.7 SHA-256/9493d98e6a9b8b43bdbd7a00e06fa1dcf295180e0bf0d94d705f18801c75ec80
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-core_2.13/pom/0.18.7 0.18.7 SHA-256/269072169480c3b9a46762f422bc63c824d03613f1865aea8d58da70a51d506a
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-pprint_2.13 0.18.7 SHA-256/9f727dfdf0883c4066509e82f9ab58142ce0fb25371599d4a1b15e35d52467af
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-pprint_2.13/pom/0.18.7 0.18.7 SHA-256/d0567815f382efb898857142f20e0baed33179519e154328607a32a2f9066bad
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-typesafe-config_2.13 0.18.7 SHA-256/85085a7a082eead66bf0dccd08d402e5a4bad1f3fc73673284fac8e8f9e6c4ec
            spotless\\:scalafmt/maven/org.scalameta/metaconfig-typesafe-config_2.13/pom/0.18.7 0.18.7 SHA-256/96a3401209a9bb68797621b602046f502004cd010969937432dcf716e7bfc1e8
            spotless\\:scalafmt/maven/org.scalameta/parsers_2.13 4.17.3 SHA-256/21931f60d741a95f6fc364f5514672b587b24a2316df0a80fc67059a9989828d
            spotless\\:scalafmt/maven/org.scalameta/parsers_2.13/pom/4.17.3 4.17.3 SHA-256/4992b1cba24a78ee470f2b86525a9a1d27d2be72b5264478ccebf643e7202dba
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-config_2.13 3.11.5 SHA-256/0e000fe9c11c991f9a012331b829a7295b10995c1f5421701755e1cce6373149
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-config_2.13/pom/3.11.5 3.11.5 SHA-256/d3e7bab770aa5905b987844a5586ff4b6f34c9dffd1a1cd12a7e2f75a886b292
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-core_2.13 3.11.5 SHA-256/301fffd68a5723390d62ce62b075ed2e9c84003a0994634951638c4df6eb77f8
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-core_2.13/pom/3.11.5 3.11.5 SHA-256/f1c93ac000c9e9f3badcc7146f9a662bd4a35684e7d1f269a14d7c1f0c773169
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-macros_2.13 3.11.5 SHA-256/7edb248e9d74ad95ce8cabf9fd903dc20419f405a3e86eedbeefdf02e291cbe5
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-macros_2.13/pom/3.11.5 3.11.5 SHA-256/521aa83591b88f7edf99ef865fafc8964804c493ef49b1b67bc0d554c3a0d857
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-sysops_2.13 3.11.5 SHA-256/314582e8086be283b9df1c22b41269cf22eb6fed5af37be0a6049ba5ee08594e
            spotless\\:scalafmt/maven/org.scalameta/scalafmt-sysops_2.13/pom/3.11.5 3.11.5 SHA-256/c454a06b4d31472863d58d872d241687a0765146e968329d8c946d1b1b0b1595
            spotless\\:scalafmt/maven/org.scalameta/scalameta_2.13 4.17.3 SHA-256/5d7b942d074c764f10da068903f7a8667838d68c6e7a737ad0d0061cada0d182
            spotless\\:scalafmt/maven/org.scalameta/scalameta_2.13/pom/4.17.3 4.17.3 SHA-256/ccab7b6c1d443b90a493003449bd5158d567d929b2980a33ef963661df366999
            spotless\\:scalafmt/maven/org.scalameta/trees2_2.13 4.17.3 SHA-256/3f3729adf6faa48802f8b7c77ac67a6077cb99073596dc3eaf48d6ca9371fe55
            spotless\\:scalafmt/maven/org.scalameta/trees2_2.13/pom/4.17.3 4.17.3 SHA-256/6a8cb0fce21333e8de6265f64ec4a632b5caa3acd6140b100706f2fd9cf02111
            spotless\\:scalafmt/maven/org.scalameta/trees_2.13 4.17.3 SHA-256/38047a5a7b0f66faad77e18177a6152413de848b23353b615b1cecab2c0212f3
            spotless\\:scalafmt/maven/org.scalameta/trees_2.13/pom/4.17.3 4.17.3 SHA-256/17e19035e7703cdd57900aff7fc154391422cdc277881138f1290709e1c072b4
            spotless\\:scalafmt/maven/org.typelevel/paiges-core_2.13 0.4.4 SHA-256/ffbd59d3648e71c5b8f4474a54121fb3512707e7901245831669aa9e85f3bbf0
            spotless\\:scalafmt/maven/org.typelevel/paiges-core_2.13/pom/0.4.4 0.4.4 SHA-256/04be1fc7d13b88b27ca127e94401cc0994b178424906fcfa9ed099d7f658f023
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
    public void formats_java_with_prince_of_space_and_starts_it_with_the_license_header_a_set_hands_down()
            throws IOException {
        Path sample = source("sample/Sample.java", "package sample;\nclass Sample{int f(){return 1;}}\n");

        newExecutor(module -> module.verify(false).sets(List.of(new SpotlessSet("java", List.of("*.java"), List.of(
                new SpotlessStep.PrinceOfSpace(null, null, null, 2, null, null, null, null),
                new SpotlessStep.LicenseHeader("/*\n * Licensed under the Apache License.\n */", null)))))).execute();

        assertThat(sample).as("Prince of Space indents by the two spaces the set names, and the header comes first")
                .content()
                .isEqualTo("/*\n * Licensed under the Apache License.\n */\npackage sample;\n\n"
                        + "class Sample {\n  int f() {\n    return 1;\n  }\n}\n");
    }

    @Test
    public void formats_kotlin_and_scala_with_the_options_each_set_hands_down_to_its_formatter() throws IOException {
        Path ktfmt = source("ktfmt/Sample.kt", "package ktfmt\nfun f( ) : Int { return 1 }\n"),
                ktlint = source("ktlint/Sample.kt", "package ktlint\n\nfun f( ) : Int { return 1 }\n"),
                scala = source("Sample.scala", "object Sample { def f( ) : Int = { 1 } }\n");

        newExecutor(module -> module.verify(false).sets(List.of(
                new SpotlessSet("ktfmt", List.of("ktfmt/*.kt"), List.of(
                        new SpotlessStep.Ktfmt(null, "GOOGLE", null, null, null, null))),
                new SpotlessSet("ktlint", List.of("ktlint/*.kt"), List.of(
                        new SpotlessStep.Ktlint(null, new TreeMap<>(Map.of("indent_size", "2"))))),
                new SpotlessSet("scala", List.of("*.scala"), List.of(
                        new SpotlessStep.Scalafmt("3.11.5", null, new TreeMap<>(Map.of("runner.dialect", "scala213")))))))).execute();

        assertThat(ktfmt).as("ktfmt formats in the Google style the set names").content()
                .isEqualTo("package ktfmt\n\nfun f(): Int {\n  return 1\n}\n");
        assertThat(ktlint).as("ktlint formats with the .editorconfig property the set names").content()
                .isEqualTo("package ktlint\n\nfun f(): Int = 1\n");
        assertThat(scala).as("scalafmt formats with the configuration the set names").content()
                .isEqualTo("object Sample { def f(): Int = { 1 } }\n");
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
        versions.load(new StringReader(KOTLIN_AND_SCALA_PINS));
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
