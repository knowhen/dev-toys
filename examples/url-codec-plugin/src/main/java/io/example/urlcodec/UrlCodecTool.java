package io.example.urlcodec;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ToolMetadata;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * URL encoder/decoder tool. Real-world example of a plugin that splits pure
 * logic ({@link UrlCodecCore}) from the JavaFX GUI shell.
 */
@ToolMetadata(
        name = "io.example.urlcodec.UrlCodecTool",
        groupName = "Extensions",
        shortTitle = "URL Codec",
        longTitle = "URL Encoder / Decoder",
        description = "Percent-encode and decode URL-safe strings (UTF-8).",
        iconCode = "mdal-link",
        searchKeywords = {"url", "encode", "decode", "percent"},
        order = 20
)
public final class UrlCodecTool implements IGuiTool {

    private Node view;

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = new TextArea();
        input.setPromptText("Paste text or URL-encoded string here…");
        input.setWrapText(true);
        input.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");

        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Encode", "Decode");
        mode.getSelectionModel().selectFirst();

        Label status = new Label();
        status.getStyleClass().add("text-muted");

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            try {
                String out = "Encode".equals(mode.getValue())
                        ? UrlCodecCore.encode(src)
                        : UrlCodecCore.decode(src);
                output.setText(out);
                status.setText(src.length() + " chars in → " + out.length() + " chars out.");
            } catch (Exception e) {
                output.setText("");
                status.setText("Error: " + e.getMessage());
            }
        };

        input.textProperty().addListener((o, a, b) -> compute.run());
        mode.valueProperty().addListener((o, a, b) -> compute.run());

        HBox topBar = new HBox(8, new Label("Mode:"), mode);
        VBox page = new VBox(12,
                new Label("URL Encoder / Decoder"),
                topBar,
                new Label("Input"), input,
                new Label("Output"), output,
                status
        );
        page.setPadding(new Insets(20));
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }
}
