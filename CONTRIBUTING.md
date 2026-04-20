# Contributing to DevToys Java

Thanks for your interest! This doc covers the basics of hacking on DevToys
Java.

## Quick start

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd devtoys-java
mvn clean install     # builds all modules, runs all tests
mvn -pl devtoys-app javafx:run   # launch the GUI
```

That's it. You should see the app window in about 5 seconds on first run.

## Prerequisites

- **JDK 21** (Eclipse Temurin recommended — `https://adoptium.net`)
- **Maven 3.9 or newer**
- An IDE that supports Java Platform Module System (JPMS) properly:
  IntelliJ IDEA 2022.1+ or Eclipse 2022-06+

Want to avoid installing Maven yourself? Generate a
[Maven Wrapper](https://maven.apache.org/tools/wrapper/) in one command:

```bash
mvn -N wrapper:wrapper
```

This creates `mvnw`, `mvnw.cmd`, and `.mvn/` in the project root. After that
you can use `./mvnw` (Linux/macOS) or `mvnw.cmd` (Windows) instead of `mvn`.

## Repository layout

```
devtoys-api/            Plugin SDK (IGuiTool, ToolMetadata, ServiceContext)
devtoys-core/           Host services (settings store, task runner, registry)
devtoys-tools/          Built-in tools — Core (pure logic) + Tool (GUI)
devtoys-plugin-loader/  Third-party plugin discovery and loading
devtoys-app/            JavaFX desktop shell
devtoys-cli/            Command-line interface sharing the same Core classes
examples/               Standalone example plugin projects
```

Every built-in tool splits into two classes:
- **`XxxCore`** — pure logic, no JavaFX. Shared with the CLI and testable
  without a JavaFX toolkit.
- **`XxxTool`** — JavaFX UI shell, calls `XxxCore`.

## Running tests

```bash
mvn test                            # all modules
mvn -pl devtoys-tools test          # just the built-in tools
mvn -pl devtoys-tools -Dtest=Base64CoreTest test   # just one test class
```

Test reports land under each module's `target/surefire-reports/`.

## Adding a new built-in tool

Walk through this using an example tool called "Foo":

1. **Create package `io.devtoys.tools.foo` in `devtoys-tools/src/main/java/`.**

2. **Write `FooCore.java`** — pure logic with no JavaFX imports. Static
   methods preferred when there's no state:
   ```java
   public final class FooCore {
       private FooCore() {}
       public static String doSomething(String input) {  }
   }
   ```

3. **Write `FooTool.java`** — JavaFX GUI that calls `FooCore`. Annotate with
   `@ToolMetadata`:
   ```java
   @ToolMetadata(
       name = "io.devtoys.tools.foo.FooTool",
       groupName = PredefinedToolGroups.CONVERTERS,
       shortTitle = "Foo",
       longTitle = "Foo Tool",
       description = "Does foo things.",
       iconCode = "mdal-star",
       searchKeywords = {"foo", "bar"}
   )
   public final class FooTool implements IGuiTool { ... }
   ```

4. **Write `FooCoreTest.java`** under `src/test/java/io/devtoys/tools/foo/`.
   Target 5+ tests per Core class covering happy path, edge cases, and errors.

5. **Update `devtoys-tools/src/main/java/module-info.java`**:
   - add `exports io.devtoys.tools.foo;`
   - add `io.devtoys.tools.foo.FooTool` to the `provides IGuiTool with` list.

6. **Update `devtoys-tools/src/main/resources/META-INF/services/io.devtoys.api.IGuiTool`**:
   - add `io.devtoys.tools.foo.FooTool` on a new line.

7. **(Optional) expose via CLI**: add `FooCommand` to `devtoys-cli/.../commands/`.

8. Run `mvn clean install` and verify the new tool appears in the sidebar.

## Icons

We use [Ikonli](https://kordamp.org/ikonli/) Material 2 pack. Valid codes
look like `mdal-link` (letters A-L) or `mdmz-vpn_key` (M-Z). Find codes at:
https://kordamp.org/ikonli/cheat-sheet-material2.html

If you pass an unknown code, `MainWindow` logs a warning and shows no icon —
it won't crash the app.

## Code style

- 4-space indentation, no tabs (enforced by `.gitattributes`)
- Javadoc on every public class and non-trivial public method
- Prefer `final` on classes and fields when immutability is possible
- Error handling: catch the narrowest exception that makes sense, log with
  SLF4J (`LoggerFactory.getLogger(getClass())`), never `e.printStackTrace()`
- Tests: one `@Test` method per behavior; test method names describe the
  behavior, not the implementation (e.g. `decode_acceptsUtf8`, not `testDecode2`)

## Commit messages

Short imperative mood, like:
```
Add JWT decoder tool
Fix timestamp parser on Windows
Rename FooBar to BazQux
```

Rough guideline: 50-char subject line, blank line, then optional wrapped body.

## Pull requests

- Keep PRs focused — one feature or fix per PR
- Make sure `mvn clean install` passes locally before opening
- Link to any GitHub issue the PR addresses
- CI must be green before merge

## Before asking for help

Check these first — they cover 80% of first-time issues:

1. **`requires` missing in module-info** — when compilation fails with "package
   X is not visible", add `requires X;` in the consuming module's
   `module-info.java`.
2. **`uses` missing for ServiceLoader** — any module calling
   `ServiceLoader.load(SomeInterface.class, ...)` must declare
   `uses SomeInterface;` in its module-info.
3. **JPMS + reflection errors** — the `devtoys-app` module `opens` packages
   to `javafx.graphics`. If you add new packages that JavaFX will reflect
   on (e.g. for FXML loading), add similar `opens ... to javafx.graphics;`
   clauses.
4. **`mvn clean install` passes but `javafx:run` fails** — usually a missing
   `requires` in the `devtoys-app` module-info, or a JVM argument issue.
   Check the stack trace for module names, not class names.

## License

By contributing, you agree that your contributions will be licensed under
the same [Apache 2.0 License](LICENSE) that covers the project.
