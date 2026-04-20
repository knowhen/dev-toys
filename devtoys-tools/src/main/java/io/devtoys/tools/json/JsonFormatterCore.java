package io.devtoys.tools.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Pure business logic for JSON formatting. Zero UI dependencies.
 */
public final class JsonFormatterCore {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonFormatterCore() {}

    /**
     * Pretty-print JSON with the requested indent width (spaces).
     *
     * @param json JSON text to format
     * @param indent number of spaces to indent (1..16 recommended)
     * @return formatted JSON
     * @throws JsonProcessingException if the input is not valid JSON
     */
    public static String format(String json, int indent) throws JsonProcessingException {
        if (json == null || json.isBlank()) return "";
        if (indent < 0) indent = 0;
        JsonNode node = MAPPER.readTree(json);
        DefaultPrettyPrinter.Indenter indenter =
                new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        ObjectWriter writer = MAPPER.writer(printer);
        return writer.writeValueAsString(node);
    }

    /**
     * Minify JSON — remove all whitespace, preserving semantics.
     *
     * @throws JsonProcessingException if the input is not valid JSON
     */
    public static String minify(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) return "";
        JsonNode node = MAPPER.readTree(json);
        return MAPPER.writeValueAsString(node);
    }

    /** Returns true iff {@code json} is syntactically valid JSON. */
    public static boolean isValid(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
