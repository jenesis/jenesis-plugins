package build.jenesis.plugin.kotlin;

import module java.base;
import build.jenesis.Environment;
import build.jenesis.Language;
import build.jenesis.LanguageFactory;
import build.jenesis.Permits;
import build.jenesis.Repository;
import build.jenesis.Resolver;
import build.jenesis.SequencedProperties;

public class KotlinFactory implements LanguageFactory {

    private static final String DOKKA = "dokka.",
            MODULE_NAME = "module-name",
            FORMAT = "format",
            DOCUMENTED_VISIBILITIES = "documented-visibilities",
            SUPPRESS_OBVIOUS_FUNCTIONS = "suppress-obvious-functions",
            SUPPRESS_INHERITED_MEMBERS = "suppress-inherited-members",
            SKIP_EMPTY_PACKAGES = "skip-empty-packages",
            SKIP_DEPRECATED = "skip-deprecated",
            REPORT_UNDOCUMENTED = "report-undocumented",
            SUPPRESSED_PACKAGES = "suppressed-packages",
            FAIL_ON_WARNING = "fail-on-warning",
            JDK_VERSION = "jdk-version",
            SOURCE_LINK = "source-link",
            SOURCE_LINK_SUFFIX = "source-link-suffix",
            FOOTER_MESSAGE = "footer-message",
            EXTERNAL_LINK = "external-link.",
            PACKAGE_LIST = ".package-list";
    private static final Set<String> DOKKA_KEYS = Set.of(MODULE_NAME,
            FORMAT,
            DOCUMENTED_VISIBILITIES,
            SUPPRESS_OBVIOUS_FUNCTIONS,
            SUPPRESS_INHERITED_MEMBERS,
            SKIP_EMPTY_PACKAGES,
            SKIP_DEPRECATED,
            REPORT_UNDOCUMENTED,
            SUPPRESSED_PACKAGES,
            FAIL_ON_WARNING,
            JDK_VERSION,
            SOURCE_LINK,
            SOURCE_LINK_SUFFIX,
            FOOTER_MESSAGE);

