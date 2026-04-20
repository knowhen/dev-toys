package io.devtoys.api;

import javafx.scene.Node;

/**
 * The core contract every DevToys tool implements.
 *
 * <p>Implementations are discovered at runtime through {@link java.util.ServiceLoader},
 * and must be annotated with {@link ToolMetadata} to carry display information.
 *
 * <p>This mirrors the design of the original .NET DevToys {@code IGuiTool} interface,
 * but returns a JavaFX {@link Node} directly rather than a custom {@code UIToolView}
 * tree. A later iteration can introduce a renderer-agnostic UI DSL on top.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>The registry instantiates the tool via its no-arg constructor (required by
 *       {@link java.util.ServiceLoader}).</li>
 *   <li>{@link #initialize(ServiceContext)} is called exactly once, before the first
 *       call to {@link #getView()}. The default is a no-op; tools that need host
 *       services override it.</li>
 *   <li>{@link #getView()} is called when the user navigates to the tool. Tools
 *       may cache and return the same node across calls.</li>
 * </ol>
 */
public interface IGuiTool {

    /**
     * Called once, right after construction, giving the tool access to host services.
     * Default is a no-op.
     */
    default void initialize(ServiceContext context) {
        // no-op by default
    }

    /**
     * Returns the root JavaFX {@link Node} for this tool's view.
     *
     * <p>Implementations may cache and return the same instance across calls, or
     * build it lazily the first time. The host mounts it into the content area
     * when the user selects this tool.
     */
    Node getView();

    /**
     * Invoked when smart-detection routes clipboard or file data to this tool.
     * The default implementation is a no-op.
     */
    default void onDataReceived(String dataTypeName, Object parsedData) {
        // no-op by default
    }
}
