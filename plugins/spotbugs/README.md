# SpotBugs

Analyses the compiled classes of a module with SpotBugs.

```xml
<!-- build.jenesis/spotbugs-exclude.xml -->
<FindBugsFilter>
    <Match>
        <Class name="~.*\.generated\..*"/>
    </Match>
</FindBugsFilter>
```

The build discovers the plugin by itself in every module where a configuration folder holds a `spotbugs-exclude.xml`,
and resolves it there as `build.jenesis.plugin.spotbugs`.

It takes no values.

It reads the `spotbugs-exclude.xml` filter, which the build binds.

`-Djenesis.validator.spotbugs` sets how findings are treated, as for every checker of the build.
