package io.devtoys.app;

import io.devtoys.core.ToolDescriptor;
import io.devtoys.core.ToolRegistry;
import io.devtoys.plugin.Plugin;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The primary application shell.
 *
 * <p>Layout, from top to bottom:
 * <pre>
 *   +-------------------------------------------------------+
 *   |  [🔍 Search...]                             [🌓 theme] |  &lt;-- top bar
 *   +-------------------------------------------------------+
 *   |         |                                             |
 *   |  Tools  |             Active tool view                |
 *   | (tree)  |                                             |
 *   |         |                                             |
 *   +-------------------------------------------------------+
 * </pre>
 *
 * <p>Icons use Ikonli's {@link FontIcon#setIconLiteral(String)}, which throws
 * {@link IllegalArgumentException} for unknown codes. Every use here is wrapped
 * in a try/catch that falls back to "no icon" rather than trying another code —
 * so a bad icon code logs a warning but never crashes the app.
 */
final class MainWindow extends BorderPane {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    private final TreeView<Object> tree;
    private final StackPane content;
    private final List<Plugin> plugins;

    MainWindow(ToolRegistry registry, List<Plugin> plugins, Runnable onToggleTheme, boolean isDarkInitially) {
        this.plugins = plugins == null ? List.of() : plugins;
        // --- Top bar ---
        TextField search = new TextField();
        search.setPromptText("Search tools…");
        HBox.setHgrow(search, Priority.ALWAYS);

        Button themeBtn = new Button();
        themeBtn.setGraphic(themeIcon(isDarkInitially));
        themeBtn.setTooltip(new Tooltip(
                isDarkInitially ? "Switch to light theme" : "Switch to dark theme"));
        // Track current mode in user-data so we don't depend on icon-literal round-tripping
        themeBtn.setUserData(isDarkInitially);
        themeBtn.setOnAction(e -> {
            onToggleTheme.run();
            boolean wasDark = (Boolean) themeBtn.getUserData();
            boolean nowDark = !wasDark;
            themeBtn.setUserData(nowDark);
            themeBtn.setGraphic(themeIcon(nowDark));
            themeBtn.getTooltip().setText(nowDark ? "Switch to light theme" : "Switch to dark theme");
        });

        HBox topBar = new HBox(8, searchIcon(), search, themeBtn);
        topBar.setPadding(new Insets(10, 12, 10, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("tool-bar");

        // --- Left nav ---
        tree = new TreeView<>();
        tree.setShowRoot(false);
        tree.setCellFactory(tv -> new NavCell());
        rebuildTree(registry.byGroup());

        VBox left = new VBox(tree);
        VBox.setVgrow(tree, Priority.ALWAYS);
        left.setPrefWidth(260);
        left.setMinWidth(180);

        // --- Content ---
        Label welcome = new Label("Select a tool on the left to begin.");
        welcome.getStyleClass().add("text-muted");
        content = new StackPane(welcome);
        content.setPadding(new Insets(0));

        SplitPane split = new SplitPane(left, content);
        split.setDividerPositions(0.22);
        SplitPane.setResizableWithParent(left, false);

        setTop(topBar);
        setCenter(split);

        // Wire: tree selection → swap content
        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if (n != null && n.getValue() instanceof ToolDescriptor d) {
                showTool(d);
            } else if (n != null && n.getValue() instanceof ExtensionsEntry) {
                content.getChildren().setAll(new ExtensionsPanel(this.plugins));
            }
        });

        // Wire: search → rebuild tree
        ChangeListener<String> searchListener = (o, a, q) -> {
            if (q == null || q.isBlank()) {
                rebuildTree(registry.byGroup());
            } else {
                List<ToolDescriptor> matches = registry.search(q);
                rebuildTree(groupOf(matches));
            }
        };
        search.textProperty().addListener(searchListener);

        // Auto-select first tool
        Platform.runLater(() -> {
            if (!registry.all().isEmpty()) {
                selectFirstTool();
            }
        });
    }

