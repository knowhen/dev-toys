package io.devtoys.core;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ServiceContext;
import io.devtoys.api.ToolMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Discovers and holds all available tools — both built-in and plugin-contributed.
 *
 * <p>Built-in tools are found via the standard {@link ServiceLoader} mechanism
 * on the boot module layer: every module that declares
 * {@code provides io.devtoys.api.IGuiTool with ...} contributes.
 *
 * <p>Plugin tools are supplied externally by {@link #discover(ServiceContext, List)}
 * — the caller is responsible for having loaded them (typically through the
 * {@code devtoys-plugin-loader} module). This keeps {@code devtoys-core} free
 * of any plugin-loading machinery.
 *
 * <p>During discovery, {@link IGuiTool#initialize(ServiceContext)} is called
 * on each tool. Tools that throw during initialization are logged and dropped
 * — one bad plugin cannot take down the whole app.
 */
public final class ToolRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ToolRegistry.class);

    private final List<ToolDescriptor> tools;

    private ToolRegistry(List<ToolDescriptor> tools) {
        this.tools = List.copyOf(tools);
    }

    /** Discover only built-in tools (from the boot module layer). */
    public static ToolRegistry discover(ServiceContext context) {
        return discover(context, List.of());
    }

    /**
     * Discover built-in tools plus any externally-provided ones.
     *
     * @param context       host services bag, passed to each tool
     * @param externalTools additional tools (typically from plugins)
     */
    public static ToolRegistry discover(ServiceContext context, List<IGuiTool> externalTools) {
        List<ToolDescriptor> found = new ArrayList<>();
        int builtinCount = 0;
        int pluginCount = 0;

        // Built-in tools: boot-layer ServiceLoader
        for (IGuiTool tool : ServiceLoader.load(IGuiTool.class)) {
            if (register(tool, context, found)) builtinCount++;
        }

        // Plugin tools: passed in explicitly
        for (IGuiTool tool : externalTools) {
            if (register(tool, context, found)) pluginCount++;
        }

        found.sort(Comparator
                .comparing(ToolDescriptor::group)
                .thenComparingInt(d -> d.metadata().order())
                .thenComparing(ToolDescriptor::shortTitle));

        if (pluginCount > 0) {
            LOG.info("Discovered {} tool(s): {} built-in, {} from plugins.",
                    found.size(), builtinCount, pluginCount);
        } else {
            LOG.info("Discovered {} tool(s).", found.size());
        }
        return new ToolRegistry(found);
    }

    /** @return true if the tool was registered successfully */
    private static boolean register(IGuiTool tool, ServiceContext context, List<ToolDescriptor> out) {
        ToolMetadata meta = tool.getClass().getAnnotation(ToolMetadata.class);
        if (meta == null) {
            LOG.warn("Tool {} is registered as a service but missing @ToolMetadata; skipping.",
                    tool.getClass().getName());
            return false;
        }
        try {
            tool.initialize(context);
        } catch (RuntimeException e) {
            LOG.warn("Tool {} failed to initialize; skipping.", tool.getClass().getName(), e);
            return false;
        }
        out.add(new ToolDescriptor(tool, meta, tool.getClass()));
        return true;
    }

    public List<ToolDescriptor> all() {
        return tools;
    }

    public Map<String, List<ToolDescriptor>> byGroup() {
        Map<String, List<ToolDescriptor>> result = new LinkedHashMap<>();
        for (ToolDescriptor d : tools) {
            result.computeIfAbsent(d.group(), k -> new ArrayList<>()).add(d);
        }
        result.replaceAll((k, v) -> Collections.unmodifiableList(v));
        return Collections.unmodifiableMap(result);
    }

    public List<ToolDescriptor> search(String query) {
        if (query == null || query.isBlank()) {
            return tools;
        }
        return tools.stream()
                .filter(d -> d.matches(query))
                .collect(Collectors.toUnmodifiableList());
    }
}
