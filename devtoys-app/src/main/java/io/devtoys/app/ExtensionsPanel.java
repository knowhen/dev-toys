package io.devtoys.app;

import io.devtoys.plugin.Plugin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static io.devtoys.app.DevToysApp.PLUGIN_DIR;

/**
 * Panel that lists installed plugins.
 *
 * <p>For each plugin, shows its name, version, author, description, and which
 * tools it contributes. Also offers an "Open plugin folder" button so users
 * can easily drop new JARs in (DevToys restart required, matching how DevToys
 * .NET handles this).
 *
 * <p>Future extensions (not in this iteration): an online marketplace, enable/
 * disable toggles, remove-plugin buttons, a link to the plugin's homepage.
 */
final class ExtensionsPanel extends ScrollPane {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionsPanel.class);

    ExtensionsPanel(List<Plugin> plugins) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.setFillWidth(true);

        // Header
        Label title = new Label("Extensions");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label(plugins.isEmpty()
                ? "No plugins installed."
                : "Installed plugins (" + plugins.size() + "):");
        subtitle.getStyleClass().add("text-muted");

        Button openFolder = new Button("Open plugin folder");
        openFolder.setOnAction(e -> openPluginFolder());

        Button refresh = new Button("Refresh (restart required)");
        refresh.setDisable(true);
        refresh.setTooltip(new javafx.scene.control.Tooltip(
                "Hot-reload not yet supported. Restart the app to pick up changes."));

        HBox toolbar = new HBox(10, openFolder, refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        page.getChildren().addAll(title, subtitle, toolbar, new Separator());

        // One card per plugin
        if (plugins.isEmpty()) {
            Label hint = new Label(
                    "Drop plugin JARs into your plugin folder, then restart DevToys.");
            hint.getStyleClass().add("text-muted");
            page.getChildren().add(hint);

            Label path = new Label(PLUGIN_DIR.toString());
            path.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
            page.getChildren().add(path);
        } else {
            for (Plugin p : plugins) {
                page.getChildren().add(buildCard(p));
            }
        }

        // Make the scroll pane work smoothly
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setContent(page);
    }

    private Region buildCard(Plugin p) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: -color-bg-subtle;"
              + " -fx-background-radius: 6;"
              + " -fx-border-color: -color-border-default;"
              + " -fx-border-radius: 6;"
              + " -fx-border-width: 1;");

        Label name = new Label(p.manifest().name());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label meta = new Label(
                "v" + p.manifest().version()
                + (p.manifest().author().isBlank() ? "" : "  •  by " + p.manifest().author())
                + "  •  " + p.manifest().id());
        meta.getStyleClass().add("text-muted");
        meta.setStyle("-fx-font-size: 11px;");

        HBox header = new HBox(name);
        HBox.setHgrow(header, Priority.ALWAYS);
        card.getChildren().addAll(header, meta);

        if (!p.manifest().description().isBlank()) {
            Label desc = new Label(p.manifest().description());
            desc.setWrapText(true);
            card.getChildren().add(desc);
        }

        if (!p.tools().isEmpty()) {
            StringBuilder toolList = new StringBuilder("Tools: ");
            for (int i = 0; i < p.tools().size(); i++) {
                if (i > 0) toolList.append(", ");
                var meta2 = p.tools().get(i).getClass().getAnnotation(
                        io.devtoys.api.ToolMetadata.class);
                toolList.append(meta2 != null ? meta2.shortTitle()
                                              : p.tools().get(i).getClass().getSimpleName());
            }
            Label tools = new Label(toolList.toString());
            tools.getStyleClass().add("text-muted");
            tools.setStyle("-fx-font-size: 11px;");
            card.getChildren().add(tools);
        }

        Label source = new Label(p.jarPath().getFileName().toString());
        source.getStyleClass().add("text-muted");
        source.setStyle("-fx-font-size: 10px; -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
        card.getChildren().add(source);

        return card;
    }

    private void openPluginFolder() {
        try {
            if (!Files.isDirectory(PLUGIN_DIR)) {
                Files.createDirectories(PLUGIN_DIR);
            }
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(PLUGIN_DIR.toFile());
            } else {
                LOG.warn("Desktop OPEN not supported on this platform; path is {}", PLUGIN_DIR);
            }
        } catch (IOException e) {
            LOG.warn("Failed to open plugin folder {}", PLUGIN_DIR, e);
        }
    }
}
