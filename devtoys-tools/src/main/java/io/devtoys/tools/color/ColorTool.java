package io.devtoys.tools.color;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

@ToolMetadata(
        name = "io.devtoys.tools.color.ColorTool",
        groupName = PredefinedToolGroups.CONVERTERS,
        shortTitle = "Color",
        longTitle = "Color Code Converter",
        description = "Convert between HEX, RGB, and HSL color formats.",
        iconCode = "mdal-color_lens",
        searchKeywords = {"color", "hex", "rgb", "hsl", "palette"},
        order = 30
)
public final class ColorTool implements IGuiTool {

    private Node view;
    private boolean syncing = false;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        TextField hex = field();
        TextField rgb = field();
        TextField hsl = field();
        hsl.setEditable(false);  // HSL is output-only for now; round-tripping HSL is lossy

        Rectangle swatch = new Rectangle(120, 120);
        swatch.setArcWidth(8); swatch.setArcHeight(8);
        swatch.setStyle("-fx-stroke: -color-border-default; -fx-stroke-width: 1;");

        hex.textProperty().addListener((o, a, b) -> {
            if (syncing) return;
            tryApply(() -> ColorCore.parseHex(b), hex, rgb, hsl, swatch);
        });
        rgb.textProperty().addListener((o, a, b) -> {
            if (syncing) return;
            tryApply(() -> ColorCore.parseRgb(b), hex, rgb, hsl, swatch);
        });

        // Default color
        hex.setText("#4a9eff");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        row(grid, 0, "Hex", hex);
        row(grid, 1, "RGB", rgb);
        row(grid, 2, "HSL", hsl);
        GridPane.setHgrow(hex, Priority.ALWAYS);

        HBox main = new HBox(20, grid, swatch);
        HBox.setHgrow(grid, Priority.ALWAYS);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Color Code Converter",
                        "Enter a HEX or RGB color — the other format and a preview update."),
                main);
        return page;
    }

    private void tryApply(java.util.function.Supplier<ColorCore.Rgb> parser,
                          TextField hex, TextField rgb, TextField hsl, Rectangle swatch) {
        try {
            ColorCore.Rgb c = parser.get();
            syncing = true;
            try {
                String h = ColorCore.toHex(c);
                if (!h.equalsIgnoreCase(hex.getText())) hex.setText(h);
                String r = ColorCore.toRgbString(c);
                if (!r.equals(rgb.getText())) rgb.setText(r);
                hsl.setText(ColorCore.toHslString(c));
                swatch.setFill(Color.rgb(c.r(), c.g(), c.b()));
            } finally { syncing = false; }
        } catch (Exception ignored) { /* invalid — don't clobber while typing */ }
    }

    private TextField field() {
        TextField t = new TextField();
        t.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
        return t;
    }

    private static void row(GridPane g, int r, String label, Region f) {
        Label l = new Label(label);
        l.setMinWidth(60);
        g.add(l, 0, r); g.add(f, 1, r);
    }
}
