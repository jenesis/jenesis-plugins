# OpenRewrite

Verifies that the recipes a project declares leave the Java sources of a module unchanged, or with
`-Djenesis.format.rewrite=true` applies them, with OpenRewrite.

```yaml
# build.jenesis/rewrite.yml
type: specs.openrewrite.org/v1beta/recipe
name: example.Cleanup
displayName: Example cleanup
recipeList:
  - org.openrewrite.java.RemoveUnusedImports
```

```properties
# build.jenesis/openrewrite.properties
configuration=rewrite.yml
```

The build discovers the plugin by itself in every module where a configuration folder holds an
`openrewrite.properties`, and resolves it there as `build.jenesis.plugin.openrewrite`; where none is found, a `rewrite.yml`
of a configuration folder switches it on alone and runs every recipe it declares. It runs before the module's Java formatter. The file's keys:

| Key | Meaning |
|---|---|
| `configuration` | a `rewrite.yml`, relative to `openrewrite.properties`, every recipe of which runs |
| `recipes` | the recipes to run, comma-separated, instead of every recipe of the `rewrite.yml`: recipes of OpenRewrite, of a recipe library the module names, or of the `rewrite.yml` |

At least one of them is required, and any other value is refused.

A recipe library is named for the plugin to load beside OpenRewrite, as
`@jenesis.plugin openrewrite:tool maven/org.openrewrite.recipe/rewrite-static-analysis` in the module's
`module-info.java`, and is pinned in the group `openrewrite:tool`. The module's dependencies are handed to OpenRewrite
as the class path its type-aware recipes read.

A verifying build fails where a recipe would change a file and prints the change as a diff. A recipe that would add,
delete or move a file fails the build in both modes, as the build applies changes to existing files only.

`-Djenesis.format.openrewrite=false` switches it off.