    /**
     * Create a FontIcon for the given literal. Returns {@code null} (no icon)
     * if the code doesn't resolve, instead of throwing. Callers decide whether
     * to substitute text or just render nothing.
     */
    private static FontIcon safeIcon(String literal, int size) {
        if (literal == null || literal.isBlank()) return null;
        try {
            FontIcon ic = new FontIcon();
            ic.setIconLiteral(literal);
            ic.setIconSize(size);
            return ic;
        } catch (Exception e) {
            LOG.warn("Unknown icon code '{}'; rendering without icon.", literal);
            return null;
        }
    }

    private static Node searchIcon() {
        FontIcon ic = safeIcon("mdmz-search", 14);
        return ic != null ? ic : new Label("");
    }

    private static Node themeIcon(boolean isDark) {
        // When dark, show a sun icon (the action is "go light"); and vice-versa
        FontIcon ic = safeIcon(isDark ? "mdmz-wb_sunny" : "mdmz-nights_stay", 14);
        if (ic != null) return ic;
        // Fallback to plain text
        return new Label(isDark ? "Light" : "Dark");
    }

    private Map<String, List<ToolDescriptor>> groupOf(List<ToolDescriptor> list) {
        java.util.LinkedHashMap<String, List<ToolDescriptor>> r = new java.util.LinkedHashMap<>();
        for (ToolDescriptor d : list) {
            r.computeIfAbsent(d.group(), k -> new java.util.ArrayList<>()).add(d);
        }
        return r;
    }

    private void rebuildTree(Map<String, List<ToolDescriptor>> byGroup) {
        TreeItem<Object> root = new TreeItem<>("root");
        for (Map.Entry<String, List<ToolDescriptor>> e : byGroup.entrySet()) {
            TreeItem<Object> groupNode = new TreeItem<>(new Group(e.getKey()));
            groupNode.setExpanded(true);
            for (ToolDescriptor d : e.getValue()) {
                groupNode.getChildren().add(new TreeItem<>(d));
            }
            root.getChildren().add(groupNode);
        }
        // Always show the Extensions entry at the bottom, even when searching —
        // it's not a tool, it's a management page.
        root.getChildren().add(new TreeItem<>(new ExtensionsEntry()));
        tree.setRoot(root);
    }

    private void selectFirstTool() {
        TreeItem<Object> root = tree.getRoot();
        if (root == null) return;
        for (TreeItem<Object> group : root.getChildren()) {
            if (!group.getChildren().isEmpty()) {
                tree.getSelectionModel().select(group.getChildren().get(0));
                return;
            }
        }
    }

    private void showTool(ToolDescriptor d) {
        Node view;
        try {
            view = d.tool().getView();
        } catch (RuntimeException ex) {
            LOG.error("Failed to build view for tool {}", d.metadata().name(), ex);
            Label err = new Label("Failed to load tool: " + ex.getMessage());
            err.getStyleClass().add("text-danger");
            view = err;
        }
        content.getChildren().setAll(view);
    }

    /** Marker type for group rows in the tree. */
    private record Group(String name) {
        @Override public String toString() { return name; }
    }

    /** Marker type for the "Extensions" nav entry that shows the plugin manager. */
    record ExtensionsEntry() {
        @Override public String toString() { return "Extensions"; }
    }

    /** TreeCell that renders groups in bold and tools with a FontIcon + title. */
    private static final class NavCell extends TreeCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }
            if (item instanceof Group g) {
                setText(g.name());
                setGraphic(null);
                setStyle("-fx-font-weight: bold;");
            } else if (item instanceof ToolDescriptor d) {
                setText("  " + d.shortTitle());
                // safeIcon returns null on unknown code → no icon, no crash
                FontIcon ic = safeIcon(d.metadata().iconCode(), 16);
                setGraphic(ic);
                setStyle("");
            } else if (item instanceof ExtensionsEntry) {
                setText("  Extensions");
                setGraphic(safeIcon("mdal-extension", 16));
                setStyle("");
            }
        }
    }
}
