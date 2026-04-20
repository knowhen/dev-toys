package io.devtoys.cli.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.devtoys.cli.InputSource;
import io.devtoys.tools.json.JsonFormatterCore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * JSON formatter.
 *
 * <pre>
 *   devtoys json format '{"a":1}'                   # pretty-print
 *   devtoys json format --indent 4 < big.json       # 4-space indent from stdin
 *   devtoys json minify < pretty.json               # strip whitespace
 *   devtoys json validate '{"a":1}'                 # exit 0 if valid, 1 otherwise
 * </pre>
 */
@Command(
        name = "json",
        description = "Format, minify, or validate JSON.",
        mixinStandardHelpOptions = true,
        subcommands = {
                JsonCommand.Format.class,
                JsonCommand.Minify.class,
                JsonCommand.Validate.class
        }
)
public final class JsonCommand implements Runnable {

    @Override
    public void run() {
        // No subcommand: show help on stderr
        picocli.CommandLine.usage(this, System.err);
    }

    @Command(name = "format", description = "Pretty-print JSON.",
            mixinStandardHelpOptions = true)
    public static final class Format implements Callable<Integer> {

        @Option(names = {"-i", "--indent"},
                description = "Indent width in spaces (default: ${DEFAULT-VALUE}).",
                defaultValue = "2")
        private int indent;

        @Option(names = {"-f", "--file"},
                description = "Read input from this file instead of stdin.")
        private Path file;

        @Parameters(arity = "0..1", paramLabel = "JSON",
                description = "Input JSON. If omitted, reads from --file or stdin.")
        private String text;

        @Override
        public Integer call() throws JsonProcessingException {
            String src = InputSource.read(text, file);
            System.out.println(JsonFormatterCore.format(src, indent));
            return 0;
        }
    }

    @Command(name = "minify", description = "Remove whitespace from JSON.",
            mixinStandardHelpOptions = true)
    public static final class Minify implements Callable<Integer> {

        @Option(names = {"-f", "--file"},
                description = "Read input from this file instead of stdin.")
        private Path file;

        @Parameters(arity = "0..1", paramLabel = "JSON",
                description = "Input JSON. If omitted, reads from --file or stdin.")
        private String text;

        @Override
        public Integer call() throws JsonProcessingException {
            String src = InputSource.read(text, file);
            System.out.println(JsonFormatterCore.minify(src));
            return 0;
        }
    }

    @Command(name = "validate", description = "Check whether the input is valid JSON.",
            mixinStandardHelpOptions = true)
    public static final class Validate implements Callable<Integer> {

        @Option(names = {"-q", "--quiet"},
                description = "Do not print any message; rely on the exit code.")
        private boolean quiet;

        @Option(names = {"-f", "--file"},
                description = "Read input from this file instead of stdin.")
        private Path file;

        @Parameters(arity = "0..1", paramLabel = "JSON",
                description = "Input JSON. If omitted, reads from --file or stdin.")
        private String text;

        @Override
        public Integer call() {
            String src = InputSource.read(text, file);
            boolean valid = JsonFormatterCore.isValid(src);
            if (!quiet) {
                System.out.println(valid ? "valid" : "invalid");
            }
            return valid ? 0 : 1;
        }
    }
}
