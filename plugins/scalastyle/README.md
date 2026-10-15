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

The build discovers the plugin by itself in every module where a configuration folder holds a `scalastyle-config.xml`,
and resolves it there as `build.jenesis.plugin.scalastyle`.

It takes no values.

It reads the `scalastyle-config.xml`, which the build binds.

`-Djenesis.source.scalastyle` sets how findings are treated, as for every checker of the build.
