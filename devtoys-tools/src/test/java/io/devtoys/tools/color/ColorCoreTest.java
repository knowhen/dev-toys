package io.devtoys.tools.color;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColorCoreTest {

    @Test void parseHex_sixDigit() {
        ColorCore.Rgb c = ColorCore.parseHex("#ff0080");
        assertEquals(255, c.r()); assertEquals(0, c.g()); assertEquals(128, c.b());
    }

    @Test void parseHex_threeDigitShorthand() {
        ColorCore.Rgb c = ColorCore.parseHex("#f08");
        assertEquals(255, c.r()); assertEquals(0, c.g()); assertEquals(136, c.b());
    }

    @Test void parseHex_caseInsensitiveAndWithoutHash() {
        assertEquals(ColorCore.parseHex("#FF0080"), ColorCore.parseHex("ff0080"));
    }

    @Test void parseRgb_standardForm() {
        ColorCore.Rgb c = ColorCore.parseRgb("rgb(255, 0, 128)");
        assertEquals(255, c.r()); assertEquals(0, c.g()); assertEquals(128, c.b());
    }

    @Test void toHex_roundTrips() {
        ColorCore.Rgb c = new ColorCore.Rgb(255, 0, 128);
        assertEquals("#ff0080", ColorCore.toHex(c));
    }

    @Test void toHsl_pureRedIsZeroDegrees() {
        ColorCore.Hsl h = ColorCore.toHsl(new ColorCore.Rgb(255, 0, 0));
        assertEquals(0.0, h.h(), 0.5);
        assertEquals(100.0, h.s(), 0.5);
        assertEquals(50.0, h.l(), 0.5);
    }

    @Test void invalidHex_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorCore.parseHex("gg0080"));
        assertThrows(IllegalArgumentException.class, () -> ColorCore.parseHex("#12345"));
        assertThrows(IllegalArgumentException.class, () -> ColorCore.parseHex(null));
    }

    @Test void rgbComponentOutOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ColorCore.Rgb(300, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ColorCore.Rgb(-1, 0, 0));
    }
}
