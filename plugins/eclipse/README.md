# Eclipse formatter

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the Eclipse formatter.

```properties
# build.jenesis/eclipse.properties
configuration=formatter.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds an `eclipse.properties`,
and resolves it there as `build.jenesis.plugin.eclipse`. The file may name the formatter profile Eclipse exports as
`configuration=<path>`, relative to it; without one, the Eclipse formatter formats with its defaults. Where no
configuration folder holds the properties, an `eclipse-formatter.xml` of a configuration folder switches it on alone,
as the profile. Its other lines are the plugin's values; it takes none. A build that finds the properties of a second
Java formatter fails, naming both.

It reads the profile that `configuration` names, handed to the Eclipse formatter as `eclipse-formatter.xml` whatever its
name, which the build binds.

`-Djenesis.format.eclipse=false` switches it off.
