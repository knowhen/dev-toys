package io.devtoys.tools.support;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Builders for the common "title + description + content" scaffold used by
 * every tool page. Keeps the tool implementations focused on their logic.
 */
public final class ToolLayout {

    private ToolLayout() {}

    /** Main vertical page: 24px padding, 16px gaps. */
    public static VBox page() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        return box;
    }

    /** Creates a title-and-description header. */
    public static VBox header(String title, String description) {
        Label t = new Label(title);
        t.getStyleClass().add("title-3");

        Label d = new Label(description == null ? "" : description);
        d.getStyleClass().add("text-muted");
        d.setWrapText(true);

        return new VBox(4, t, d);
    }

    /** Section with a small heading above an arbitrary content node. */
    public static VBox section(String heading, Region content) {
        Label h = new Label(heading);
        h.getStyleClass().add("text-caption");
        VBox v = new VBox(6, h, content);
        VBox.setVgrow(content, Priority.SOMETIMES);
        return v;
    }

    /** Horizontal button row aligned left. */
    public static HBox buttonRow(javafx.scene.Node... nodes) {
        HBox row = new HBox(8, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Standard monospace TextArea used for all input / output fields. */
    public static TextArea codeArea(String promptText) {
        TextArea ta = new TextArea();
        ta.setPromptText(promptText);
        ta.setWrapText(false);
        ta.getStyleClass().add("code-area");
        ta.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Menlo', monospace;");
        return ta;
    }

    /** A "Copy" button that puts the current text of some input control on the clipboard. */
    public static Button copyButton(TextInputControl source) {
        Button b = new Button("Copy");
        b.setOnAction(e -> {
            ClipboardContent c = new ClipboardContent();
            c.putString(source.getText() == null ? "" : source.getText());
            Clipboard.getSystemClipboard().setContent(c);
        });
        return b;
    }

    /**
     * A small ProgressIndicator that shows only when {@code busy} is true.
     * Use with {@code BackgroundTaskRunner.DebouncedTaskRunner#busyProperty()}.
     */
    public static ProgressIndicator busySpinner(ObservableValue<Boolean> busy) {
        ProgressIndicator p = new ProgressIndicator();
        p.setPrefSize(16, 16);
        p.setMaxSize(16, 16);
        p.visibleProperty().bind(busy);
        p.managedProperty().bind(busy);
        return p;
    }

    /** Label whose visibility is bound to text being non-empty. */
    public static Label statusLabel() {
        Label l = new Label();
        l.getStyleClass().add("text-muted");
        l.visibleProperty().bind(Bindings.isNotEmpty(l.textProperty()));
        l.managedProperty().bind(Bindings.isNotEmpty(l.textProperty()));
        return l;
    }
}
