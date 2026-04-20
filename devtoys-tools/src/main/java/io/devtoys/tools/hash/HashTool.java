package io.devtoys.tools.hash;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.core.tasks.BackgroundTaskRunner.DebouncedTaskRunner;
import io.devtoys.tools.support.ToolLayout;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@ToolMetadata(
        name = "io.devtoys.tools.hash.HashTool",
        groupName = PredefinedToolGroups.GENERATORS,
        shortTitle = "Hash",
        longTitle = "Hash Generator",
        description = "Compute MD5, SHA-1, SHA-256 and SHA-512 digests of text.",
        iconCode = "mdal-functions",
        searchKeywords = {"hash", "md5", "sha1", "sha256", "sha512", "checksum", "digest"},
        order = 20
)
public final class HashTool implements IGuiTool {

    private static final Logger LOG = LoggerFactory.getLogger(HashTool.class);

    private Node view;
    private final DebouncedTaskRunner<Map<String, String>> runner = new DebouncedTaskRunner<>();

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Type or paste text to hash…");
        CheckBox upper = new CheckBox("Uppercase");

        VBox outputs = new VBox(10);
        Map<String, TextField> fields = new LinkedHashMap<>();
        for (String algo : HashCore.DEFAULT_ALGORITHMS) {
            TextField f = new TextField();
            f.setEditable(false);
            f.getStyleClass().add("code-area");
            f.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Menlo', monospace;");

            Button copy = ToolLayout.copyButton(f);

            Label algoLabel = new Label(algo);
            algoLabel.setMinWidth(70);

            HBox row = new HBox(8, algoLabel, f, copy);
            HBox.setHgrow(f, Priority.ALWAYS);

            outputs.getChildren().add(row);
            fields.put(algo, f);
        }

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            boolean upperNow = upper.isSelected();
            runner.run(
                    () -> HashCore.digestAll(src, upperNow),
                    digests -> digests.forEach((algo, hex) -> fields.get(algo).setText(hex)),
                    err -> {
                        fields.values().forEach(TextField::clear);
                        LOG.warn("Hash computation failed", err);
                    }
            );
        };

        input.textProperty().addListener((o, a, b) -> compute.run());
        upper.selectedProperty().addListener((o, a, b) -> compute.run());

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Hash Generator",
                        "Compute cryptographic digests of the input text."),
                new HBox(12, upper, ToolLayout.busySpinner(runner.busyProperty())),
                ToolLayout.section("Input", input),
                ToolLayout.section("Digests", outputs)
        );
        VBox.setVgrow(input, Priority.ALWAYS);
        return page;
    }
}
