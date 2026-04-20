package io.devtoys.tools.base64;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.core.tasks.BackgroundTaskRunner.DebouncedTaskRunner;
import io.devtoys.tools.support.ToolLayout;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI shell for the Base64 tool. All business logic is in {@link Base64Core}.
 */
@ToolMetadata(
        name = "io.devtoys.tools.base64.Base64Tool",
        groupName = PredefinedToolGroups.ENCODERS_DECODERS,
        shortTitle = "Base64",
        longTitle = "Base64 Encoder / Decoder",
        description = "Encode and decode Base64 strings.",
        iconCode = "mdal-lock",
        searchKeywords = {"base64", "encode", "decode", "b64"},
        order = 10
)
public final class Base64Tool implements IGuiTool {

    private static final Logger LOG = LoggerFactory.getLogger(Base64Tool.class);

    private Node view;
    private final DebouncedTaskRunner<Result> runner = new DebouncedTaskRunner<>();

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Paste text or Base64 here…");
        TextArea output = ToolLayout.codeArea("Result appears here");
        output.setEditable(false);

        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Encode", "Decode");
        mode.getSelectionModel().selectFirst();

        Label status = ToolLayout.statusLabel();

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            boolean encode = "Encode".equals(mode.getValue());
            runner.run(
                    () -> doOperation(src, encode),
                    r -> {
                        output.setText(r.text);
                        status.setText(r.status);
                    },
                    err -> {
                        output.setText("");
                        status.setText("Error: " + err.getMessage());
                        LOG.debug("Base64 tool computation failed", err);
                    }
            );
        };

        ChangeListener<Object> live = (obs, o, n) -> compute.run();
        input.textProperty().addListener(live);
        mode.valueProperty().addListener(live);

        Button copyBtn = ToolLayout.copyButton(output);
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> { input.clear(); output.clear(); status.setText(""); });

        HBox topBar = new HBox(8,
                new Label("Mode:"), mode, copyBtn, clearBtn,
                ToolLayout.busySpinner(runner.busyProperty()));

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Base64 Encoder / Decoder",
                        "Encode text to Base64 or decode Base64 back to text."),
                topBar,
                ToolLayout.section("Input", input),
                ToolLayout.section("Output", output),
                status
        );
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }

    private static Result doOperation(String src, boolean encode) {
        if (encode) {
            String encoded = Base64Core.encode(src);
            return new Result(encoded,
                    "Encoded " + src.length() + " chars → " + encoded.length() + " chars.");
        } else {
            String decoded = Base64Core.decode(src);
            int bytes = decoded.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            return new Result(decoded, "Decoded " + bytes + " bytes.");
        }
    }

    private record Result(String text, String status) {}
}
