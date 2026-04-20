package io.devtoys.tools.password;

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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@ToolMetadata(
        name = "io.devtoys.tools.password.PasswordTool",
        groupName = PredefinedToolGroups.GENERATORS,
        shortTitle = "Password",
        longTitle = "Password Generator",
        description = "Generate secure random passwords.",
        iconCode = "mdmz-vpn_key",
        searchKeywords = {"password", "pass", "secret", "random", "secure"},
        order = 40
)
public final class PasswordTool implements IGuiTool {

    private Node view;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        Spinner<Integer> length = new Spinner<>(4, 128, 16);
        length.setPrefWidth(90);
        length.setEditable(true);

        Spinner<Integer> count = new Spinner<>(1, 50, 5);
        count.setPrefWidth(80);
        count.setEditable(true);

        CheckBox lower = new CheckBox("a-z"); lower.setSelected(true);
        CheckBox upper = new CheckBox("A-Z"); upper.setSelected(true);
        CheckBox digits = new CheckBox("0-9"); digits.setSelected(true);
        CheckBox symbols = new CheckBox("!@#$"); symbols.setSelected(false);
        CheckBox excludeAmb = new CheckBox("Exclude ambiguous (l, 1, O, 0, I)");
        excludeAmb.setSelected(true);

        TextArea output = ToolLayout.codeArea("Generated passwords");
        output.setEditable(false);

        Button generate = new Button("Generate");
        Runnable run = () -> {
            try {
                PasswordCore.Options opts = new PasswordCore.Options(
                        lower.isSelected(), upper.isSelected(),
                        digits.isSelected(), symbols.isSelected(),
                        excludeAmb.isSelected());
                String[] pwds = PasswordCore.generateMany(
                        count.getValue(), length.getValue(), opts);
                output.setText(String.join("\n", pwds));
            } catch (IllegalArgumentException e) {
                output.setText("Error: " + e.getMessage());
            }
        };
        generate.setOnAction(e -> run.run());

        FlowPane charsets = new FlowPane(10, 6, lower, upper, digits, symbols);
        HBox sizes = new HBox(10,
                new Label("Length:"), length,
                new Label("Count:"), count,
                generate, ToolLayout.copyButton(output));

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Password Generator",
                        "Uses java.security.SecureRandom — suitable for real credentials."),
                sizes, charsets, excludeAmb,
                ToolLayout.section("Output", output));
        VBox.setVgrow(output, Priority.ALWAYS);
        run.run();
        return page;
    }
}
