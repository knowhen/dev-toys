/**
 * Plugin loader module.
 *
 * <p>Scans a plugin directory for JAR files, parses each JAR's {@code plugin.json}
 * manifest, loads its classes via a {@link java.net.URLClassLoader}, and discovers
 * {@link io.devtoys.api.IGuiTool} implementations via {@link java.util.ServiceLoader}.
 *
 * <p>The default {@link io.devtoys.plugin.SharedClassLoaderPluginLoader} uses a
 * single shared {@code URLClassLoader} for all plugins — matching DevToys .NET's
 * MEF behaviour (all plugins in one AppDomain). Future implementations may use
 * per-plugin classloaders for isolation.
 */
module io.devtoys.plugin.loader {
    requires io.devtoys.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;

    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.plugin;
}
