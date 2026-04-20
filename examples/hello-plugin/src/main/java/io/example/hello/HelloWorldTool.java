package io.example.hello;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ToolMetadata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Minimal "Hello World" plugin — proves the whole loading chain works.
 *
 * <p>This class is:
 * <ol>
 *   <li>Annotated with {@link ToolMetadata} so the host picks it up as a tool.</li>
 *   <li>Listed in {@code META-INF/services/io.devtoys.api.IGuiTool} so
 *       {@link java.util.ServiceLoader} finds it.</li>
 *   <li>Shipped in a JAR whose root contains {@code plugin.json}.</li>
 * </ol>
 */
@ToolMetadata(
        name = "io.example.hello.HelloWorldTool",
        groupName = "Extensions",
        shortTitle = "Hello",
        longTitle = "Hello from a plugin!",
        description = "A tiny example that proves plugin loading works.",
        iconCode = "mdal-emoji_emotions",
        searchKeywords = {"hello", "world", "example", "plugin"},
        order = 10
)
public final class HelloWorldTool implements IGuiTool {

    private Node view;

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        Label big = new Label("👋 Hello from a plugin!");
        big.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label detail = new Label(
                "This tool is loaded from an external JAR in your plugin folder.\n"
              + "If you see this, the plugin system is working.");
        detail.setWrapText(true);

        VBox page = new VBox(16, big, detail);
        page.setPadding(new Insets(40));
        page.setAlignment(Pos.TOP_LEFT);
        return page;
    }
}
