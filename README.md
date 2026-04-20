# DevToys Java

[![CI](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml)

> Replace `YOUR_USERNAME/YOUR_REPO` above with your actual GitHub path, e.g. `johndoe/devtoys-java`.

A Java port of [DevToys](https://github.com/DevToys-app/DevToys), built with
**JavaFX 21 + AtlantaFX 2**.

This is a **skeleton**: plugin architecture + UI shell + 5 starter tools
(JSON, Base64, Hash, UUID, Regex), plus phase-1 infrastructure (persistent
settings, icon fonts, background task runner, proper logging).

## Requirements

- **JDK 21** (includes `jpackage`, which is bundled since JDK 14)
- **Maven 3.9+**
- No JavaFX SDK needed separately — pulled in via Maven (`org.openjfx`).

Verify with:

```bash
java -version       # should say 21.x
mvn -version        # last line should show Java version: 21.x
```

## Run from source

```bash
mvn install -DskipTests
mvn -pl devtoys-app javafx:run
```

- The first command installs all 4 modules to the local Maven repo.
- The second command runs the app.
- Subsequent runs only need the second command — unless you modify
  `devtoys-api`, `devtoys-core`, or `devtoys-tools`, in which case re-run
  the first.

## Build a native installer (jlink + jpackage)

```bash
mvn -pl devtoys-app -am -Ppackage package
```

Produces:

```
devtoys-app/target/
├── app/                 # jlink runtime image (~80 MB, self-contained)
│   └── bin/devtoys      # launcher binary
└── installer/
    └── DevToys-0.1.0.msi   # or .dmg on macOS, .deb on Linux
```

## Project layout

```
devtoys-java/
├── pom.xml                              ← parent: versions + shared config
│
├── devtoys-api/                         ← plugin SDK
│   └── io/devtoys/api/
│       ├── IGuiTool.java                ← core plugin contract
│       ├── ToolMetadata.java            ← @annotation (iconCode, keywords, …)
│       ├── ServiceContext.java          ← host services bag (DI alternative)
│       ├── ClipboardService.java
│       ├── SettingsStore.java
│       └── PredefinedToolGroups.java
│
├── devtoys-core/                        ← runtime infra
│   └── io/devtoys/core/
│       ├── ToolRegistry.java            ← ServiceLoader-based discovery
│       ├── ToolDescriptor.java
│       ├── services/
│       │   ├── FileSettingsStore.java   ← persists to ~/.devtoys/settings.properties
│       │   ├── InMemorySettingsStore.java  (still available for tests)
│       │   └── JavaFxClipboardService.java
│       └── tasks/
│           └── BackgroundTaskRunner.java ← DebouncedTaskRunner<T>: off-UI compute
│
├── devtoys-tools/                       ← the 5 starter tools
│   └── io/devtoys/tools/
│       ├── encoders/Base64Tool.java
│       ├── formatters/JsonFormatterTool.java
│       ├── generators/{HashTool, UuidTool}.java
│       ├── testers/RegexTool.java
│       └── support/ToolLayout.java
│
└── devtoys-app/                         ← desktop shell
    ├── src/main/java/io/devtoys/app/
    │   ├── Launcher.java                ← plain main-class shim
    │   ├── DevToysApp.java              ← JavaFX Application
    │   └── MainWindow.java              ← SplitPane + nav tree + search
    └── src/main/resources/
        └── logback.xml                  ← log configuration
```

## Phase-1 infrastructure

Since the initial skeleton, these capabilities are now in place:

### Persistent settings

`FileSettingsStore` writes to `~/.devtoys/settings.properties` with
debounced async flushes (max one write per ~100 ms even under rapid updates).
The app's dark-mode choice now survives restart. Tool authors can store any
key they want via `ServiceContext.settings()`.

### Icon fonts (Ikonli)

Three icon packs are bundled and can be used freely:

- **Fluent UI System Icons** — prefix `fluent-` (recommended; matches DevToys)
- **FontAwesome 5 Solid** — prefix `fas-`
- **Material Design Icons v2** — prefix `mdi2-`

Set `iconCode` in your `@ToolMetadata`; the pack is inferred from the prefix.
Unknown codes fall back silently to a wrench icon.

### Background task runner

`BackgroundTaskRunner.DebouncedTaskRunner<T>` runs computations off the
JavaFX Application Thread and delivers results back on it. When a new task
is submitted while an old one is still running, the old one is cancelled.
Every tool now uses this for responsive behavior with big inputs, and
exposes a `busyProperty` that's bound to a tiny spinner in the toolbar.

### Logging (SLF4J + Logback)

All modules use SLF4J. The app ships a `logback.xml` with DEBUG for
`io.devtoys.*` and INFO for everything else. Tune in
`devtoys-app/src/main/resources/logback.xml`.

## How to write a new tool

```java
@ToolMetadata(
    name = "com.example.MyTool",
    groupName = PredefinedToolGroups.ENCODERS_DECODERS,
    shortTitle = "My Tool",
    description = "Do something useful.",
    iconCode = "fluent-wrench-24-regular",
    searchKeywords = {"keyword1", "keyword2"}
)
public final class MyTool implements IGuiTool {

    private final DebouncedTaskRunner<string> runner = new DebouncedTaskRunner<>();
    private Node view;
    private SettingsStore settings;

    @Override public void initialize(ServiceContext ctx) {
        this.settings = ctx.settings();
    }

    @Override public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Input…");
        TextArea output = ToolLayout.codeArea("Output");
        output.setEditable(false);
        input.textProperty().addListener((o, a, b) ->
            runner.run(
                () -> expensiveCompute(input.getText()),
                output::setText,
                err -> output.setText("Error: " + err.getMessage())
            )
        );
        VBox page = ToolLayout.page();
        page.getChildren().addAll(
            ToolLayout.header("My Tool", "Does something."),
            ToolLayout.section("Input", input),
            ToolLayout.section("Output", output)
        );
        return page;
    }
}
```

Register it:

```java
// module-info.java
provides io.devtoys.api.IGuiTool with com.example.MyTool;

// AND  META-INF/services/io.devtoys.api.IGuiTool
com.example.MyTool
```

Run — the tool appears in the nav tree automatically.

## DevToys → Java mapping

| DevToys (.NET)                       | DevToys Java                               |
|--------------------------------------|--------------------------------------------|
| `[Export(typeof(IGuiTool))]` (MEF)   | `provides IGuiTool with …`                 |
| `[ToolDisplayInformation(…)]`        | `@ToolMetadata(…)`                         |
| `IMefProvider.Import<T>()`           | `ServiceContext.clipboard()` / etc.        |
| `ISettingsProvider`                  | `SettingsStore` (interface) + `FileSettingsStore` (impl) |
| `Task.Run` / `async`                 | `BackgroundTaskRunner.DebouncedTaskRunner` |
| `UIToolView` (custom tree)           | JavaFX `Node` directly                     |
| Blazor + WebView2                    | JavaFX + AtlantaFX `PrimerDark/Light`      |
| Fluent System Icons (font)           | Ikonli (fluent / fas / mdi2)               |

## Still missing (future phases)

- Clipboard smart-detection
- Internationalization
- Plugin marketplace UI + per-plugin `URLClassLoader` isolation
- CLI mode (separate `devtoys-cli` module + its own `.exe`)
- The other 25 DevToys tools

## License

MIT (same as original DevToys).

## Command-line mode

DevToys Java also ships as a CLI. All 5 tools are available as subcommands,
reusing the same `*Core` business logic that backs the GUI.

### Run from source

```bash
mvn install -DskipTests
mvn -pl devtoys-cli exec:java -Dexec.mainClass=io.devtoys.cli.Main -Dexec.args="base64 hello"
```

Or after installing:

```bash
java --module-path ~/.m2/.../  -m io.devtoys.cli/io.devtoys.cli.Main base64 hello
```

### Build a native executable

```bash
mvn -pl devtoys-cli -am -Ppackage package
# produces devtoys-cli/target/installer/devtoys-cli (on Linux/Mac)
#       or devtoys-cli/target/installer/devtoys-cli.exe (on Windows)
```

### Usage

```
$ devtoys base64 "hello world"
aGVsbG8gd29ybGQ=

$ devtoys base64 --decode aGVsbG8gd29ybGQ=
hello world

$ echo "hello" | devtoys hash --algo SHA-256
2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824

$ devtoys hash "hello"               # all four algorithms
MD5      5d41402abc4b2a76b9719d911017c592
SHA-1    aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d
SHA-256  2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
SHA-512  9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043

$ echo '{"a":1,"b":2}' | devtoys json format --indent 4
{
    "a": 1,
    "b": 2
}

$ devtoys uuid -n 3
c7a6e934-4fd5-4e34-b6dc-4cf25b9a9c0a
3f2a1b5d-2c9e-4f8a-85d3-8b1234567890
...

$ devtoys regex '\b\w+@\w+\.\w+\b' "contact alice@acme.com or bob@corp.io"
1. [8-23] alice@acme.com
2. [27-37] bob@corp.io
(2 matches)
```

### Subcommand reference

Each subcommand has `--help`:

| Subcommand | What it does |
|---|---|
| `devtoys base64 [-d]`      | Encode text to Base64, or decode with `-d` |
| `devtoys hash [--algo X]`  | MD5/SHA-1/SHA-256/SHA-512 digest |
| `devtoys json format`      | Pretty-print JSON, configurable indent |
| `devtoys json minify`      | Strip whitespace from JSON |
| `devtoys json validate`    | Exit 0 if input is valid JSON, 1 otherwise |
| `devtoys uuid [-n N]`      | Generate random v4 UUIDs |
| `devtoys regex PAT [TEXT]` | Test a regex and print matches |

All commands read from stdin when no positional argument is given, so they
compose naturally in Unix pipelines. Use `--file PATH` to read from a file instead.

### Exit codes

Commands follow Unix conventions:

- **0** — success (for `regex`, at least one match)
- **1** — business-logic failure (for `regex`, no matches; for `json validate`, invalid)
- **2** — command-line usage error (invalid option, etc.)


## Writing a plugin

DevToys Java loads third-party plugins from `~/.devtoys/plugins/*.jar`.
Each JAR must contain:

1. A `plugin.json` manifest at the JAR root.
2. Java classes implementing `io.devtoys.api.IGuiTool`, annotated with `@ToolMetadata`.
3. A `META-INF/services/io.devtoys.api.IGuiTool` file listing the tool class FQCNs.

### Quickstart: build the example plugins

Two example plugins ship with the source:

```bash
cd examples/hello-plugin
mvn package
# produces target/devtoys-hello-plugin-1.0.0.jar

cp target/devtoys-hello-plugin-1.0.0.jar ~/.devtoys/plugins/
# then restart DevToys — you'll see a new "Hello" tool in the sidebar
```

On Windows:

```powershell
cd examples\hello-plugin
mvn package
copy target\devtoys-hello-plugin-1.0.0.jar $HOME\.devtoys\plugins```

### plugin.json schema

```json
{
  "id":             "io.example.my-plugin",
  "name":           "My Plugin",
  "version":        "1.0.0",
  "author":         "Your Name",
  "description":    "Optional description shown in Extensions panel.",
  "homepage":       "https://example.com (optional)",
  "minHostVersion": "0.1.0 (optional, for future compatibility checks)"
}
```

`id`, `name`, and `version` are required. The `id` should use reverse-domain
notation to avoid collisions.

### Your tool class

```java
package com.example.mytool;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ToolMetadata;
import javafx.scene.Node;
import javafx.scene.control.Label;

@ToolMetadata(
    name        = "com.example.MyTool",
    groupName   = "Extensions",
    shortTitle  = "My Tool",
    description = "Does something useful.",
    iconCode    = "mdal-star",
    searchKeywords = {"my", "tool"}
)
public final class MyTool implements IGuiTool {
    @Override
    public Node getView() {
        return new Label("Hello from my plugin!");
    }
}
```

### Register it for ServiceLoader

Create `src/main/resources/META-INF/services/io.devtoys.api.IGuiTool`:

```
com.example.mytool.MyTool
```

One FQCN per line. Multiple tools per plugin are fine.

### Plugin POM

Declare the host as `provided` scope — do **not** shade these into your JAR.
DevToys already ships them, and bundling duplicates causes classloader chaos:

```xml
<dependencies>
    <dependency>
        <groupId>io.devtoys</groupId>
        <artifactId>devtoys-api</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.5</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-graphics</artifactId>
        <version>21.0.5</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

If your plugin has other dependencies (e.g. a specific library not provided by
the host), shade them in using `maven-shade-plugin`.

### Viewing installed plugins

In the running app, click **Extensions** at the bottom of the sidebar. You'll
see a card for each plugin with its metadata and tool list, plus an "Open
plugin folder" button.

### Limitations (current iteration)

- **No hot-reload** — you must restart DevToys after adding/removing plugins
- **Shared classloader** — plugins cannot have conflicting versions of the same
  library. All plugins + the host share one classpath at runtime.
- **No enable/disable toggle** — to disable a plugin, remove its JAR
- **No online marketplace** — manual install only (that's a future iteration)
