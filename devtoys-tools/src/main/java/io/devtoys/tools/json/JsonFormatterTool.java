package io.devtoys.tools.json;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.core.tasks.BackgroundTaskRunner.DebouncedTaskRunner;
import io.devtoys.tools.support.ToolLayout;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToolMetadata(
        name = "io.devtoys.tools.json.JsonFormatterTool",
        groupName = PredefinedToolGroups.FORMATTERS,
        shortTitle = "JSON",
        longTitle = "JSON Formatter",
        description = "Format, validate and minify JSON.",
        iconCode = "mdal-code",
        searchKeywords = {"json", "format", "pretty", "minify", "validate"},
        order = 10
)
public final class JsonFormatterTool implements IGuiTool {

    private static final Logger LOG = LoggerFactory.getLogger(JsonFormatterTool.class);

    private Node view;
    private final DebouncedTaskRunner<Result> runner = new DebouncedTaskRunner<>();

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Paste JSON here…");
        TextArea output = ToolLayout.codeArea("Formatted JSON appears here");
        output.setEditable(false);

        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Format", "Minify");
        mode.getSelectionModel().selectFirst();

        Spinner<Integer> indent = new Spinner<>(1, 8, 2);
        indent.setPrefWidth(72);

        Label status = ToolLayout.statusLabel();

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            boolean format = "Format".equals(mode.getValue());
            int indentN = indent.getValue() == null ? 2 : indent.getValue();
            runner.run(
                    () -> doOperation(src, format, indentN),
                    r -> {
                        output.setText(r.text);
                        status.setText(r.status);
                    },
                    err -> {
                        output.setText("");
                        status.setText("Invalid JSON: " + err.getMessage());
                        LOG.debug("JSON op failed", err);
                    }
            );
        };

        input.textProperty().addListener((o, a, b) -> compute.run());
        mode.valueProperty().addListener((o, a, b) -> compute.run());
        indent.valueProperty().addListener((o, a, b) -> compute.run());

        Button copyBtn = ToolLayout.copyButton(output);

        HBox topBar = new HBox(10,
                new Label("Mode:"), mode,
                new Label("Indent:"), indent,
                copyBtn,
                ToolLayout.busySpinner(runner.busyProperty()));

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("JSON Formatter", "Validate, pretty-print and minify JSON."),
                topBar,
                ToolLayout.section("Input", input),
                ToolLayout.section("Output", output),
                status
        );
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }

    private static Result doOperation(String src, boolean format, int indent) throws Exception {
        if (src.isBlank()) return new Result("", "");
        String out = format
                ? JsonFormatterCore.format(src, indent)
                : JsonFormatterCore.minify(src);
        String msg = format ? "Valid JSON, formatted." : "Valid JSON, minified.";
        return new Result(out, msg);
    }

    private record Result(String text, String status) {}
}
