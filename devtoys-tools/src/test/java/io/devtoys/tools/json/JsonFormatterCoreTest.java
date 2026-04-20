package io.devtoys.tools.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatterCoreTest {

    @Test
    void format_simpleObject_addsIndentationAndNewlines() throws Exception {
        String out = JsonFormatterCore.format("{\"a\":1,\"b\":2}", 2);
        // Has newlines and 2-space indent before "a"
        assertTrue(out.contains("\n"));
        assertTrue(out.contains("  \"a\""));
    }

    @Test
    void format_respectsIndentWidth() throws Exception {
        String twoSpace = JsonFormatterCore.format("{\"a\":1}", 2);
        String fourSpace = JsonFormatterCore.format("{\"a\":1}", 4);
        assertTrue(twoSpace.contains("  \"a\""));
        assertTrue(fourSpace.contains("    \"a\""));
    }

    @Test
    void format_invalidJson_throws() {
        assertThrows(JsonProcessingException.class,
                () -> JsonFormatterCore.format("{\"a\":}", 2));
    }

    @Test
    void format_blank_returnsEmpty() throws Exception {
        assertEquals("", JsonFormatterCore.format("", 2));
        assertEquals("", JsonFormatterCore.format("   ", 2));
        assertEquals("", JsonFormatterCore.format(null, 2));
    }

    @Test
    void minify_removesWhitespace() throws Exception {
        String minified = JsonFormatterCore.minify("{\n  \"a\" : 1,\n  \"b\" : 2\n}");
        assertEquals("{\"a\":1,\"b\":2}", minified);
    }

    @Test
    void isValid_true_forValidJson() {
        assertTrue(JsonFormatterCore.isValid("{\"a\":1}"));
        assertTrue(JsonFormatterCore.isValid("[1,2,3]"));
        assertTrue(JsonFormatterCore.isValid("\"hello\""));
        assertTrue(JsonFormatterCore.isValid("42"));
    }

    @Test
    void isValid_false_forInvalidJson() {
        assertFalse(JsonFormatterCore.isValid("{\"a\":}"));
        assertFalse(JsonFormatterCore.isValid(""));
        assertFalse(JsonFormatterCore.isValid("not json"));
    }
}
