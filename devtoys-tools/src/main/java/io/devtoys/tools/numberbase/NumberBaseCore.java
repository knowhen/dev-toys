package io.devtoys.tools.numberbase;

/**
 * Pure logic for number base conversion (binary/octal/decimal/hex and anything
 * else Java supports, radix 2..36).
 */
public final class NumberBaseCore {

    private NumberBaseCore() {}

    /**
     * Parse {@code input} as a number in {@code fromRadix}. Accepts common
     * prefixes: {@code 0x}/{@code 0X} for hex, {@code 0b}/{@code 0B} for
     * binary. Also tolerates underscores and whitespace as separators.
     *
     * @throws NumberFormatException if the input doesn't parse
     */
    public static long parse(String input, int fromRadix) {
        if (input == null || input.isBlank()) {
            throw new NumberFormatException("Empty input");
        }
        String s = input.trim().replaceAll("[\\s_]", "");
        // Strip prefixes, but only if they match the declared radix
        if (fromRadix == 16 && (s.startsWith("0x") || s.startsWith("0X"))) {
            s = s.substring(2);
        } else if (fromRadix == 2 && (s.startsWith("0b") || s.startsWith("0B"))) {
            s = s.substring(2);
        }
        if (s.isEmpty()) throw new NumberFormatException("No digits after prefix");
        return Long.parseLong(s, fromRadix);
    }

    /**
     * Format {@code value} in the given base. Uses lowercase for hex and
     * other letter-using radixes; callers can toUpperCase() if they prefer.
     */
    public static String format(long value, int toRadix) {
        return Long.toString(value, toRadix);
    }

    /**
     * Convenience: parse in one base, format in another, in one call.
     *
     * @throws NumberFormatException if the input doesn't parse in {@code fromRadix}
     */
    public static String convert(String input, int fromRadix, int toRadix) {
        return format(parse(input, fromRadix), toRadix);
    }
}
