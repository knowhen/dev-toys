package io.devtoys.api;

import java.util.Objects;

/**
 * Host-provided services available to tools.
 *
 * <p>Deliberately minimal and hand-rolled. This replaces the role of a DI
 * container (MEF in the original DevToys, Guice in a typical Java port)
 * without any framework dependency. Tools that need services call the
 * corresponding getter; tools that don't simply ignore it.
 *
 * <p>Adding a new host service means: (1) adding a field and getter here,
 * (2) wiring it up in the {@code Launcher}. Tools stay untouched unless
 * they actually want the new service.
 *
 * <p>If the service list ever grows past ~10 entries or services start
 * depending on each other, this is the cue to switch to Google Guice.
 */
public final class ServiceContext {

    private final ClipboardService clipboard;
    private final SettingsStore settings;

    public ServiceContext(ClipboardService clipboard, SettingsStore settings) {
        this.clipboard = Objects.requireNonNull(clipboard, "clipboard");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public ClipboardService clipboard() {
        return clipboard;
    }

    public SettingsStore settings() {
        return settings;
    }
}
