package io.devtoys.tools.base64;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Pure business logic for Base64 encode/decode.
 *
 * <p>Zero UI dependencies — callable from the GUI tool, from CLI commands, and
 * from unit tests. Keep this class free of any {@code javafx.*} imports.
 */
public final class Base64Core {

    private Base64Core() {}

    /** UTF-8 encode the input string to a Base64 string. Never returns null. */
    public static String encode(String plain) {
        String src = plain == null ? "" : plain;
        return Base64.getEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a Base64 string to UTF-8 text. Leading/trailing whitespace is
     * stripped before decoding.
     *
     * @throws IllegalArgumentException if the input is not valid Base64
     */
    public static String decode(String encoded) {
        String src = encoded == null ? "" : encoded.trim();
        byte[] bytes = Base64.getDecoder().decode(src);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
