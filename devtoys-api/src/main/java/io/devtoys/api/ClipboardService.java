package io.devtoys.api;

/**
 * Thin abstraction over the system clipboard.
 *
 * <p>Tools could call JavaFX's {@code Clipboard.getSystemClipboard()} directly,
 * but routing through a service makes it easy to (a) mock in tests,
 * (b) later add smart-detection hooks that observe clipboard changes, and
 * (c) keep the API module independent of any specific JavaFX clipboard type
 * should we ever run headless (CLI mode).
 */
public interface ClipboardService {

    /** Returns the current text on the system clipboard, or empty string if none. */
    String getText();

    /** Replaces the system clipboard with the given text. */
    void setText(String text);
}
