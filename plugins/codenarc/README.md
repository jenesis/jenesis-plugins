# CodeNarc

Checks the Groovy sources of a module with CodeNarc.

```xml
<!-- build.jenesis/codenarc.xml -->
<ruleset xmlns="http://codenarc.org/ruleset/1.0">
    <rule class="org.codenarc.rule.basic.EmptyIfStatementRule"/>
</ruleset>
```

```properties
# build.jenesis/codenarc.properties
configuration=codenarc.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `codenarc.properties`,
and resolves it there as `build.jenesis.plugin.codenarc`; where none is found, a `codenarc.xml` of a configuration
folder switches it on alone, as if the properties named it. The file names the
rule set as `configuration=<path>`, relative to `codenarc.properties`, which is required: properties that leave it out
fail the build, naming the key to add.

Its other lines are the plugin's values; it takes none.

It reads the rule set that `configuration` names, handed to CodeNarc as `codenarc.xml` whatever its name, which the
build binds.

`-Djenesis.check.codenarc=false` switches it off, and `-Djenesis.strict.codenarc=true` fails the build on its findings rather
than reporting them, as for every checker of the build.
