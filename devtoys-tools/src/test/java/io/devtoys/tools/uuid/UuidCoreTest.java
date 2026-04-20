package io.devtoys.tools.uuid;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UuidCoreTest {

    /** Canonical v4 UUID pattern (lowercase, with hyphens, version bit = 4). */
    private static final Pattern V4_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    @Test
    void generate_returnsRequestedCount() {
        List<String> ids = UuidCore.generate(7, UuidCore.Format.DEFAULT);
        assertEquals(7, ids.size());
    }

    @Test
    void generate_defaultFormat_matchesV4Pattern() {
        UuidCore.generate(10, UuidCore.Format.DEFAULT)
                .forEach(s -> assertTrue(V4_PATTERN.matcher(s).matches(),
                        "Not a valid v4 UUID: " + s));
    }

    @Test
    void generate_uuidsAreUnique() {
        List<String> ids = UuidCore.generate(100, UuidCore.Format.DEFAULT);
        assertEquals(100, ids.stream().distinct().count());
    }

    @Test
    void format_noHyphens_producesHex32() {
        UUID u = UUID.fromString("12345678-1234-4234-8234-123456789abc");
        String formatted = UuidCore.format(u, new UuidCore.Format(false, false, false));
        assertEquals("12345678123442348234123456789abc", formatted);
    }

    @Test
    void format_uppercase() {
        UUID u = UUID.fromString("12345678-1234-4234-8234-12345678abcd");
        String formatted = UuidCore.format(u, new UuidCore.Format(true, true, false));
        assertEquals("12345678-1234-4234-8234-12345678ABCD", formatted);
    }

    @Test
    void format_braces_wrapsOutput() {
        UUID u = UUID.fromString("12345678-1234-4234-8234-12345678abcd");
        String formatted = UuidCore.format(u, new UuidCore.Format(false, true, true));
        assertTrue(formatted.startsWith("{"));
        assertTrue(formatted.endsWith("}"));
    }

    @Test
    void generate_negativeCount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> UuidCore.generate(-1, UuidCore.Format.DEFAULT));
    }

    @Test
    void generate_zeroCount_returnsEmptyList() {
        assertTrue(UuidCore.generate(0, UuidCore.Format.DEFAULT).isEmpty());
    }
}
