package io.devtoys.tools.timestamp;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TimestampCoreTest {

    @Test void fromSeconds_and_toIsoUtc() {
        Instant i = TimestampCore.fromSeconds(1710498600L);
        assertEquals("2024-03-15T09:50:00Z", TimestampCore.toIsoUtc(i));
    }

    @Test void fromMillis() {
        Instant i = TimestampCore.fromMillis(1710498600000L);
        assertEquals(1710498600L, i.getEpochSecond());
    }

    @Test void parse_acceptsIsoWithZ() {
        Instant i = TimestampCore.parse("2024-03-15T09:50:00Z");
        assertEquals(1710498600L, i.getEpochSecond());
    }

    @Test void parse_acceptsLocalIso() {
        // No offset → treated as UTC
        Instant i = TimestampCore.parse("2024-03-15T09:50:00");
        assertEquals(1710498600L, i.getEpochSecond());
    }

    @Test void parse_acceptsSpaceFormat() {
        Instant i = TimestampCore.parse("2024-03-15 09:50:00");
        assertEquals(1710498600L, i.getEpochSecond());
    }

    @Test void parse_acceptsPureDate() {
        Instant i = TimestampCore.parse("2024-03-15");
        // midnight UTC
        assertEquals("2024-03-15T00:00:00Z", TimestampCore.toIsoUtc(i));
    }

    @Test void parse_rejectsGarbage() {
        assertThrows(Exception.class, () -> TimestampCore.parse("not a date"));
        assertThrows(IllegalArgumentException.class, () -> TimestampCore.parse(""));
    }
}
