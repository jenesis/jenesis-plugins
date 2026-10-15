# Checkstyle

Checks the Java sources of a module with Checkstyle.

```xml
<!-- build.jenesis/checkstyle.xml -->
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <property name="severity" value="${checkstyle.severity}"/>
    <module name="SuppressionFilter">
        <property name="file" value="${config_loc}/suppressions.xml"/>
    </module>
    <module name="TreeWalker">
        <module name="TypeName"/>
    </module>
</module>
```

```properties
# build.jenesis/checkstyle.properties
checkstyle.severity=warning
```

The build discovers the plugin by itself in every module where a configuration folder holds a `checkstyle.xml`, and
resolves it there as `build.jenesis.plugin.checkstyle`. The lines of a `checkstyle.properties` beside it are its
values:

| Value | Meaning |
|---|---|
| <any key> | a property the configuration expands as `${<key>}`; `config_loc` is set to the folder of `checkstyle.xml` and cannot be given |

It reads the `checkstyle.xml`, and the files it names as `${config_loc}/<path>` beside it, all of which the build
binds.

`-Djenesis.source.checkstyle` - `ignore`, `report` or `strict` - sets how findings are treated, as for every checker of
the build.
