package io.devtoys.tools.password;

import java.security.SecureRandom;

/**
 * Secure password generation using {@link SecureRandom}.
 *
 * <p>Character sets can be combined: at least one set must be enabled or
 * an empty string is returned.
 */
public final class PasswordCore {

    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGITS = "0123456789";
    public static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?/";

    /** Password generation options. */
    public record Options(
            boolean lower,
            boolean upper,
            boolean digits,
            boolean symbols,
            boolean excludeAmbiguous
    ) {
        public static final Options DEFAULT = new Options(true, true, true, false, true);
    }

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordCore() {}

    /**
     * Generate a random password of the given length.
     *
     * @throws IllegalArgumentException if no character set is enabled, or length &lt; 1
     */
    public static String generate(int length, Options options) {
        if (length < 1) throw new IllegalArgumentException("length must be >= 1");
        if (options == null) options = Options.DEFAULT;

        StringBuilder alphabet = new StringBuilder();
        if (options.lower())   alphabet.append(options.excludeAmbiguous() ? strip(LOWER, "lo") : LOWER);
        if (options.upper())   alphabet.append(options.excludeAmbiguous() ? strip(UPPER, "IO") : UPPER);
        if (options.digits())  alphabet.append(options.excludeAmbiguous() ? strip(DIGITS, "01") : DIGITS);
        if (options.symbols()) alphabet.append(SYMBOLS);

        if (alphabet.length() == 0) {
            throw new IllegalArgumentException("At least one character set must be enabled");
        }

        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = alphabet.charAt(RNG.nextInt(alphabet.length()));
        }
        return new String(chars);
    }

    /** Convenience: generate multiple passwords at once. */
    public static String[] generateMany(int count, int length, Options options) {
        if (count < 0) throw new IllegalArgumentException("count must be >= 0");
        String[] result = new String[count];
        for (int i = 0; i < count; i++) result[i] = generate(length, options);
        return result;
    }

    private static String strip(String src, String ambiguous) {
        StringBuilder out = new StringBuilder(src.length());
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (ambiguous.indexOf(c) < 0) out.append(c);
        }
        return out.toString();
    }
}
