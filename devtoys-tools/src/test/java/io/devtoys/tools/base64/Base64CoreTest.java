package io.devtoys.tools.base64;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64CoreTest {

    @Test
    void encode_simpleAscii() {
        assertEquals("aGVsbG8=", Base64Core.encode("hello"));
    }

    @Test
    void encode_empty() {
        assertEquals("", Base64Core.encode(""));
    }

    @Test
    void encode_null_treatedAsEmpty() {
        assertEquals("", Base64Core.encode(null));
    }

    @Test
    void encode_utf8Multibyte() {
        // "你好" is 6 UTF-8 bytes, which Base64-encodes to 8 chars
        String encoded = Base64Core.encode("你好");
        assertEquals("5L2g5aW9", encoded);
    }

    @Test
    void decode_simpleAscii() {
        assertEquals("hello", Base64Core.decode("aGVsbG8="));
    }

    @Test
    void decode_trimsWhitespace() {
        assertEquals("hello", Base64Core.decode("  aGVsbG8=\n"));
    }

    @Test
    void decode_invalid_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Base64Core.decode("!!!not-base64!!!"));
    }

    @Test
    void roundTrip_preservesContent() {
        String original = "The quick brown fox jumps over the lazy dog 🦊";
        assertEquals(original, Base64Core.decode(Base64Core.encode(original)));
    }
}
