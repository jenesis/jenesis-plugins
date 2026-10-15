# PMD

Checks the Java sources of a module with PMD.

```xml
<!-- build.jenesis/pmd.xml -->
<ruleset name="example" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
    <rule ref="category/java/bestpractices.xml/SystemPrintln"/>
</ruleset>
```

The build discovers the plugin by itself in every module where a configuration folder holds a `pmd.xml`, and resolves
it there as `build.jenesis.plugin.pmd`.

It takes no values.

It reads the `pmd.xml` rule set, which the build binds.

`-Djenesis.source.pmd` and `-Djenesis.pmd.priority` set how findings are treated, as for every checker of the build.
