# CodeNarc

Checks the Groovy sources of a module with CodeNarc.

```xml
<!-- build.jenesis/codenarc.xml -->
<ruleset xmlns="http://codenarc.org/ruleset/1.0">
    <rule class="org.codenarc.rule.basic.EmptyIfStatementRule"/>
</ruleset>
```

The build discovers the plugin by itself in every module where a configuration folder holds a `codenarc.xml`, and
resolves it there as `build.jenesis.plugin.codenarc`.

It takes no values.

It reads the `codenarc.xml` rule set, which the build binds.

`-Djenesis.source.codenarc` sets how findings are treated, as for every checker of the build.
