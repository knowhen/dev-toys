package io.devtoys.app;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import io.devtoys.api.ClipboardService;
import io.devtoys.api.IGuiTool;
import io.devtoys.api.ServiceContext;
import io.devtoys.api.SettingsStore;
import io.devtoys.core.ToolRegistry;
import io.devtoys.core.services.FileSettingsStore;
import io.devtoys.core.services.JavaFxClipboardService;
import io.devtoys.core.tasks.BackgroundTaskRunner;
import io.devtoys.plugin.Plugin;
import io.devtoys.plugin.PluginLoader;
import io.devtoys.plugin.SharedClassLoaderPluginLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main JavaFX {@link Application}.
 *
 * <p>Responsibilities, in order:
 * <ol>
 *   <li>Wire up host services into a {@link ServiceContext}.</li>
 *   <li>Load third-party plugins from {@code ~/.devtoys/plugins/}.</li>
 *   <li>Discover all tools (built-in + plugin-contributed).</li>
 *   <li>Install the initial AtlantaFX theme from persisted preference.</li>
 *   <li>Build and show the {@link MainWindow}.</li>
 * </ol>
 */
public final class DevToysApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(DevToysApp.class);

    public static final String SETTING_DARK_MODE = "app.darkMode";

    /** User-visible plugin directory. Matches DevToys .NET's convention. */
    public static final Path PLUGIN_DIR =
            Paths.get(System.getProperty("user.home"), ".devtoys", "plugins");

    private SettingsStore settings;
    private List<Plugin> plugins = List.of();

    @Override
    public void start(Stage stage) {
        long t0 = System.currentTimeMillis();
        LOG.info("Starting DevToys Java…");

        // 1. Host services (persisted settings + real clipboard)
        ClipboardService clipboard = new JavaFxClipboardService();
        settings = new FileSettingsStore();
        ServiceContext context = new ServiceContext(clipboard, settings);

        // 2. Load third-party plugins
        PluginLoader loader = new SharedClassLoaderPluginLoader();
        plugins = loader.loadAll(PLUGIN_DIR);
        List<IGuiTool> pluginTools = new ArrayList<>();
        for (Plugin p : plugins) pluginTools.addAll(p.tools());

        // 3. Discover all tools (built-in + from plugins)
        ToolRegistry registry = ToolRegistry.discover(context, pluginTools);

        // 4. Theme from persisted setting — default dark on first launch
        boolean dark = settings.getBoolean(SETTING_DARK_MODE, true);
        applyTheme(dark);

        // 5. Main window
        MainWindow window = new MainWindow(registry, plugins, this::toggleTheme, dark);
        Scene scene = new Scene(window, 1100, 720);
        stage.setScene(scene);
        stage.setTitle("DevToys Java");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();

        LOG.info("Startup completed in {} ms.", System.currentTimeMillis() - t0);
    }

    @Override
    public void stop() {
        BackgroundTaskRunner.shutdown();
    }

    private void toggleTheme() {
        boolean nowDark = !settings.getBoolean(SETTING_DARK_MODE, true);
        settings.setBoolean(SETTING_DARK_MODE, nowDark);
        applyTheme(nowDark);
        LOG.debug("Theme switched to {}.", nowDark ? "dark" : "light");
    }

    private void applyTheme(boolean dark) {
        setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
