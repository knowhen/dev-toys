package io.devtoys.api;

import java.util.Optional;

/**
 * Minimal key/value settings abstraction.
 *
 * <p>Keys are plain strings. Tools are responsible for namespacing (e.g.
 * {@code "json.indent.width"}) to avoid collisions. A proper implementation
 * persists to disk; the skeleton ships an in-memory implementation that is
 * fine for the first demos.
 */
public interface SettingsStore {

    Optional<String> get(String key);

    void set(String key, String value);

    default String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    default void setBoolean(String key, boolean value) {
        set(key, Boolean.toString(value));
    }
}
