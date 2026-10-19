# PMD

Checks the Java sources of a module with PMD.

```xml
<!-- build.jenesis/pmd.xml -->
<ruleset name="example" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
    <rule ref="category/java/bestpractices.xml/SystemPrintln"/>
</ruleset>
```

```properties
# build.jenesis/pmd.properties
configuration=pmd.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `pmd.properties`,
and resolves it there as `build.jenesis.plugin.pmd`; where none is found, a `pmd.xml` of a configuration folder
switches it on alone, as if the properties named it. The file names the
rule set as `configuration=<path>`, relative to `pmd.properties`, which is required: properties that leave it out
fail the build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the rule set that `configuration` names, handed to PMD as `pmd.xml` whatever its name, which the
build binds.

`-Djenesis.check.pmd=false` switches it off, and `-Djenesis.strict.pmd=true` fails the build on its findings rather
than reporting them, as for every checker of the build; `-Djenesis.pmd.priority` sets the lowest rule priority PMD runs.
