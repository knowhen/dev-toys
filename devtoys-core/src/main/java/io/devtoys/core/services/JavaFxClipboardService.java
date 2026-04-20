package io.devtoys.core.services;

import io.devtoys.api.ClipboardService;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * {@link ClipboardService} backed by the JavaFX system clipboard.
 *
 * <p>All methods must be called on the JavaFX Application Thread; callers that
 * might be on a background thread should wrap calls in {@code Platform.runLater}.
 */
public final class JavaFxClipboardService implements ClipboardService {

    @Override
    public String getText() {
        String text = Clipboard.getSystemClipboard().getString();
        return text == null ? "" : text;
    }

    @Override
    public void setText(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text == null ? "" : text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
