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
configuration=checkstyle.xml
checkstyle.severity=warning
```

The build discovers the plugin by itself in every module where a configuration folder holds a `checkstyle.properties`,
and resolves it there as `build.jenesis.plugin.checkstyle`; where none is found, a `checkstyle.xml` of a configuration
folder switches it on alone, read as it stands. The file names the configuration, and its other lines are the plugin's values:

| Key | Meaning |
|---|---|
| `configuration` | the Checkstyle configuration, relative to `checkstyle.properties`; required |
| <any other key> | a property the configuration expands as `${<key>}`; `config_loc` is set to the folder of the configuration and cannot be given |

It reads the configuration, handed to Checkstyle as `checkstyle.xml` whatever its name, and the files it names as
`${config_loc}/<path>` beside it, all of which the build binds. Properties that name no `configuration` fail the build,
naming the key to add.

`-Djenesis.check.checkstyle=false` switches it off, and `-Djenesis.strict.checkstyle=true` fails the build on its
findings rather than reporting them, as for every checker of the build.
