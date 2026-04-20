package io.devtoys.tools.regex;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.core.tasks.BackgroundTaskRunner.DebouncedTaskRunner;
import io.devtoys.tools.support.ToolLayout;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.PatternSyntaxException;

@ToolMetadata(
        name = "io.devtoys.tools.regex.RegexTool",
        groupName = PredefinedToolGroups.TESTERS,
        shortTitle = "Regex",
        longTitle = "Regex Tester",
        description = "Test regular expressions against sample text and inspect matches.",
        iconCode = "mdmz-search",
        searchKeywords = {"regex", "regexp", "regular expression", "pattern", "match"},
        order = 10
)
public final class RegexTool implements IGuiTool {

    private static final Logger LOG = LoggerFactory.getLogger(RegexTool.class);

    private Node view;
    private final DebouncedTaskRunner<Result> runner = new DebouncedTaskRunner<>();

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextField pattern = new TextField();
        pattern.setPromptText("Pattern, e.g.  \\b\\w+@\\w+\\.\\w+\\b");
        pattern.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Menlo', monospace;");

        CheckBox caseInsensitive = new CheckBox("Case insensitive");
        CheckBox multiline = new CheckBox("Multiline");
        CheckBox dotall = new CheckBox("Dot matches newline");

        TextArea sample = ToolLayout.codeArea("Text to match against…");
        TextArea results = ToolLayout.codeArea("Matches appear here");
        results.setEditable(false);

        Label status = ToolLayout.statusLabel();

        Runnable compute = () -> {
            String p = pattern.getText() == null ? "" : pattern.getText();
            String s = sample.getText() == null ? "" : sample.getText();
            RegexCore.Flags flags = new RegexCore.Flags(
                    caseInsensitive.isSelected(),
                    multiline.isSelected(),
                    dotall.isSelected());
            runner.run(
                    () -> buildResult(p, s, flags),
                    r -> {
                        results.setText(r.text);
                        status.setText(r.status);
                    },
                    err -> {
                        results.clear();
                        if (err instanceof PatternSyntaxException pse) {
                            status.setText("Pattern error: " + pse.getDescription()
                                    + (pse.getIndex() >= 0 ? " (at index " + pse.getIndex() + ")" : ""));
                        } else {
                            status.setText("Error: " + err.getMessage());
                            LOG.debug("Regex evaluation failed", err);
                        }
                    }
            );
        };

        pattern.textProperty().addListener((o, a, b) -> compute.run());
        sample.textProperty().addListener((o, a, b) -> compute.run());
        caseInsensitive.selectedProperty().addListener((o, a, b) -> compute.run());
        multiline.selectedProperty().addListener((o, a, b) -> compute.run());
        dotall.selectedProperty().addListener((o, a, b) -> compute.run());

        HBox flagsRow = new HBox(12, caseInsensitive, multiline, dotall,
                ToolLayout.busySpinner(runner.busyProperty()));

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Regex Tester",
                        "Write a pattern on the left, sample text below, and see matches in real time."),
                ToolLayout.section("Pattern", pattern),
                flagsRow,
                ToolLayout.section("Sample", sample),
                ToolLayout.section("Matches", results),
                status
        );
        VBox.setVgrow(sample, Priority.ALWAYS);
        VBox.setVgrow(results, Priority.ALWAYS);
        return page;
    }

    private static Result buildResult(String pattern, String sample, RegexCore.Flags flags) {
        if (pattern.isEmpty()) return new Result("", "");
        List<RegexCore.Match> matches = RegexCore.findAll(pattern, sample, flags);  // may throw
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (RegexCore.Match m : matches) {
            sb.append("Match ").append(i++)
              .append(" [").append(m.start()).append("–").append(m.end()).append("]: ")
              .append(m.value()).append('\n');
            int g = 1;
            for (String grp : m.groups()) {
                sb.append("    group ").append(g++).append(": ")
                  .append(grp == null ? "(null)" : grp).append('\n');
            }
        }
        String status = matches.size() + " match" + (matches.size() == 1 ? "" : "es") + ".";
        return new Result(sb.toString(), status);
    }

    private record Result(String text, String status) {}
}
