package io.devtoys.api;

/**
 * Predefined group names mirroring the categories used by the original DevToys.
 *
 * <p>Plugin authors are free to use any string as a {@link ToolMetadata#groupName()},
 * but using these constants makes the tool appear alongside built-ins in the
 * expected category.
 */
public final class PredefinedToolGroups {

    public static final String CONVERTERS = "Converters";
    public static final String ENCODERS_DECODERS = "Encoders / Decoders";
    public static final String FORMATTERS = "Formatters";
    public static final String GENERATORS = "Generators";
    public static final String GRAPHIC = "Graphic";
    public static final String TESTERS = "Testers";
    public static final String TEXT = "Text";

    private PredefinedToolGroups() {
        // no instances
    }
}
