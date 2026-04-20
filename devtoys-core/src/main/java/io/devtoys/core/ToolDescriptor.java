package io.devtoys.core;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ToolMetadata;

import java.util.Objects;

/**
 * A tool instance paired with its {@link ToolMetadata}.
 *
 * <p>This is what the UI layer consumes — it never talks to {@link IGuiTool}
 * implementations directly, only through descriptors. That keeps a clean
 * boundary if we later add tool virtualization (create on first open instead of
 * upfront), lazy loading, per-plugin classloader isolation, etc.
 */
public record ToolDescriptor(IGuiTool tool, ToolMetadata metadata, Class<?> toolClass) {

    public ToolDescriptor {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(toolClass, "toolClass");
    }

    /** Convenience: the short title used in the navigation list. */
    public String shortTitle() {
        return metadata.shortTitle();
    }

    /** Convenience: the long title for the tool page, falling back to short. */
    public String longTitle() {
        String longTitle = metadata.longTitle();
        return (longTitle == null || longTitle.isBlank()) ? metadata.shortTitle() : longTitle;
    }

    /** Convenience: the group this tool is placed in. */
    public String group() {
        return metadata.groupName();
    }

    /** Returns true if the query (case-insensitive) matches title, description or keywords. */
    public boolean matches(String query) {
        if (metadata.notSearchable() || query == null || query.isBlank()) {
            return false;
        }
        String q = query.toLowerCase();
        if (metadata.shortTitle().toLowerCase().contains(q)) return true;
        if (metadata.longTitle().toLowerCase().contains(q)) return true;
        if (metadata.description().toLowerCase().contains(q)) return true;
        for (String kw : metadata.searchKeywords()) {
            if (kw.toLowerCase().contains(q)) return true;
        }
        return false;
    }
}
