# Scalastyle

Checks the Scala sources of a module with Scalastyle.

```xml
<!-- build.jenesis/scalastyle-config.xml -->
<scalastyle>
    <name>example</name>
    <check level="error" class="org.scalastyle.file.FileLineLengthChecker" enabled="true">
        <parameters>
            <parameter name="maxLineLength">120</parameter>
        </parameters>
    </check>
</scalastyle>
```

```properties
# build.jenesis/scalastyle.properties
configuration=scalastyle-config.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `scalastyle.properties`,
and resolves it there as `build.jenesis.plugin.scalastyle`; where none is found, a `scalastyle-config.xml` of a
configuration folder switches it on alone, as if the properties named it. The file names the
configuration as `configuration=<path>`, relative to `scalastyle.properties`, which is required: properties that leave it out
fail the build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the configuration that `configuration` names, handed to Scalastyle as `scalastyle-config.xml` whatever its name, which the
build binds.

`-Djenesis.check.scalastyle=false` switches it off, and `-Djenesis.strict.scalastyle=true` fails the build on its findings rather
than reporting them, as for every checker of the build.
