package io.devtoys.tools.htmlentity;

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
        name = "io.devtoys.tools.htmlentity.HtmlEntityTool",
        groupName = PredefinedToolGroups.ENCODERS_DECODERS,
        shortTitle = "HTML",
        longTitle = "HTML Entity Encoder / Decoder",
        description = "Escape and unescape HTML/XML entities.",
        iconCode = "mdal-code",
        searchKeywords = {"html", "entity", "escape", "xml", "&amp;"},
        order = 30
)
public final class HtmlEntityTool implements IGuiTool {

    private Node view;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Text or HTML-encoded string…");
        TextArea output = ToolLayout.codeArea("Output");
        output.setEditable(false);
        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Encode", "Decode");
        mode.getSelectionModel().selectFirst();
        Label status = ToolLayout.statusLabel();

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            String out = "Encode".equals(mode.getValue())
                    ? HtmlEntityCore.encode(src) : HtmlEntityCore.decode(src);
            output.setText(out);
            status.setText(src.length() + " → " + out.length() + " chars.");
        };
        input.textProperty().addListener((o, a, b) -> compute.run());
        mode.valueProperty().addListener((o, a, b) -> compute.run());

        HBox top = new HBox(8, new Label("Mode:"), mode, ToolLayout.copyButton(output));
        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("HTML Entity Encoder / Decoder",
                        "Escape the XML/HTML special characters, and decode entities back."),
                top,
                ToolLayout.section("Input", input),
                ToolLayout.section("Output", output),
                status);
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }
}
