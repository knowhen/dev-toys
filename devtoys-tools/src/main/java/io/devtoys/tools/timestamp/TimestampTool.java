package io.devtoys.tools.timestamp;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;

@ToolMetadata(
        name = "io.devtoys.tools.timestamp.TimestampTool",
        groupName = PredefinedToolGroups.CONVERTERS,
        shortTitle = "Timestamp",
        longTitle = "Unix Timestamp Converter",
        description = "Convert between Unix epoch time and human-readable dates.",
        iconCode = "mdal-access_time",
        searchKeywords = {"timestamp", "unix", "epoch", "date", "time", "iso"},
        order = 10
)
public final class TimestampTool implements IGuiTool {

    private Node view;
    private boolean syncing = false;

    @Override public Node getView() { if (view == null) view = build(); return view; }

    private Node build() {
        TextField seconds = new TextField();
        TextField millis = new TextField();
        TextField iso = new TextField();
        TextField local = new TextField();
        for (TextField tf : new TextField[]{seconds, millis, iso, local}) {
            tf.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
        }
        local.setEditable(false);

        Button now = new Button("Now");
        now.setOnAction(e -> fill(Instant.now(), seconds, millis, iso, local));

        // Any field change triggers re-parse and cross-fill
        seconds.textProperty().addListener((o, a, b) -> {
            if (syncing || b == null || b.isBlank()) return;
            try { fill(TimestampCore.fromSeconds(Long.parseLong(b.trim())),
                      seconds, millis, iso, local); } catch (Exception ignored) {}
        });
        millis.textProperty().addListener((o, a, b) -> {
            if (syncing || b == null || b.isBlank()) return;
            try { fill(TimestampCore.fromMillis(Long.parseLong(b.trim())),
                      seconds, millis, iso, local); } catch (Exception ignored) {}
        });
        iso.textProperty().addListener((o, a, b) -> {
            if (syncing || b == null || b.isBlank()) return;
            try { fill(TimestampCore.parse(b), seconds, millis, iso, local); }
            catch (Exception ignored) {}
        });

        fill(Instant.now(), seconds, millis, iso, local);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(8));
        row(grid, 0, "Seconds", seconds);
        row(grid, 1, "Milliseconds", millis);
        row(grid, 2, "ISO-8601 (UTC)", iso);
        row(grid, 3, "Local", local);
        GridPane.setHgrow(seconds, Priority.ALWAYS);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Unix Timestamp Converter",
                        "Enter a value in any row — the other rows update automatically."),
                new VBox(10, now), grid);
        return page;
    }

    private static void row(GridPane g, int r, String label, javafx.scene.Node ctrl) {
        Label l = new Label(label);
        l.setMinWidth(140);
        g.add(l, 0, r); g.add(ctrl, 1, r);
    }

    private void fill(Instant instant, TextField s, TextField ms, TextField iso, TextField local) {
        syncing = true;
        try {
            s.setText(Long.toString(instant.getEpochSecond()));
            ms.setText(Long.toString(instant.toEpochMilli()));
            iso.setText(TimestampCore.toIsoUtc(instant));
            local.setText(TimestampCore.toLocal(instant, ZoneId.systemDefault()));
        } finally { syncing = false; }
    }
}
