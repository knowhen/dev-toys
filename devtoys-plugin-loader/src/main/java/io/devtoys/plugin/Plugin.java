package io.devtoys.plugin;

import io.devtoys.api.IGuiTool;

import java.nio.file.Path;
import java.util.List;

/**
 * A successfully loaded plugin.
 *
 * <p>The {@link #tools} list contains every {@link IGuiTool} found in the
 * plugin's {@code META-INF/services/io.devtoys.api.IGuiTool} (or its
 * {@code module-info}, if the plugin is itself a named module). The host
 * {@code ToolRegistry} merges these with the built-in tools.
 *
 * <p>The {@link #classLoader} is kept so the host can keep a reference — if
 * it were garbage-collected, so would the plugin's classes, causing
 * {@code NoClassDefFoundError} at the worst possible moment.
 *
 * @param manifest   the parsed {@code plugin.json}
 * @param jarPath    the JAR file on disk
 * @param classLoader the ClassLoader that loaded the plugin's classes
 * @param tools      the {@code IGuiTool} implementations discovered in the plugin
 */
public record Plugin(
        PluginManifest manifest,
        Path jarPath,
        ClassLoader classLoader,
        List<IGuiTool> tools
) {}
