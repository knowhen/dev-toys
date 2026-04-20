package io.devtoys.tools.urlcodec;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Pure logic for URL (percent-)encoding. UTF-8 only. */
public final class UrlCodecCore {

    private UrlCodecCore() {}

    /** URL-encode using {@code application/x-www-form-urlencoded} with UTF-8. */
    public static String encode(String plain) {
        if (plain == null || plain.isEmpty()) return "";
        return URLEncoder.encode(plain, StandardCharsets.UTF_8);
    }

    /** URL-decode assuming UTF-8. Throws IllegalArgumentException on malformed input. */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
