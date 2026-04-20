package io.devtoys.tools.numberbase;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumberBaseCoreTest {

    @Test void convertsBetweenBases() {
        assertEquals("ff", NumberBaseCore.convert("255", 10, 16));
        assertEquals("11111111", NumberBaseCore.convert("ff", 16, 2));
        assertEquals("377", NumberBaseCore.convert("255", 10, 8));
        assertEquals("42", NumberBaseCore.convert("101010", 2, 10));
    }

    @Test void acceptsHexPrefix() {
        assertEquals(255L, NumberBaseCore.parse("0xff", 16));
        assertEquals(255L, NumberBaseCore.parse("0XFF", 16));
    }

    @Test void acceptsBinaryPrefix() {
        assertEquals(5L, NumberBaseCore.parse("0b101", 2));
        assertEquals(5L, NumberBaseCore.parse("0B101", 2));
    }

    @Test void ignoresWhitespaceAndUnderscores() {
        assertEquals(1000000L, NumberBaseCore.parse("1_000_000", 10));
        assertEquals(0xDEADBEEFL, NumberBaseCore.parse("DEAD BEEF", 16));
    }

    @Test void emptyAndGarbageFail() {
        assertThrows(NumberFormatException.class, () -> NumberBaseCore.parse("", 10));
        assertThrows(NumberFormatException.class, () -> NumberBaseCore.parse("xyz", 10));
        assertThrows(NumberFormatException.class, () -> NumberBaseCore.parse(null, 10));
    }

    @Test void formatZero() {
        assertEquals("0", NumberBaseCore.format(0L, 16));
        assertEquals("0", NumberBaseCore.format(0L, 2));
    }
}
