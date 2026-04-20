package io.devtoys.cli;

import io.devtoys.cli.commands.Base64Command;
import io.devtoys.cli.commands.HashCommand;
import io.devtoys.cli.commands.JsonCommand;
import io.devtoys.cli.commands.RegexCommand;
import io.devtoys.cli.commands.UuidCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * DevToys Java CLI entry point.
 *
 * <p>Subcommands are registered declaratively via the {@code subcommands}
 * attribute. The top-level {@code devtoys} command has no action of its own;
 * invoking it without a subcommand prints the usage help.
 *
 * <p>Exit codes follow Picocli conventions:
 * <ul>
 *   <li>0 — success</li>
 *   <li>2 — command-line usage error (invalid option etc.)</li>
 *   <li>1 — business logic exception</li>
 * </ul>
 */
@Command(
        name = "devtoys",
        mixinStandardHelpOptions = true,
        version = "DevToys Java CLI 0.1.0",
        description = "Swiss-army knife for developers, the command line version.",
        subcommands = {
                Base64Command.class,
                JsonCommand.class,
                HashCommand.class,
                UuidCommand.class,
                RegexCommand.class,
                CommandLine.HelpCommand.class
        }
)
public final class Main implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    /** Runs when the user invokes {@code devtoys} without a subcommand. */
    @Override
    public void run() {
        // Show usage on the error stream; return usage exit code.
        CommandLine.usage(this, System.err);
    }
}
