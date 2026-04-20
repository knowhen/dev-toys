package io.devtoys.core.services;

import io.devtoys.api.SettingsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link SettingsStore} backed by a {@code .properties} file under
 * {@code ~/.devtoys/settings.properties}.
 *
 * <p>Implementation notes:
 * <ul>
 *   <li>All {@code get} calls are served from an in-memory {@link Properties}
 *       cache loaded once at construction. This keeps reads cheap.</li>
 *   <li>Each {@code set} writes through to disk on a background (daemon)
 *       saver. We coalesce multiple rapid writes into a single flush using a
 *       pending-write flag, so rapid-fire updates don't thrash the disk.</li>
 *   <li>Failure to persist is logged but not thrown. Tools never care whether
 *       a write succeeded — the value is already in memory for this session.</li>
 * </ul>
 */
public final class FileSettingsStore implements SettingsStore {

    private static final Logger LOG = LoggerFactory.getLogger(FileSettingsStore.class);

    private final Path file;
    private final Properties cache = new Properties();
    private final AtomicBoolean writePending = new AtomicBoolean(false);
    private final Object writeLock = new Object();

    public FileSettingsStore() {
        this(defaultFile());
    }

    FileSettingsStore(Path file) {
        this.file = file;
        load();
    }

    /** Default location: {@code ~/.devtoys/settings.properties}. */
    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".devtoys", "settings.properties");
    }

    private void load() {
        if (!Files.exists(file)) {
            LOG.debug("No settings file at {}, starting with empty settings.", file);
            return;
        }
        try (var in = Files.newInputStream(file)) {
            cache.load(in);
            LOG.debug("Loaded {} setting(s) from {}.", cache.size(), file);
        } catch (IOException e) {
            LOG.warn("Failed to load settings from {}: {}", file, e.getMessage());
        }
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.getProperty(key));
    }

    @Override
    public void set(String key, String value) {
        synchronized (cache) {
            if (value == null) {
                cache.remove(key);
            } else {
                cache.setProperty(key, value);
            }
        }
        scheduleFlush();
    }

    /** Request a flush to disk. Multiple rapid calls collapse into one write. */
    private void scheduleFlush() {
        if (writePending.compareAndSet(false, true)) {
            Thread t = new Thread(this::flushNow, "devtoys-settings-writer");
            t.setDaemon(true);
            t.start();
        }
    }

    private void flushNow() {
        try {
            // Small coalescing window: if more set() calls come in quickly, they
            // all land in the cache before we snapshot it below.
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        writePending.set(false);

        synchronized (writeLock) {
            try {
                Files.createDirectories(file.getParent());
                Properties snapshot;
                synchronized (cache) {
                    snapshot = (Properties) cache.clone();
                }
                try (var out = Files.newOutputStream(file)) {
                    snapshot.store(out, "DevToys Java settings");
                }
                LOG.trace("Flushed {} setting(s) to {}.", snapshot.size(), file);
            } catch (IOException e) {
                LOG.warn("Failed to persist settings to {}: {}", file, e.getMessage());
            }
        }
    }
}
