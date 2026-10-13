# Checkstyle

Checks the Java sources of a module with Checkstyle.

```properties
# jenesis.plugins.properties
checkstyle+check=build.jenesis.plugin.checkstyle
```

```properties
# build.jenesis/plugin-checkstyle.properties
@rules=../checkstyle.xml
checkstyle.severity=warning
```

| Value | Meaning |
|---|---|
| <any key> | a property the configuration expands as `${<key>}`; `config_loc` is set to the folder of `checkstyle.xml` and cannot be given |

It reads a `checkstyle.xml`, and the files it names as `${config_loc}/<path>` beside it; the project binds them with an `@<input>[/<target>]=<path>` line.

`-Djenesis.source.checkstyle` sets how findings are treated, as for every checker of the build.
