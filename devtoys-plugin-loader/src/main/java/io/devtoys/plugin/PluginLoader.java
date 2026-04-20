package io.devtoys.plugin;

import java.nio.file.Path;
import java.util.List;

/**
 * Discovers and loads plugin JARs from a directory.
 *
 * <p>Different implementations can use different classloader strategies:
 * <ul>
 *   <li>{@link SharedClassLoaderPluginLoader} — all plugins share one
 *       {@code URLClassLoader}. Simple, matches DevToys .NET behaviour,
 *       but no isolation.</li>
 *   <li>(Future) IsolatedClassLoaderPluginLoader — one classloader per
 *       plugin. More complex but enables unloading and dependency isolation.</li>
 * </ul>
 */
public interface PluginLoader {

    /**
     * Load every {@code *.jar} file in {@code pluginDir} as a plugin.
     *
     * <p>If {@code pluginDir} does not exist, returns an empty list.
     * Individual plugins that fail to load (missing/invalid manifest,
     * class-loading errors) are logged and skipped — one bad plugin cannot
     * take down the whole app.
     *
     * @param pluginDir directory to scan, typically {@code ~/.devtoys/plugins}
     * @return one {@link Plugin} per successfully loaded JAR
     */
    List<Plugin> loadAll(Path pluginDir);
}
