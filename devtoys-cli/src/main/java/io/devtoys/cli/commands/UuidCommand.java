package io.devtoys.cli.commands;

import io.devtoys.tools.uuid.UuidCore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Generate UUIDs.
 *
 * <pre>
 *   devtoys uuid                                   # 1 UUID, default format
 *   devtoys uuid -n 10                             # 10 UUIDs
 *   devtoys uuid --no-hyphens --upper              # compact hex, uppercase
 *   devtoys uuid --braces                          # {xxxxxxxx-...}
 * </pre>
 */
@Command(
        name = "uuid",
        description = "Generate random v4 UUIDs with configurable formatting.",
        mixinStandardHelpOptions = true
)
public final class UuidCommand implements Callable<Integer> {

    @Option(names = {"-n", "--count"},
            description = "How many UUIDs to generate (default: ${DEFAULT-VALUE}).",
            defaultValue = "1")
    private int count;

    @Option(names = {"-u", "--upper", "--uppercase"},
            description = "Output in uppercase.")
    private boolean upper;

    // Picocli supports --no-<option> automatically when negatable = true (since 4.0)
    @Option(names = {"--no-hyphens"},
            description = "Omit hyphens (produce compact 32-char hex).")
    private boolean noHyphens;

    @Option(names = {"-b", "--braces"},
            description = "Wrap each UUID in {curly braces}.")
    private boolean braces;

    @Override
    public Integer call() {
        UuidCore.Format fmt = new UuidCore.Format(upper, !noHyphens, braces);
        UuidCore.generate(count, fmt).forEach(System.out::println);
        return 0;
    }
}
