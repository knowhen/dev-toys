package io.devtoys.core.services;

import io.devtoys.api.SettingsStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link SettingsStore}. Values are lost at application exit.
 *
 * <p>Adequate for the initial skeleton. A real implementation should persist
 * to {@code ~/.devtoys/settings.properties} or an OS-specific location.
 */
public final class InMemorySettingsStore implements SettingsStore {

    private final Map<String, String> values = new ConcurrentHashMap<>();

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public void set(String key, String value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }
}
