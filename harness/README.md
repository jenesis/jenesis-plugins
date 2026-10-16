# Plugin harness

Builds an example project with `jenesis-make` against plugins installed into a module repository of its own, so that a
plugin is tested as the build of a project loads it. Every plugin of this repository tests itself with it in `it/`.

```java
ExampleProject project = ExampleProject.of(folder, AntlrFactory.class)
        .write("jenesis.plugins.properties", "antlr+binary/generated=build.jenesis.plugin.antlr\n")
        .write("sources/module-info.java", "module example {}\n");
project.make("+sources/produce/assemble/binary/generated");
Path parser = project.find("GreetingParser.java");
```

| Method | What it does |
| --- | --- |
| `ExampleProject.of(folder, plugins...)` | Installs the module of each class and `build.jenesis` into `folder/repository`, the project lies in `folder/project`. |
| `write(file, content)` | Writes a file of the project. |
| `make(selectors...)` | Runs `jenesis-make` on the project in this JVM and returns what it printed, failing on a failed build. |
| `find(name)` | Finds a file the build wrote below `target/`. |

The module requires `build.jenesis` alone: a test module requires `build.jenesis.plugin.harness` beside the plugin it
tests and its test framework.
