package io.devtoys.tools.uuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pure business logic for UUID generation and formatting. Zero UI dependencies.
 */
public final class UuidCore {

    /** UUID format options. */
    public record Format(boolean uppercase, boolean hyphens, boolean braces) {
        public static final Format DEFAULT = new Format(false, true, false);
    }

    private UuidCore() {}

    /** Generate {@code count} random v4 UUIDs formatted per {@code format}. */
    public static List<String> generate(int count, Format format) {
        if (count < 0) throw new IllegalArgumentException("count must be >= 0");
        if (format == null) format = Format.DEFAULT;
        List<String> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(format(UUID.randomUUID(), format));
        }
        return out;
    }

    /** Apply formatting to a single UUID. */
    public static String format(UUID uuid, Format format) {
        if (uuid == null) throw new IllegalArgumentException("uuid is null");
        if (format == null) format = Format.DEFAULT;
        String s = uuid.toString();                  // always lowercase with hyphens from JDK
        if (!format.hyphens()) s = s.replace("-", "");
        if (format.uppercase()) s = s.toUpperCase();
        if (format.braces()) s = "{" + s + "}";
        return s;
    }
}
