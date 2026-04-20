package io.devtoys.tools.timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Pure logic for Unix timestamp conversion.
 *
 * <p>Supports seconds and milliseconds epoch representations, plus a handful
 * of human-readable parse formats.
 */
public final class TimestampCore {

    private TimestampCore() {}

    /** Parse seconds-since-epoch to a Java {@link Instant}. */
    public static Instant fromSeconds(long seconds) {
        return Instant.ofEpochSecond(seconds);
    }

    /** Parse milliseconds-since-epoch to a Java {@link Instant}. */
    public static Instant fromMillis(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    /**
     * Try to parse a human-entered string in one of several common formats:
     * <ul>
     *   <li>ISO-8601 with offset, e.g. {@code 2024-03-15T10:30:00Z}</li>
     *   <li>ISO-8601 local (assumes UTC), e.g. {@code 2024-03-15T10:30:00}</li>
     *   <li>Space-separated, e.g. {@code 2024-03-15 10:30:00}</li>
     *   <li>Pure date, e.g. {@code 2024-03-15} (midnight UTC)</li>
     * </ul>
     *
     * @throws DateTimeParseException if no format matches
     */
    public static Instant parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input is empty");
        }
        String s = input.trim();
        // Try each format in order of specificity
        try { return Instant.parse(s); } catch (DateTimeParseException ignored) {}
        try { return LocalDateTime.parse(s).toInstant(ZoneOffset.UTC); }
            catch (DateTimeParseException ignored) {}
        try { return LocalDateTime.parse(s, SPACE_FORMAT).toInstant(ZoneOffset.UTC); }
            catch (DateTimeParseException ignored) {}
        try {
            return LocalDateTime.parse(s + "T00:00:00").toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {}
        throw new DateTimeParseException("Unrecognised date/time format", s, 0);
    }

    /** Format an {@link Instant} as ISO-8601 UTC, e.g. {@code 2024-03-15T10:30:00Z}. */
    public static String toIsoUtc(Instant instant) {
        return instant.toString();
    }

    /**
     * Format an {@link Instant} in the user's local time zone with offset,
     * e.g. {@code 2024-03-15 18:30:00 +0800}.
     */
    public static String toLocal(Instant instant, ZoneId zone) {
        return LOCAL_FORMAT.withZone(zone).format(instant);
    }

    private static final DateTimeFormatter SPACE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter LOCAL_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
}
