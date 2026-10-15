# Eclipse formatter

Verifies, or with `-Djenesis.format.rewrite=true` rewrites, the Java sources of a module with the Eclipse formatter.

```properties
# build.jenesis/javaformat.properties
formatter=eclipse
profile=formatter.xml
```

The build discovers the plugin by itself in every module where a configuration folder holds a `javaformat.properties`
naming `formatter=eclipse`, and resolves it there as `build.jenesis.plugin.eclipse`. The other lines of the file are
its values:

| Value | Meaning |
|---|---|
| `profile` | the formatter profile Eclipse exports, a file beside `javaformat.properties` |

Any other value is refused.

It reads the profile that `profile` names, which the build binds.

`-Djenesis.format.java=false` switches it off.
