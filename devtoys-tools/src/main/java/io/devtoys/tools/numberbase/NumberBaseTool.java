package io.devtoys.tools.numberbase;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@ToolMetadata(
        name = "io.devtoys.tools.numberbase.NumberBaseTool",
        groupName = PredefinedToolGroups.CONVERTERS,
        shortTitle = "Number Base",
        longTitle = "Number Base Converter",
        description = "Convert between binary, octal, decimal and hexadecimal.",
        iconCode = "mdal-calculate",
        searchKeywords = {"number", "base", "binary", "hex", "octal", "decimal", "radix"},
        order = 20
)
public final class NumberBaseTool implements IGuiTool {

    private Node view;
    private boolean syncing = false;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        TextField hex = field(), dec = field(), oct = field(), bin = field();

        hex.textProperty().addListener((o, a, b) -> sync(b, 16, hex, dec, oct, bin));
        dec.textProperty().addListener((o, a, b) -> sync(b, 10, hex, dec, oct, bin));
        oct.textProperty().addListener((o, a, b) -> sync(b, 8,  hex, dec, oct, bin));
        bin.textProperty().addListener((o, a, b) -> sync(b, 2,  hex, dec, oct, bin));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        row(grid, 0, "Hex",     hex);
        row(grid, 1, "Decimal", dec);
        row(grid, 2, "Octal",   oct);
        row(grid, 3, "Binary",  bin);
        GridPane.setHgrow(hex, Priority.ALWAYS);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Number Base Converter",
                        "Type in any row — the others update. Prefixes 0x / 0b are accepted."),
                grid);
        return page;
    }

    private TextField field() {
        TextField t = new TextField();
        t.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
        return t;
    }

    private void sync(String text, int fromRadix,
                      TextField hex, TextField dec, TextField oct, TextField bin) {
        if (syncing || text == null || text.isBlank()) return;
        try {
            long v = NumberBaseCore.parse(text, fromRadix);
            syncing = true;
            try {
                if (fromRadix != 16) hex.setText(NumberBaseCore.format(v, 16));
                if (fromRadix != 10) dec.setText(NumberBaseCore.format(v, 10));
                if (fromRadix != 8)  oct.setText(NumberBaseCore.format(v, 8));
                if (fromRadix != 2)  bin.setText(NumberBaseCore.format(v, 2));
            } finally { syncing = false; }
        } catch (Exception ignored) { /* don't clobber while typing */ }
    }

    private static void row(GridPane g, int r, String label, TextField f) {
        Label l = new Label(label);
        l.setMinWidth(80);
        g.add(l, 0, r); g.add(f, 1, r);
    }
}
