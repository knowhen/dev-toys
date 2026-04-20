package io.devtoys.tools.loremipsum;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoremIpsumCoreTest {

    @Test void words_startsWithLorem() {
        String out = LoremIpsumCore.words(10, 42);
        assertTrue(out.startsWith("Lorem "), "Output: " + out);
        assertEquals(10, out.split(" ").length);
    }

    @Test void sentences_firstIsCanonical() {
        String out = LoremIpsumCore.sentences(3, 42);
        assertTrue(out.startsWith(LoremIpsumCore.CANONICAL_START), "Output: " + out);
    }

    @Test void paragraphs_separatedByBlankLines() {
        String out = LoremIpsumCore.paragraphs(3, 42);
        assertEquals(3, out.split("\n\n").length);
    }

    @Test void zeroOrNegative_returnEmpty() {
        assertEquals("", LoremIpsumCore.words(0, 1));
        assertEquals("", LoremIpsumCore.sentences(-1, 1));
        assertEquals("", LoremIpsumCore.paragraphs(0, 1));
    }

    @Test void deterministicForSameSeed() {
        assertEquals(LoremIpsumCore.words(20, 100), LoremIpsumCore.words(20, 100));
    }
}
