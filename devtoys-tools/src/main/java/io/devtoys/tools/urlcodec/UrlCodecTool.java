package io.devtoys.tools.urlcodec;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@ToolMetadata(
        name = "io.devtoys.tools.urlcodec.UrlCodecTool",
        groupName = PredefinedToolGroups.ENCODERS_DECODERS,
        shortTitle = "URL",
        longTitle = "URL Encoder / Decoder",
        description = "Percent-encode and decode URL-safe strings (UTF-8).",
        iconCode = "mdal-link",
        searchKeywords = {"url", "percent", "encode", "decode"},
        order = 20
)
public final class UrlCodecTool implements IGuiTool {

    private Node view;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Text or URL-encoded string…");
        TextArea output = ToolLayout.codeArea("Output");
        output.setEditable(false);
        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Encode", "Decode");
        mode.getSelectionModel().selectFirst();
        Label status = ToolLayout.statusLabel();

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            try {
                String out = "Encode".equals(mode.getValue())
                        ? UrlCodecCore.encode(src) : UrlCodecCore.decode(src);
                output.setText(out);
                status.setText(src.length() + " → " + out.length() + " chars.");
            } catch (Exception e) {
                output.setText("");
                status.setText("Error: " + e.getMessage());
            }
        };
        input.textProperty().addListener((o, a, b) -> compute.run());
        mode.valueProperty().addListener((o, a, b) -> compute.run());

        HBox top = new HBox(8, new Label("Mode:"), mode, ToolLayout.copyButton(output));
        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("URL Encoder / Decoder", "Percent-encode and decode strings using UTF-8."),
                top,
                ToolLayout.section("Input", input),
                ToolLayout.section("Output", output),
                status);
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }
}
