import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DashboardController {

    private static final long[] INITIAL = {10, 15, 8, 22, 5, 12, 19, 7};
    private final LazySegmentTree tree = new LazySegmentTree(INITIAL);

    private TextArea rows, advanced;
    private Label result, opsLabel;

    private ComboBox<Integer> ql, qr, pl, rl, rr;
    private TextField pv, rv;

    private CheckBox showTree, showLazy;

    public Scene buildScene() {

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        root.getChildren().addAll(
                title(),
                new Separator(),
                rowSection(),
                new Separator(),
                opsSection(),
                new Separator(),
                querySection(),
                new Separator(),
                pointSection(),
                new Separator(),
                rangeSection(),
                new Separator(),
                resultSection(),
                new Separator(),
                advancedSection(),
                new Separator(),
                printTreeSection(),     // NEW
                new Separator(),
                benchmarkSection()
        );

        refresh();
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        return new Scene(scroll, 750, 680);
    }

    private Label title() {
        Label l = new Label("Cinema Ticket Tracker");
        l.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        return l;
    }

    private VBox rowSection() {
        rows = new TextArea();
        rows.setEditable(false);
        rows.setPrefHeight(150);
        rows.setStyle("-fx-font-family: monospace;");

        refreshRows();
        return box("Current Ticket Availability", rows);
    }

    private VBox opsSection() {
        opsLabel = new Label("Operations: 0");

        Button reset = new Button("Reset");
        reset.setOnAction(e -> {
            OperationCounter.reset();
            refresh();
        });

        HBox h = new HBox(10, opsLabel, reset);
        h.setAlignment(Pos.CENTER_LEFT);

        return new VBox(h);
    }

    private VBox querySection() {
        ql = boxIndex();
        qr = boxIndex();

        Button b = new Button("Query");
        b.setOnAction(e -> {
            long res = tree.rangeQuery(ql.getValue(), qr.getValue());
            result.setText("Number of tickets = " + res);
            refresh();
        });

        return row("Range Query/Number of tickets", "From", ql, "To", qr, b);
    }

    private VBox pointSection() {
        pl = boxIndex();
        pv = new TextField("10");

        Button b = new Button("Update");
        b.setOnAction(e -> {
            tree.pointUpdate(pl.getValue(), Long.parseLong(pv.getText()));
            refresh();
        });

        return row("Point Update/ Update nr of tickets in row", "Row", pl, "Value", pv, b);
    }

    private VBox rangeSection() {
        rl = boxIndex();
        rr = boxIndex();
        rv = new TextField("5");

        Button b = new Button("Apply");
        b.setOnAction(e -> {
            tree.rangeUpdate(rl.getValue(), rr.getValue(),
                    Long.parseLong(rv.getText()));
            refresh();
        });

        return row("Range Update/Add tickets for each row", "From", rl, "To", rr, "Add", rv, b);
    }

    private VBox resultSection() {
        result = new Label("Ready");
        return new VBox(result);
    }

    private VBox advancedSection() {

        showTree = new CheckBox("Segment Tree");
        showLazy = new CheckBox("Lazy Array");

        advanced = new TextArea();
        advanced.setEditable(false);
        advanced.setPrefHeight(120);
        advanced.setVisible(false);
        advanced.setManaged(false);

        showTree.setOnAction(e -> syncVisibility());
        showLazy.setOnAction(e -> syncVisibility());

        return new VBox(
                new HBox(10, showTree, showLazy),
                advanced
        );

    }

    private VBox printTreeSection() {
        Button b = new Button("Print Tree");
        b.setOnAction(e -> {
            System.out.println("\n TREE SNAPSHOT ");
            Visualizer.printTree(
                    tree.getTreeArray(),
                    tree.size(),
                    "Lazy Segment Tree - current state");
        });
        return new VBox(b);
    }

    private VBox benchmarkSection() {

        Button b = new Button("Run Benchmark");
        Label status = new Label();

        b.setOnAction(e -> {
            b.setDisable(true);
            status.setText("Running...");

            new Thread(() -> {
                Benchmark.runAll();
                Platform.runLater(() -> {
                    status.setText("Done (see console)");
                    b.setDisable(false);
                });
            }).start();
        });

        return new VBox(b, status);
    }

    private void refresh() {
        refreshRows();
        opsLabel.setText("Operations: " + OperationCounter.getOperations());
        if (showTree.isSelected() || showLazy.isSelected())
            refreshAdvanced();
    }

    private void refreshRows() {
        long[] a = tree.getOriginalArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++)
            sb.append("Row ").append(i).append(" → ").append(a[i]).append("\n");
        rows.setText(sb.toString());
    }

    private void refreshAdvanced() {
        StringBuilder sb = new StringBuilder();

        if (showTree.isSelected()) {
            sb.append("TREE\n");
            long[] t = tree.getTreeArray();
            for (int i = 1; i < t.length; i++)
                if (t[i] != 0) sb.append(i).append(": ").append(t[i]).append("\n");
        }

        if (showLazy.isSelected()) {
            sb.append("\nLAZY\n");
            long[] l = tree.getLazyArray();
            for (int i = 1; i < l.length; i++)
                if (l[i] != 0) sb.append(i).append(": ").append(l[i]).append("\n");
        }

        advanced.setText(sb.toString());
    }

    private void syncVisibility() {
        boolean show = showTree.isSelected() || showLazy.isSelected();
        advanced.setVisible(show);
        advanced.setManaged(show);
        if (show) refreshAdvanced();
    }

    private ComboBox<Integer> boxIndex() {
        ComboBox<Integer> c = new ComboBox<>();
        for (int i = 0; i < INITIAL.length; i++) c.getItems().add(i);
        c.setValue(0);
        return c;
    }

    private VBox box(String title, Control c) {
        return new VBox(new Label(title), c);
    }

    private VBox row(String title, String l1, Control c1,
                     String l2, Control c2, Control btn) {
        HBox h = new HBox(8,
                new Label(l1), c1,
                new Label(l2), c2,
                btn);
        h.setAlignment(Pos.CENTER_LEFT);
        return new VBox(new Label(title), h);
    }

    private VBox row(String title, String l1, Control c1,
                     String l2, Control c2,
                     String l3, Control c3, Control btn) {

        HBox h = new HBox(8,
                new Label(l1), c1,
                new Label(l2), c2,
                new Label(l3), c3,
                btn);
        h.setAlignment(Pos.CENTER_LEFT);

        return new VBox(new Label(title), h);
    }
}