/**
 * Public SDK for DevToys Java tools.
 *
 * <p>This module contains only contracts and metadata annotations — no UI
 * framework beyond the JavaFX {@link javafx.scene.Node} type required as the
 * return type of {@link io.devtoys.api.IGuiTool#getView()}.
 */
module io.devtoys.api {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    exports io.devtoys.api;
}
