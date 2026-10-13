# forbiddenapis

Checks the compiled classes of a module with forbiddenapis against bundled signatures and signatures files of the project.

```properties
# jenesis.plugins.properties
forbiddenapis+binary/validate=build.jenesis.plugin.forbiddenapis
```

```properties
# build.jenesis/plugin-forbiddenapis.properties
bundled=jdk-unsafe,jdk-deprecated
signatures=forbidden.txt
@signatures=../forbidden.txt
```

| Value | Meaning |
|---|---|
| `bundled` | comma-separated bundled signatures, such as `jdk-unsafe`, `jdk-deprecated`, `jdk-internal` (checked against the release the classes were compiled for) or `jdk-system-out` |
| `signatures` | comma-separated names of the signatures files bound to the plugin, below the input they are bound into |
| `suppress` | comma-separated annotations that suppress a finding, such as `**.SuppressForbidden` |
| `includes`, `excludes` | comma-separated patterns of the classes that are checked |
| `warn` | comma-separated signatures that are reported as warnings rather than as findings |
| `allowmissingclasses` | `true` to check classes that reference a class the class path lacks; `false` by default |
| `ignoresignaturesofmissingclasses` | `true` to ignore a signature whose class is missing; `false` by default |

At least one of `bundled` and `signatures` is required, and any other value is refused.

It reads the signatures files that `signatures` names, each where its `@<input>[/<target>]=<path>` line binds it.

`-Djenesis.validator.forbiddenapis` sets how findings are treated, as for every checker of the build.
