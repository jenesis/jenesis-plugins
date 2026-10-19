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

```properties
# build.jenesis/spotbugs.properties
configuration=spotbugs-exclude.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `spotbugs.properties`,
and resolves it there as `build.jenesis.plugin.spotbugs`; where none is found, a `spotbugs-exclude.xml` of a
configuration folder switches it on alone, as the exclusion filter. The file
may name an exclusion filter as `configuration=<path>`, relative to `spotbugs.properties`; without one, SpotBugs reports
every finding. Its other lines are the plugin's values; it takes none.

It reads the filter that `configuration` names, handed to SpotBugs as `spotbugs-exclude.xml` whatever its name, which
the build binds.

`-Djenesis.validate.spotbugs=false` switches it off, and `-Djenesis.strict.spotbugs=true` fails the build on its findings rather
than reporting them, as for every checker of the build.
