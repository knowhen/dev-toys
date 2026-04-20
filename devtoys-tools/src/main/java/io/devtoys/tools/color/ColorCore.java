package io.devtoys.tools.color;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure logic for color format conversion between HEX, RGB and HSL.
 *
 * <p>Internally colors are represented as an immutable {@link Rgb} record
 * with components in 0..255. All parsers return {@code Rgb}; all formatters
 * take {@code Rgb} as input. This is the simplest pivot point.
 */
public final class ColorCore {

    /** 24-bit RGB color with components in 0..255. */
    public record Rgb(int r, int g, int b) {
        public Rgb {
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
                throw new IllegalArgumentException("RGB components must be in 0..255");
        }
    }

    /** HSL: H in 0..360, S and L in 0..100 (percent). */
    public record Hsl(double h, double s, double l) {}

    private ColorCore() {}

    // --- Parsers ---

    /**
     * Parse HEX input. Accepts {@code #rgb}, {@code #rrggbb},
     * {@code rgb}, {@code rrggbb}. Case-insensitive.
     */
    public static Rgb parseHex(String hex) {
        if (hex == null) throw new IllegalArgumentException("hex is null");
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() == 3) {
            // expand "abc" -> "aabbcc"
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 3; i++) { char c = s.charAt(i); sb.append(c).append(c); }
            s = sb.toString();
        }
        if (s.length() != 6 || !s.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Not a valid hex color: " + hex);
        }
        int r = Integer.parseInt(s.substring(0, 2), 16);
        int g = Integer.parseInt(s.substring(2, 4), 16);
        int b = Integer.parseInt(s.substring(4, 6), 16);
        return new Rgb(r, g, b);
    }

    /** Parse {@code rgb(255, 0, 128)} form. */
    public static Rgb parseRgb(String rgb) {
        Matcher m = RGB_PATTERN.matcher(rgb == null ? "" : rgb.trim());
        if (!m.matches()) throw new IllegalArgumentException("Not rgb() syntax: " + rgb);
        return new Rgb(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)));
    }

    // --- Formatters ---

    public static String toHex(Rgb c) {
        return String.format("#%02x%02x%02x", c.r(), c.g(), c.b());
    }

    public static String toRgbString(Rgb c) {
        return "rgb(" + c.r() + ", " + c.g() + ", " + c.b() + ")";
    }

    public static String toHslString(Rgb c) {
        Hsl hsl = toHsl(c);
        return String.format(Locale.ROOT, "hsl(%.0f, %.0f%%, %.0f%%)",
                hsl.h(), hsl.s(), hsl.l());
    }

    // --- RGB ↔ HSL ---

    public static Hsl toHsl(Rgb c) {
        double r = c.r() / 255.0, g = c.g() / 255.0, b = c.b() / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double l = (max + min) / 2.0;
        double h, s;
        if (max == min) {
            h = 0; s = 0;  // achromatic
        } else {
            double d = max - min;
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            if (max == r)      h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else               h = (r - g) / d + 4;
            h *= 60;
        }
        return new Hsl(h, s * 100, l * 100);
    }

    private static final Pattern RGB_PATTERN =
            Pattern.compile("^rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
}
