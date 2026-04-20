package io.devtoys.tools.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Pure business logic for regex testing. Zero UI dependencies.
 */
public final class RegexCore {

    /** Regex flag set. Maps directly onto {@link Pattern} flag constants. */
    public record Flags(boolean caseInsensitive, boolean multiline, boolean dotAll) {
        public static final Flags NONE = new Flags(false, false, false);

        public int toPatternFlags() {
            int f = 0;
            if (caseInsensitive) f |= Pattern.CASE_INSENSITIVE;
            if (multiline) f |= Pattern.MULTILINE;
            if (dotAll) f |= Pattern.DOTALL;
            return f;
        }
    }

    /** One match found in the input text. */
    public record Match(int start, int end, String value, List<String> groups) {}

    private RegexCore() {}

    /**
     * Find all matches of {@code pattern} in {@code input}.
     *
     * @throws PatternSyntaxException if {@code pattern} is not a valid regex
     */
    public static List<Match> findAll(String pattern, String input, Flags flags) {
        if (pattern == null || pattern.isEmpty()) return Collections.emptyList();
        String safeInput = input == null ? "" : input;
        Flags f = flags == null ? Flags.NONE : flags;

        Pattern compiled = Pattern.compile(pattern, f.toPatternFlags());
        Matcher matcher = compiled.matcher(safeInput);

        List<Match> matches = new ArrayList<>();
        while (matcher.find()) {
            List<String> groups = new ArrayList<>(matcher.groupCount());
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i)); // may be null
            }
            matches.add(new Match(matcher.start(), matcher.end(), matcher.group(), groups));
            // Guard against zero-width infinite loops in pathological patterns
            if (matcher.start() == matcher.end()) {
                if (matcher.end() >= safeInput.length()) break;
                matcher.region(matcher.end() + 1, safeInput.length());
            }
        }
        return matches;
    }
}
