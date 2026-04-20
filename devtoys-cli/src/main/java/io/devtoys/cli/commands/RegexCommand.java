package io.devtoys.cli.commands;

import io.devtoys.cli.InputSource;
import io.devtoys.tools.regex.RegexCore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Regex matcher.
 *
 * <pre>
 *   devtoys regex '\b\w+@\w+\.\w+\b' "alice@acme bob@corp"
 *   devtoys regex -i 'hello' --file greet.txt
 *   echo "foo1 bar2" | devtoys regex '\w+(\d)' --groups
 * </pre>
 *
 * <p>Exit code:
 * <ul>
 *   <li>0 — at least one match was found (pipeline-friendly, like {@code grep})</li>
 *   <li>1 — no matches</li>
 *   <li>2 — invalid pattern (Picocli default for parameter errors)</li>
 * </ul>
 */
@Command(
        name = "regex",
        description = "Test a regular expression against input text.",
        mixinStandardHelpOptions = true
)
public final class RegexCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "PATTERN",
            description = "The regular expression to apply.")
    private String pattern;

    @Parameters(index = "1", arity = "0..1", paramLabel = "TEXT",
            description = "Text to match against. If omitted, reads from --file or stdin.")
    private String text;

    @Option(names = {"-f", "--file"},
            description = "Read input text from this file.")
    private Path file;

    @Option(names = {"-i", "--ignore-case"},
            description = "Case-insensitive match.")
    private boolean caseInsensitive;

    @Option(names = {"-m", "--multiline"},
            description = "{@code ^} and {@code $} match per line.")
    private boolean multiline;

    @Option(names = {"-s", "--dotall"},
            description = "{@code .} matches newline characters.")
    private boolean dotall;

    @Option(names = {"-g", "--groups"},
            description = "Print capture groups under each match.")
    private boolean showGroups;

    @Option(names = {"-q", "--quiet"},
            description = "Only exit with status 0/1; suppress output.")
    private boolean quiet;

    @Override
    public Integer call() {
        String input = InputSource.read(text, file);
        RegexCore.Flags flags = new RegexCore.Flags(caseInsensitive, multiline, dotall);
        List<RegexCore.Match> matches = RegexCore.findAll(pattern, input, flags);

        if (!quiet) {
            int i = 1;
            for (RegexCore.Match m : matches) {
                System.out.printf("%d. [%d-%d] %s%n", i++, m.start(), m.end(), m.value());
                if (showGroups) {
                    int g = 1;
                    for (String grp : m.groups()) {
                        System.out.printf("     group %d: %s%n", g++,
                                grp == null ? "(null)" : grp);
                    }
                }
            }
            System.err.printf("(%d match%s)%n",
                    matches.size(), matches.size() == 1 ? "" : "es");
        }
        return matches.isEmpty() ? 1 : 0;
    }
}
