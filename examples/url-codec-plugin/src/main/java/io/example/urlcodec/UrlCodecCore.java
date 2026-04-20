package io.example.urlcodec;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL (percent-)encoding / decoding. Pure logic, no UI dependencies.
 *
 * <p>Uses {@link URLEncoder} / {@link URLDecoder} with UTF-8. Follows the
 * {@code application/x-www-form-urlencoded} variant — i.e. spaces become
 * {@code +}, not {@code %20}. The decoder accepts both.
 */
public final class UrlCodecCore {

    private UrlCodecCore() {}

    /** URL-encode using {@code application/x-www-form-urlencoded} with UTF-8. */
    public static String encode(String plain) {
        if (plain == null || plain.isEmpty()) return "";
        return URLEncoder.encode(plain, StandardCharsets.UTF_8);
    }

    /** URL-decode assuming UTF-8. */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
