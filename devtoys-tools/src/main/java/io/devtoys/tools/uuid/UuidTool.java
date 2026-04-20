package io.devtoys.tools.uuid;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

@ToolMetadata(
        name = "io.devtoys.tools.uuid.UuidTool",
        groupName = PredefinedToolGroups.GENERATORS,
        shortTitle = "UUID",
        longTitle = "UUID Generator",
        description = "Generate random v4 UUIDs with configurable formatting.",
        iconCode = "mdal-fingerprint",
        searchKeywords = {"uuid", "guid", "id", "random"},
        order = 10
)
public final class UuidTool implements IGuiTool {

    private Node view;

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        Spinner<Integer> count = new Spinner<>(1, 1000, 5);
        count.setEditable(true);
        count.setPrefWidth(90);

        CheckBox hyphens = new CheckBox("Hyphens");
        hyphens.setSelected(true);
        CheckBox uppercase = new CheckBox("Uppercase");
        CheckBox braces = new CheckBox("Braces");

        TextArea output = ToolLayout.codeArea("Generated UUIDs appear here");
        output.setEditable(false);

        Button generate = new Button("Generate");
        Button copy = ToolLayout.copyButton(output);

        Runnable run = () -> {
            int n = count.getValue() == null ? 1 : count.getValue();
            UuidCore.Format fmt = new UuidCore.Format(
                    uppercase.isSelected(), hyphens.isSelected(), braces.isSelected());
            List<String> list = UuidCore.generate(n, fmt);
            output.setText(String.join("\n", list));
        };

        generate.setOnAction(e -> run.run());

        HBox topBar = new HBox(10,
                new Label("Count:"), count, hyphens, uppercase, braces, generate, copy);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("UUID Generator", "Generate random v4 UUIDs."),
                topBar,
                ToolLayout.section("Output", output)
        );
        VBox.setVgrow(output, Priority.ALWAYS);

        // Generate once at startup so the pane isn't empty
        run.run();
        return page;
    }
}
