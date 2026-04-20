package io.devtoys.tools.htmlentity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HtmlEntityCoreTest {

    @Test void encode_fiveSpecCharacters() {
        assertEquals("&lt;b&gt;hi&lt;/b&gt;", HtmlEntityCore.encode("<b>hi</b>"));
        assertEquals("a &amp; b", HtmlEntityCore.encode("a & b"));
        assertEquals("&quot;x&quot;", HtmlEntityCore.encode("\"x\""));
        assertEquals("it&#39;s", HtmlEntityCore.encode("it's"));
    }

    @Test void decode_namedEntities() {
        assertEquals("<b>hi</b>", HtmlEntityCore.decode("&lt;b&gt;hi&lt;/b&gt;"));
        assertEquals("a & b", HtmlEntityCore.decode("a &amp; b"));
        assertEquals("\u00A0space", HtmlEntityCore.decode("&nbsp;space"));
    }

    @Test void decode_numericEntities() {
        assertEquals("A", HtmlEntityCore.decode("&#65;"));
        assertEquals("A", HtmlEntityCore.decode("&#x41;"));
        assertEquals("©", HtmlEntityCore.decode("&#169;"));
    }

    @Test void roundTrips() {
        String src = "<html>A & B > C \"quoted\" 'single'</html>";
        assertEquals(src, HtmlEntityCore.decode(HtmlEntityCore.encode(src)));
    }

    @Test void emptyInputs_returnEmpty() {
        assertEquals("", HtmlEntityCore.encode(null));
        assertEquals("", HtmlEntityCore.decode(null));
    }

    @Test void decode_leavesUnknownEntityAsIs() {
        // &fake; isn't a known entity — preserved, not stripped
        assertEquals("&fake;", HtmlEntityCore.decode("&fake;"));
    }
}
