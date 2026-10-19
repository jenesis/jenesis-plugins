# OpenRewrite

Verifies that the recipes a project names leave the Java sources of a module unchanged, or with
`-Djenesis.format.rewrite=true` applies them, with OpenRewrite.

```properties
# build.jenesis/openrewrite.properties
recipes=org.openrewrite.java.RemoveUnusedImports,example.Cleanup
```

```yaml
# build.jenesis/rewrite.yml
type: specs.openrewrite.org/v1beta/recipe
name: example.Cleanup
displayName: Example cleanup
recipeList:
  - org.openrewrite.staticanalysis.UseDiamondOperator
```

The build discovers the plugin by itself in every module where a configuration folder holds an
`openrewrite.properties`, and resolves it there as `build.jenesis.plugin.openrewrite`. The lines of the file are its
values:

| Value | Meaning |
|---|---|
| `recipes` | the recipes to run, comma-separated: recipes of OpenRewrite, of a recipe library the module names, or of its `rewrite.yml` |

Any other value is refused, and so is a file that names no recipe.

It reads the `rewrite.yml` beside the file, which the build binds. A recipe library is named for the plugin to load
beside OpenRewrite, as `@jenesis.plugin openrewrite:tool maven/org.openrewrite.recipe/rewrite-static-analysis` in the
module's `module-info.java`, and is pinned in the group `openrewrite:tool`. The module's dependencies are handed to
OpenRewrite as the class path its type-aware recipes read.

A verifying build fails where a recipe would change a file and prints the change as a diff. A recipe that would add,
delete or move a file fails the build in both modes, as the build applies changes to existing files only.

`-Djenesis.format.openrewrite=false` switches it off.
