package io.devtoys.cli.commands;

import io.devtoys.cli.InputSource;
import io.devtoys.tools.hash.HashCore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Compute cryptographic digests.
 *
 * <pre>
 *   devtoys hash "hello"                                # all default algorithms
 *   devtoys hash --algo SHA-256 "hello"                 # just one algorithm
 *   devtoys hash --algo MD5 --upper "hello"             # uppercase
 *   devtoys hash --file README.md                       # hash a file
 * </pre>
 */
@Command(
        name = "hash",
        description = "Compute MD5/SHA-1/SHA-256/SHA-512 digests of the input.",
        mixinStandardHelpOptions = true
)
public final class HashCommand implements Callable<Integer> {

    @Option(names = {"-a", "--algo", "--algorithm"},
            description = "Specific algorithm to compute. If omitted, all four are printed. "
                    + "Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = AlgorithmCandidates.class)
    private String algorithm;

    @Option(names = {"-u", "--upper", "--uppercase"},
            description = "Output hex in uppercase.")
    private boolean upper;

    @Option(names = {"-f", "--file"},
            description = "Read input from this file instead of stdin / positional arg.")
    private Path file;

    @Parameters(arity = "0..1", paramLabel = "TEXT",
            description = "Input text. If omitted, reads from --file or stdin.")
    private String text;

    @Override
    public Integer call() {
        String src = InputSource.read(text, file);
        if (algorithm != null) {
            // Single algorithm: print just the hex (one-line, pipeline-friendly)
            String hex = HashCore.digest(algorithm, src, upper);
            System.out.println(hex);
        } else {
            // All four: "ALGO  HEX" per line, aligned
            Map<String, String> all = HashCore.digestAll(src, upper);
            int width = all.keySet().stream().mapToInt(String::length).max().orElse(8);
            all.forEach((algo, hex) ->
                    System.out.printf("%-" + width + "s  %s%n", algo, hex));
        }
        return 0;
    }

    /** Used by Picocli for ${COMPLETION-CANDIDATES} in the help text. */
    static final class AlgorithmCandidates implements Iterable<String> {
        @Override public java.util.Iterator<String> iterator() {
            return HashCore.DEFAULT_ALGORITHMS.iterator();
        }
    }
}
