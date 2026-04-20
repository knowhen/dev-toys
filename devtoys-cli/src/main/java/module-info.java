/**
 * Command-line interface module.
 *
 * <p>Reuses the {@code *Core} classes from {@code io.devtoys.tools} — the same
 * business logic that backs the GUI.
 *
 * <p>Picocli accesses private {@code @Option} / {@code @Parameters} fields via
 * reflection, so we must open the command packages to {@code info.picocli}.
 */
module io.devtoys.cli {
    requires io.devtoys.api;
    requires io.devtoys.tools;
    requires com.fasterxml.jackson.core;
    requires info.picocli;
    requires org.slf4j;

    exports io.devtoys.cli;

    // Picocli needs reflective access to @Command classes and their fields
    opens io.devtoys.cli to info.picocli;
    opens io.devtoys.cli.commands to info.picocli;
}
