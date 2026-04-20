package io.devtoys.tools.loremipsum;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
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

@ToolMetadata(
        name = "io.devtoys.tools.loremipsum.LoremIpsumTool",
        groupName = PredefinedToolGroups.GENERATORS,
        shortTitle = "Lorem Ipsum",
        longTitle = "Lorem Ipsum Generator",
        description = "Generate placeholder text.",
        iconCode = "mdal-article",
        searchKeywords = {"lorem", "ipsum", "placeholder", "filler", "text"},
        order = 30
)
public final class LoremIpsumTool implements IGuiTool {

    private Node view;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        ChoiceBox<String> unit = new ChoiceBox<>();
        unit.getItems().addAll("Words", "Sentences", "Paragraphs");
        unit.getSelectionModel().select(2);

        Spinner<Integer> count = new Spinner<>(1, 100, 3);
        count.setPrefWidth(90);
        count.setEditable(true);

        TextArea output = ToolLayout.codeArea("Output");
        output.setEditable(false);
        output.setWrapText(true);

        Button generate = new Button("Generate");
        Runnable run = () -> {
            long seed = System.nanoTime();
            int n = count.getValue() == null ? 1 : count.getValue();
            String text = switch (unit.getValue()) {
                case "Words" -> LoremIpsumCore.words(n, seed);
                case "Sentences" -> LoremIpsumCore.sentences(n, seed);
                default -> LoremIpsumCore.paragraphs(n, seed);
            };
            output.setText(text);
        };
        generate.setOnAction(e -> run.run());

        HBox top = new HBox(10,
                new Label("Count:"), count,
                new Label("Unit:"), unit,
                generate,
                ToolLayout.copyButton(output));
        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Lorem Ipsum Generator",
                        "Produce placeholder text by word, sentence, or paragraph count."),
                top,
                ToolLayout.section("Output", output));
        VBox.setVgrow(output, Priority.ALWAYS);

        run.run();  // initial sample
        return page;
    }
}
