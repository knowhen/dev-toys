package io.devtoys.tools.htmlentity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML entity encode/decode. Handles the 5 predefined entities
 * ({@code & < > " '}) for encoding, and all named/numeric entities for decoding.
 *
 * <p>For full W3C entity coverage we'd ship a 2,000-entry lookup table — out of
 * scope here. The most common cases (the XML 1.0 set + &nbsp; + decimal/hex
 * numeric refs) are covered.
 */
public final class HtmlEntityCore {

    private HtmlEntityCore() {}

    /** Encode: replace {@code & < > " '} with their named entities. */
    public static String encode(String plain) {
        if (plain == null || plain.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(plain.length() + 16);
        for (int i = 0; i < plain.length(); i++) {
            char c = plain.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Decode: handle the common named entities plus numeric entities
     * ({@code &#38;} decimal and {@code &#x26;} hex).
     *
     * <p>Unknown named entities are left as-is — browsers tolerate this and we
     * prefer fidelity over being clever.
     */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        String result = NUMERIC_DEC.matcher(encoded).replaceAll(mr -> {
            try {
                int cp = Integer.parseInt(mr.group(1));
                return Matcher.quoteReplacement(new String(Character.toChars(cp)));
            } catch (Exception e) {
                return Matcher.quoteReplacement(mr.group());
            }
        });
        result = NUMERIC_HEX.matcher(result).replaceAll(mr -> {
            try {
                int cp = Integer.parseInt(mr.group(1), 16);
                return Matcher.quoteReplacement(new String(Character.toChars(cp)));
            } catch (Exception e) {
                return Matcher.quoteReplacement(mr.group());
            }
        });
        // Named entities — do & last so we don't double-decode
        result = result
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&nbsp;", "\u00A0")
                .replace("&amp;", "&");
        return result;
    }

    private static final Pattern NUMERIC_DEC = Pattern.compile("&#([0-9]+);");
    private static final Pattern NUMERIC_HEX = Pattern.compile("&#[xX]([0-9a-fA-F]+);");
}
