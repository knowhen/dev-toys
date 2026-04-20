package io.devtoys.tools.urlcodec;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UrlCodecCoreTest {

    @Test void encode_handlesSpacesAndSpecials() {
        assertEquals("hello+world", UrlCodecCore.encode("hello world"));
        assertEquals("a%3Db%26c%3Dd", UrlCodecCore.encode("a=b&c=d"));
    }

    @Test void encode_preservesPlainAscii() {
        assertEquals("abcABC123", UrlCodecCore.encode("abcABC123"));
    }

    @Test void decode_roundTrips() {
        String plain = "hello world & friends = ?";
        assertEquals(plain, UrlCodecCore.decode(UrlCodecCore.encode(plain)));
    }

    @Test void decode_handlesUtf8() {
        String plain = "你好,世界";
        assertEquals(plain, UrlCodecCore.decode(UrlCodecCore.encode(plain)));
    }

    @Test void emptyInputs_returnEmpty() {
        assertEquals("", UrlCodecCore.encode(""));
        assertEquals("", UrlCodecCore.encode(null));
        assertEquals("", UrlCodecCore.decode(""));
        assertEquals("", UrlCodecCore.decode(null));
    }
}
