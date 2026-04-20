package io.devtoys.cli.commands;

import io.devtoys.cli.InputSource;
import io.devtoys.tools.base64.Base64Core;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Base64 encode / decode.
 *
 * <pre>
 *   devtoys base64 "hello world"                   # encode
 *   devtoys base64 --decode "aGVsbG8="             # decode
 *   echo "hello" | devtoys base64                   # encode from stdin
 *   devtoys base64 --file input.txt                 # encode from file
 * </pre>
 */
@Command(
        name = "base64",
        description = "Encode text to Base64, or decode Base64 back to text.",
        mixinStandardHelpOptions = true
)
public final class Base64Command implements Callable<Integer> {

    @Option(names = {"-d", "--decode"},
            description = "Decode instead of encode.")
    private boolean decode;

    @Option(names = {"-f", "--file"},
            description = "Read input from this file instead of the positional argument or stdin.")
    private Path file;

    @Parameters(arity = "0..1",
            paramLabel = "TEXT",
            description = "Input text. If omitted, reads from --file or stdin.")
    private String text;

    @Override
    public Integer call() {
        String src = InputSource.read(text, file);
        String out = decode ? Base64Core.decode(src) : Base64Core.encode(src);
        System.out.println(out);
        return 0;
    }
}
