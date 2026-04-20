package io.devtoys.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devtoys.api.IGuiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Default plugin loader: loads every JAR in the plugin directory through a
 * single shared {@link URLClassLoader}.
 *
 * <p>Matches DevToys .NET's MEF-style "all plugins in one AppDomain" model:
 * plugins can see each other's classes, share singletons, and use the same
 * Jackson/SLF4J instance as the host. The downsides are no isolation (plugin
 * A's bug may affect plugin B) and no runtime unloading — acceptable for an
 * MVP, swappable later by a different {@link PluginLoader} implementation.
 *
 * <h2>What counts as a plugin</h2>
 * <ol>
 *   <li>A {@code .jar} file directly under {@code pluginDir}.</li>
 *   <li>The JAR's root contains a {@code plugin.json} manifest
 *       (parsed into {@link PluginManifest}).</li>
 *   <li>The JAR contains a {@code META-INF/services/io.devtoys.api.IGuiTool}
 *       file listing the fully-qualified tool classes, OR (if the JAR is
 *       itself a named Java module) the plugin declares
 *       {@code provides io.devtoys.api.IGuiTool with ...} in its
 *       {@code module-info}. Either form is fine — both are found by
 *       {@link ServiceLoader#load(Class, ClassLoader)}.</li>
 * </ol>
 *
 * <h2>Classloader hierarchy</h2>
 * <pre>
 *   Host ClassLoader (system modules + host modules)
 *         ^
 *         |
 *   URLClassLoader over [plugin1.jar, plugin2.jar, ...]   &lt;-- shared
 *         ^
 *         |
 *   (plugin classes resolve here first, then delegate up)
 * </pre>
 */
public final class SharedClassLoaderPluginLoader implements PluginLoader {

    private static final Logger LOG = LoggerFactory.getLogger(SharedClassLoaderPluginLoader.class);
    private static final String MANIFEST_RESOURCE = "plugin.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<Plugin> loadAll(Path pluginDir) {
        if (pluginDir == null || !Files.isDirectory(pluginDir)) {
            LOG.debug("Plugin directory {} does not exist; no plugins loaded.", pluginDir);
            return List.of();
        }

        // Step 1 — collect all *.jar files
        List<Path> jars = new ArrayList<>();
        try (Stream<Path> stream = Files.list(pluginDir)) {
            stream.filter(p -> p.toString().endsWith(".jar"))
                  .sorted()
                  .forEach(jars::add);
        } catch (Exception e) {
            LOG.warn("Failed to scan plugin directory {}: {}", pluginDir, e.getMessage());
            return List.of();
        }
        if (jars.isEmpty()) {
            LOG.debug("No plugin JARs found in {}", pluginDir);
            return List.of();
        }

        // Step 2 — parse each JAR's manifest (any failing JARs are dropped
        // from the set before we even build the classloader, so a bad JAR
        // cannot affect the others).
        List<JarWithManifest> parsed = new ArrayList<>();
        for (Path jar : jars) {
            PluginManifest manifest = readManifest(jar);
            if (manifest != null) {
                parsed.add(new JarWithManifest(jar, manifest));
            }
        }
        if (parsed.isEmpty()) {
            return List.of();
        }

        // Step 3 — build the shared URLClassLoader over all surviving JARs.
        URL[] urls = parsed.stream()
                .map(jm -> {
                    try {
                        return jm.jar.toUri().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);

        URLClassLoader sharedLoader = new URLClassLoader(
                "devtoys-plugins",
                urls,
                SharedClassLoaderPluginLoader.class.getClassLoader());

        // Step 4 — use ServiceLoader to find every IGuiTool across all JARs.
        // Then, for each plugin JAR, figure out which tools came from it.
        // We do this by matching each tool's CodeSource to the plugin's JAR URL.
        List<IGuiTool> allTools = new ArrayList<>();
        ServiceLoader<IGuiTool> sl = ServiceLoader.load(IGuiTool.class, sharedLoader);
        for (IGuiTool tool : sl) {
            // Filter out tools that came from the host itself (those are the
            // built-in tools, which the host discovers separately via its
            // own boot-layer ServiceLoader call).
            if (tool.getClass().getClassLoader() == sharedLoader
                    || isDescendantOf(tool.getClass().getClassLoader(), sharedLoader)) {
                allTools.add(tool);
            }
        }

        // Step 5 — group tools by source JAR.
        List<Plugin> plugins = new ArrayList<>();
        for (JarWithManifest jm : parsed) {
            List<IGuiTool> forThisPlugin = new ArrayList<>();
            for (IGuiTool tool : allTools) {
                if (comesFrom(tool, jm.jar)) {
                    forThisPlugin.add(tool);
                }
            }
            if (forThisPlugin.isEmpty()) {
                LOG.warn("Plugin {} ({}) loaded but contributed no IGuiTool services. "
                         + "Does it have META-INF/services/io.devtoys.api.IGuiTool?",
                        jm.manifest.name(), jm.jar.getFileName());
            }
            plugins.add(new Plugin(jm.manifest, jm.jar, sharedLoader, List.copyOf(forThisPlugin)));
            LOG.info("Loaded plugin: {} v{} ({} tool(s))",
                    jm.manifest.name(), jm.manifest.version(), forThisPlugin.size());
        }

        LOG.info("Plugin loading complete: {} plugin(s), {} tool(s) total.",
                plugins.size(), allTools.size());
        return List.copyOf(plugins);
    }

    /** Read plugin.json from the given JAR, or return null on error. */
    private static PluginManifest readManifest(Path jar) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            JarEntry entry = jf.getJarEntry(MANIFEST_RESOURCE);
            if (entry == null) {
                LOG.warn("Plugin {} has no {} — skipping.", jar.getFileName(), MANIFEST_RESOURCE);
                return null;
            }
            try (InputStream in = jf.getInputStream(entry)) {
                return MAPPER.readValue(in, PluginManifest.class);
            }
        } catch (Exception e) {
            LOG.warn("Failed to read {} from {}: {}",
                    MANIFEST_RESOURCE, jar.getFileName(), e.getMessage());
            return null;
        }
    }

    /** Check whether {@code tool}'s class was loaded from {@code jar}. */
    private static boolean comesFrom(IGuiTool tool, Path jar) {
        try {
            var codeSource = tool.getClass().getProtectionDomain().getCodeSource();
            if (codeSource == null) return false;
            URL location = codeSource.getLocation();
            if (location == null) return false;
            return location.toString().equals(jar.toUri().toURL().toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isDescendantOf(ClassLoader cl, ClassLoader ancestor) {
        while (cl != null) {
            if (cl == ancestor) return true;
            cl = cl.getParent();
        }
        return false;
    }

    private record JarWithManifest(Path jar, PluginManifest manifest) {}
}
