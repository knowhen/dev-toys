package io.devtoys.tools.regex;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class RegexCoreTest {

    @Test
    void findAll_emptyPattern_returnsEmpty() {
        assertTrue(RegexCore.findAll("", "anything", RegexCore.Flags.NONE).isEmpty());
        assertTrue(RegexCore.findAll(null, "anything", RegexCore.Flags.NONE).isEmpty());
    }

    @Test
    void findAll_simpleLiteral() {
        List<RegexCore.Match> matches =
                RegexCore.findAll("foo", "foobar foo baz", RegexCore.Flags.NONE);
        assertEquals(2, matches.size());
        assertEquals(0, matches.get(0).start());
        assertEquals(3, matches.get(0).end());
        assertEquals("foo", matches.get(0).value());
        assertEquals(7, matches.get(1).start());
    }

    @Test
    void findAll_withGroups_capturesThem() {
        List<RegexCore.Match> matches = RegexCore.findAll(
                "(\\w+)@(\\w+)",
                "alice@acme and bob@corp",
                RegexCore.Flags.NONE);
        assertEquals(2, matches.size());
        assertEquals(List.of("alice", "acme"), matches.get(0).groups());
        assertEquals(List.of("bob", "corp"), matches.get(1).groups());
    }

    @Test
    void findAll_caseInsensitive_matchesMixedCase() {
        RegexCore.Flags flags = new RegexCore.Flags(true, false, false);
        assertEquals(3,
                RegexCore.findAll("foo", "FOO Foo foo", flags).size());
        assertEquals(0,
                RegexCore.findAll("foo", "FOO Foo", RegexCore.Flags.NONE).size());
    }

    @Test
    void findAll_dotall_matchesAcrossLines() {
        RegexCore.Flags off = RegexCore.Flags.NONE;
        RegexCore.Flags on = new RegexCore.Flags(false, false, true);
        assertEquals(0, RegexCore.findAll("a.b", "a\nb", off).size());
        assertEquals(1, RegexCore.findAll("a.b", "a\nb", on).size());
    }

    @Test
    void findAll_invalidPattern_throwsPatternSyntaxException() {
        assertThrows(PatternSyntaxException.class,
                () -> RegexCore.findAll("(unclosed", "anything", RegexCore.Flags.NONE));
    }

    @Test
    void findAll_zeroWidthPattern_doesNotLoopForever() {
        // Empty-string match at every position shouldn't hang.
        List<RegexCore.Match> matches = RegexCore.findAll("a*", "aaa", RegexCore.Flags.NONE);
        assertTrue(matches.size() < 100, "zero-width guard should prevent runaway");
    }
}