    @Override
    public Language create(Environment environment,
                           SequencedMap<String, String> properties,
                           Permits permits,
                           String group,
                           Map<String, Repository> repositories,
                           Map<String, Resolver> resolvers) {
        SequencedProperties values = new SequencedProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey().startsWith(DOKKA) ? entry.getKey().substring(DOKKA.length()) : null;
            String link = key != null && key.startsWith(EXTERNAL_LINK) ? key.substring(EXTERNAL_LINK.length()) : null;
            if (link != null && link.endsWith(PACKAGE_LIST)) {
                link = link.substring(0, link.length() - PACKAGE_LIST.length());
                if (properties.get(DOKKA + EXTERNAL_LINK + link) == null) {
                    throw new IllegalArgumentException("The Kotlin plugin is handed " + entry.getKey() + ", the"
                            + " package-list of no link - name " + DOKKA + EXTERNAL_LINK + link + "=<url> beside it");
                }
            }
            if (key == null || !DOKKA_KEYS.contains(key) && (link == null || link.isEmpty() || link.contains("."))) {
                throw new IllegalArgumentException("The Kotlin plugin takes no value " + entry.getKey() + ": name "
                        + DOKKA + "<key> for a key of " + new TreeSet<>(DOKKA_KEYS) + ", " + DOKKA + EXTERNAL_LINK
                        + "<name> or " + DOKKA + EXTERNAL_LINK + "<name>" + PACKAGE_LIST);
            }
            values.setProperty(key, entry.getValue());
        }
        DokkaDocumentationModule documentation = DokkaDocumentationModule.ofEnvironment(environment,
                        repositories,
                        resolvers)
                .permits(permits)
                .tool(group + ":dokka");
        String moduleName = values.value(MODULE_NAME);
        if (moduleName != null) {
            documentation = documentation.moduleName(moduleName);
        }
        String format = values.value(FORMAT);
        if (format != null) {
            documentation = documentation.format(Arrays.stream(DokkaDocumentationModule.Format.values())
                    .filter(candidate -> candidate.name().toLowerCase(Locale.ROOT).equals(format))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The value " + DOKKA + FORMAT + " of the Kotlin"
                            + " plugin names " + format + ", but expected one of "
                            + Arrays.stream(DokkaDocumentationModule.Format.values())
                                    .map(candidate -> candidate.name().toLowerCase(Locale.ROOT))
                                    .toList())));
        }
        List<String> visibilities = values.entries(DOCUMENTED_VISIBILITIES);
        if (visibilities != null) {
            Set<DokkaDocumentationModule.Visibility> documented = new LinkedHashSet<>();
            for (String visibility : visibilities) {
                documented.add(Arrays.stream(DokkaDocumentationModule.Visibility.values())
                        .filter(candidate -> candidate.name().equalsIgnoreCase(visibility))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The value " + DOKKA + DOCUMENTED_VISIBILITIES
                                + " of the Kotlin plugin names " + visibility + ", but expected one of "
                                + Arrays.stream(DokkaDocumentationModule.Visibility.values())
                                        .map(candidate -> candidate.name().toLowerCase(Locale.ROOT))
                                        .sorted()
                                        .toList())));
            }
            documentation = documentation.documentedVisibilities(documented);
        }
        Boolean suppressObviousFunctions = values.flagOrNull(SUPPRESS_OBVIOUS_FUNCTIONS);
        if (suppressObviousFunctions != null) {
            documentation = documentation.suppressObviousFunctions(suppressObviousFunctions);
        }
        Boolean suppressInheritedMembers = values.flagOrNull(SUPPRESS_INHERITED_MEMBERS);
        if (suppressInheritedMembers != null) {
            documentation = documentation.suppressInheritedMembers(suppressInheritedMembers);
        }
        Boolean skipEmptyPackages = values.flagOrNull(SKIP_EMPTY_PACKAGES);
        if (skipEmptyPackages != null) {
            documentation = documentation.skipEmptyPackages(skipEmptyPackages);
        }
        Boolean skipDeprecated = values.flagOrNull(SKIP_DEPRECATED);
        if (skipDeprecated != null) {
            documentation = documentation.skipDeprecated(skipDeprecated);
        }
        Boolean reportUndocumented = values.flagOrNull(REPORT_UNDOCUMENTED);
        if (reportUndocumented != null) {
            documentation = documentation.reportUndocumented(reportUndocumented);
        }
        Boolean failOnWarning = values.flagOrNull(FAIL_ON_WARNING);
        if (failOnWarning != null) {
            documentation = documentation.failOnWarning(failOnWarning);
        }
        List<String> suppressed = values.entries(SUPPRESSED_PACKAGES);
        if (suppressed != null) {
            List<Pattern> patterns = new ArrayList<>();
            for (String pattern : suppressed) {
                try {
                    patterns.add(Pattern.compile(pattern));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("The value " + DOKKA + SUPPRESSED_PACKAGES + " of the Kotlin"
                            + " plugin names " + pattern + ", which is no regular expression of package names: "
                            + e.getDescription());
                }
            }
            documentation = documentation.suppressedPackages(patterns);
        }
        String jdk = values.value(JDK_VERSION);
        if (jdk != null) {
            if (!jdk.matches("[1-9][0-9]{0,3}")) {
                throw new IllegalArgumentException("The value " + DOKKA + JDK_VERSION + " of the Kotlin plugin is "
                        + jdk + ", but names the feature release of the JDK whose documentation Dokka links to,"
                        + " such as 17");
            }
            documentation = documentation.jdkVersion(Integer.valueOf(jdk));
        }
        String sourceLink = values.value(SOURCE_LINK), sourceLinkSuffix = values.value(SOURCE_LINK_SUFFIX);
        if (sourceLink == null && sourceLinkSuffix != null) {
            throw new IllegalArgumentException("The value " + DOKKA + SOURCE_LINK_SUFFIX + " of the Kotlin plugin"
                    + " names the line suffix of a source link, but " + DOKKA + SOURCE_LINK + " names none - name "
                    + DOKKA + SOURCE_LINK + "=<url of the source folder>");
        }
        if (sourceLink != null) {
            documentation = documentation.sourceLink(url(SOURCE_LINK, sourceLink));
        }
        if (sourceLinkSuffix != null) {
            documentation = documentation.sourceLinkSuffix(sourceLinkSuffix);
        }
        for (String key : new TreeSet<>(values.stringPropertyNames())) {
            if (key.startsWith(EXTERNAL_LINK) && !key.endsWith(PACKAGE_LIST)) {
                String url = values.value(key), packageList = values.value(key + PACKAGE_LIST);
                if (url == null) {
                    throw new IllegalArgumentException("The value " + DOKKA + key + " of the Kotlin plugin names no"
                            + " URL - name the root of the documentation to link to");
                }
                documentation = packageList == null
                        ? documentation.externalLink(url(key, url))
                        : documentation.externalLink(url(key, url), url(key + PACKAGE_LIST, packageList));
            }
        }
        String footerMessage = values.value(FOOTER_MESSAGE);
        if (footerMessage != null) {
            documentation = documentation.footerMessage(footerMessage);
        }
        KotlinCompilerModule compiler = KotlinCompilerModule.ofEnvironment(environment, repositories, resolvers)
                .permits(permits)
                .tool(group + ":tool")
                .includeResources(false);
        return new Language(new LinkedHashSet<>(List.of(".kt")),
                Language.Order.BEFORE_JAVAC,
                compiler,
                documentation);
    }

    private static URI url(String key, String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The value " + DOKKA + key + " of the Kotlin plugin names " + value
                    + ", which is no URL: " + e.getReason());
        }
    }
}
