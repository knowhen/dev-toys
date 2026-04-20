package io.devtoys.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative metadata for a tool.
 *
 * <p>This single annotation is the Java equivalent of the stack of C# attributes
 * DevToys uses ({@code [Export]}, {@code [Name]}, {@code [ToolDisplayInformation]},
 * {@code [Order]}, {@code [NotSearchable]}, {@code [NotFavorable]}, etc.).
 *
 * <h3>Icons</h3>
 * <p>{@link #iconCode()} is an Ikonli icon code. The prefix determines which
 * icon pack is used; all three bundled packs are supported:
 * <ul>
 *   <li>{@code "fluent-xxx-NN"} - Microsoft Fluent UI System Icons (recommended;
 *       matches DevToys look-and-feel).</li>
 *   <li>{@code "fas-xxx"} - FontAwesome 5 Solid.</li>
 *   <li>{@code "mdi2-xxx"} - Material Design Icons v2.</li>
 * </ul>
 * Tool authors pick the pack that best fits their icon. No central registry
 * needs updating.
 *
 * <p>Retained at runtime so that the {@code ToolRegistry} can read it reflectively
 * during {@link java.util.ServiceLoader} discovery.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToolMetadata {

    /**
     * Stable, unique identifier for the tool. By convention this matches the
     * fully-qualified class name for built-in tools, but plugins can use any
     * unique string.
     */
    String name();

    /**
     * Group this tool belongs to in the navigation pane. Use constants from
     * {@link PredefinedToolGroups} for built-in groups, or any string for a
     * custom group.
     */
    String groupName();

    /** Short label shown in the navigation list (e.g. "Base64"). */
    String shortTitle();

    /** Longer title for the tool page header. Defaults to {@link #shortTitle()} if empty. */
    String longTitle() default "";

    /** Short description shown in tooltips and the tool page. */
    String description() default "";

    /**
     * Ikonli icon code (pack is inferred from prefix). See class javadoc for
     * available packs. Defaults to a generic gear so a tool without an icon
     * still renders cleanly.
     */
    String iconCode() default "mdal-code";

    /** Search keywords. Used by the global search feature. */
    String[] searchKeywords() default {};

    /**
     * Display order within the group. Lower values appear first. Ties are
     * broken by {@link #shortTitle()}.
     */
    int order() default 0;

    /** When true, the tool is excluded from search results. */
    boolean notSearchable() default false;

    /** When true, the tool cannot be added to favorites. */
    boolean notFavorable() default false;
}
