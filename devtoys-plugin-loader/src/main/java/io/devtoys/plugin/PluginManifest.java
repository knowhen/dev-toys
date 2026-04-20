package io.devtoys.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The contents of a plugin's {@code plugin.json}.
 *
 * <p>Schema (all fields required except where noted):
 * <pre>{@code
 * {
 *   "id":          "io.example.url-codec",
 *   "name":        "URL Codec",
 *   "version":     "1.0.0",
 *   "author":      "John Doe",
 *   "description": "Optional longer description.",
 *   "homepage":    "https://example.com/plugin",
 *   "minHostVersion": "0.1.0"
 * }
 * }</pre>
 *
 * <p>{@code id} uses reverse-domain notation to avoid collisions between
 * plugins from different authors. It is used as the install key and shown in
 * the Extensions panel.
 */
public record PluginManifest(
        String id,
        String name,
        String version,
        String author,
        String description,
        String homepage,
        String minHostVersion
) {
    @JsonCreator
    public PluginManifest(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("author") String author,
            @JsonProperty("description") String description,
            @JsonProperty("homepage") String homepage,
            @JsonProperty("minHostVersion") String minHostVersion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("plugin.json: 'id' is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("plugin.json: 'name' is required");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("plugin.json: 'version' is required");
        }
        this.id = id;
        this.name = name;
        this.version = version;
        this.author = author == null ? "" : author;
        this.description = description == null ? "" : description;
        this.homepage = homepage == null ? "" : homepage;
        this.minHostVersion = minHostVersion == null ? "" : minHostVersion;
    }
}
