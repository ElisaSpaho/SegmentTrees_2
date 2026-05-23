import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * DashboardController
 * ─────────────────────────────────────────────────────────────────────────────
 * JavaFX GUI for the Cinema Ticket Sales Tracker.
 *
 * RESPONSIBILITY (GUI = interactive simulation):
 *   Show available tickets per cinema row and let the user query / update
 *   them through simple controls.  The console (Main.java) handles all
 *   deep technical output (tree nodes, lazy arrays, benchmarks).
 *
 * DEFAULT VIEW:
 *   Plain white, no styling.  Shows "Row X → Y tickets" for every row.
 *
 * OPTIONAL ADVANCED VIEW (via checkboxes):
 *   [ ] Show Internal Tree Structure  → displays getTreeArray()
 *   [ ] Show Lazy Propagation State   → displays non-zero getLazyArray() entries
 *
 * OPERATIONS:
 *   • Range Query  — sum tickets across a row range
 *   • Point Update — set ticket count for one row
 *   • Range Update — add a delta to every row in a range (lazy propagation)
 *   • Benchmark    — runs Benchmark.runAll() on a background thread
 */
public class DashboardController {

    // ── Data ─────────────────────────────────────────────────────────────────

    /** Starting ticket counts for 8 cinema rows. */
    private static final long[] INITIAL_TICKETS = {10, 15, 8, 22, 5, 12, 19, 7};

    /** The single shared tree instance used by all GUI operations. */
    private final LazySegmentTree segmentTree = new LazySegmentTree(INITIAL_TICKETS);

    // ── Controls referenced by handlers ──────────────────────────────────────

    private TextArea rowDisplay;          // main "Row X → Y tickets" display
    private TextArea advancedDisplay;     // tree / lazy internals (hidden by default)

    private Label    resultLabel;         // shows last query or update result
    private Label    opCountLabel;        // "Operations Count: X"

    private ComboBox<Integer> queryStartBox;
    private ComboBox<Integer> queryEndBox;

    private ComboBox<Integer> pointRowBox;
    private TextField         pointValueField;

    private ComboBox<Integer> rangeStartBox;
    private ComboBox<Integer> rangeEndBox;
    private TextField         rangeDeltaField;

    private CheckBox showTreeCheckBox;
    private CheckBox showLazyCheckBox;

    // ── Scene builder ─────────────────────────────────────────────────────────

    /**
     * Build and return the complete Scene.
     * Called once by RangeIntelligenceApp.start().
     */
    public Scene buildScene() {

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        // Plain white background — no colour styling
        root.setStyle("-fx-background-color: white;");

        // ── Title ─────────────────────────────────────────────────────────────
        Label title = new Label("Cinema Ticket Sales Tracker");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label("Each row represents available tickets in a cinema row.");
        subtitle.setStyle("-fx-font-size: 11px;");

        root.getChildren().addAll(title, subtitle, new Separator());

        // ── Row display ───────────────────────────────────────────────────────
        Label rowHeading = new Label("Current Ticket Availability:");
        rowHeading.setStyle("-fx-font-weight: bold;");

        rowDisplay = new TextArea();
        rowDisplay.setEditable(false);
        rowDisplay.setPrefHeight(160);
        rowDisplay.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        root.getChildren().addAll(rowHeading, rowDisplay, new Separator());

        // ── Operation counter ─────────────────────────────────────────────────
        opCountLabel = new Label("Operations Count: 0");

        Button resetCounterBtn = new Button("Reset Counter");
        resetCounterBtn.setOnAction(e -> {
            OperationCounter.reset();
            opCountLabel.setText("Operations Count: 0");
        });

        HBox counterRow = new HBox(10, opCountLabel, resetCounterBtn);
        counterRow.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(counterRow, new Separator());

        // ── Range query ───────────────────────────────────────────────────────
        Label queryHeading = new Label("Range Sum Query:");
        queryHeading.setStyle("-fx-font-weight: bold;");

        queryStartBox = makeIndexCombo();
        queryEndBox   = makeIndexCombo();
        queryEndBox.setValue(7);

        Button queryBtn = new Button("Calculate Total Tickets");
        queryBtn.setOnAction(e -> handleRangeQuery());

        HBox queryRow = new HBox(8,
                new Label("From Row:"), queryStartBox,
                new Label("To Row:"),   queryEndBox,
                queryBtn);
        queryRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(queryHeading, queryRow, new Separator());

        // ── Point update ──────────────────────────────────────────────────────
        Label pointHeading = new Label("Point Update  (set one row):");
        pointHeading.setStyle("-fx-font-weight: bold;");

        pointRowBox    = makeIndexCombo();
        pointValueField = new TextField("10");
        pointValueField.setPrefWidth(60);

        Button pointBtn = new Button("Update Ticket Count");
        pointBtn.setOnAction(e -> handlePointUpdate());

        HBox pointRow = new HBox(8,
                new Label("Row:"),       pointRowBox,
                new Label("New Count:"), pointValueField,
                pointBtn);
        pointRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(pointHeading, pointRow, new Separator());

        // ── Range update (lazy propagation) ───────────────────────────────────
        Label rangeHeading = new Label("Range Update  (add to a range, uses lazy propagation):");
        rangeHeading.setStyle("-fx-font-weight: bold;");

        rangeStartBox   = makeIndexCombo();
        rangeEndBox     = makeIndexCombo();
        rangeEndBox.setValue(7);
        rangeDeltaField = new TextField("5");
        rangeDeltaField.setPrefWidth(60);

        Button rangeBtn = new Button("Apply Range Update");
        rangeBtn.setOnAction(e -> handleRangeUpdate());

        HBox rangeRow = new HBox(8,
                new Label("From:"), rangeStartBox,
                new Label("To:"),   rangeEndBox,
                new Label("Add:"),  rangeDeltaField,
                rangeBtn);
        rangeRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(rangeHeading, rangeRow, new Separator());

        // ── Result label ──────────────────────────────────────────────────────
        resultLabel = new Label("Select an operation above.");
        resultLabel.setStyle("-fx-font-size: 12px;");
        resultLabel.setWrapText(true);
        root.getChildren().addAll(resultLabel, new Separator());

        // ── Advanced view checkboxes ──────────────────────────────────────────
        Label advancedHeading = new Label("Advanced View (optional):");
        advancedHeading.setStyle("-fx-font-weight: bold;");

        showTreeCheckBox = new CheckBox("Show Internal Tree Structure");
        showLazyCheckBox = new CheckBox("Show Lazy Propagation State");

        showTreeCheckBox.selectedProperty().addListener((obs, old, selected) -> refreshAdvancedDisplay());
        showLazyCheckBox.selectedProperty().addListener((obs, old, selected) -> refreshAdvancedDisplay());

        advancedDisplay = new TextArea();
        advancedDisplay.setEditable(false);
        advancedDisplay.setPrefHeight(130);
        advancedDisplay.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        advancedDisplay.setVisible(false);
        advancedDisplay.setManaged(false);

        // Show the advancedDisplay area whenever at least one checkbox is ticked
        showTreeCheckBox.selectedProperty().addListener((obs, old, v) -> syncAdvancedVisibility());
        showLazyCheckBox.selectedProperty().addListener((obs, old, v) -> syncAdvancedVisibility());

        root.getChildren().addAll(
                advancedHeading,
                new HBox(15, showTreeCheckBox, showLazyCheckBox),
                advancedDisplay,
                new Separator());

        // ── Benchmark ─────────────────────────────────────────────────────────
        Label benchmarkHeading = new Label("Performance Benchmark:");
        benchmarkHeading.setStyle("-fx-font-weight: bold;");

        Label benchmarkNote = new Label("Full results are printed to the console.");
        benchmarkNote.setStyle("-fx-font-size: 11px;");

        Label benchmarkStatusLabel = new Label("");

        Button benchmarkBtn = new Button("Run Performance Benchmark");
        benchmarkBtn.setOnAction(e -> {
            benchmarkBtn.setDisable(true);
            benchmarkStatusLabel.setText("Running… check console for output.");
            Thread t = new Thread(() -> {
                Benchmark.runAll();
                Platform.runLater(() -> {
                    benchmarkStatusLabel.setText("Benchmark completed. Check console output.");
                    benchmarkBtn.setDisable(false);
                });
            });
            t.setDaemon(true);
            t.start();
        });

        root.getChildren().addAll(benchmarkHeading, benchmarkNote, benchmarkBtn, benchmarkStatusLabel);

        // ── Initial refresh ───────────────────────────────────────────────────
        refreshRowDisplay();

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        return new Scene(scroll, 750, 680);
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    private void handleRangeQuery() {
        int l = queryStartBox.getValue();
        int r = queryEndBox.getValue();
        if (l > r) {
            resultLabel.setText("Error: start row must be less than or equal to end row.");
            return;
        }
        long total = segmentTree.rangeQuery(l, r);
        refreshAfterOperation();
        resultLabel.setText("Total tickets from Row " + l + " to Row " + r + " = " + total);
        System.out.println("[GUI] rangeQuery(" + l + ", " + r + ") = " + total
                + "  [nodes visited: " + OperationCounter.getOperations() + "]");
    }

    private void handlePointUpdate() {
        try {
            int  row   = pointRowBox.getValue();
            long value = Long.parseLong(pointValueField.getText().trim());
            if (value < 0) {
                resultLabel.setText("Error: ticket count cannot be negative.");
                return;
            }
            segmentTree.pointUpdate(row, value);
            refreshAfterOperation();
            resultLabel.setText("Row " + row + " updated to " + value + " tickets.");
            System.out.println("[GUI] pointUpdate(" + row + ", " + value + ")"
                    + "  [nodes visited: " + OperationCounter.getOperations() + "]");
        } catch (NumberFormatException ex) {
            resultLabel.setText("Error: please enter a valid whole number.");
        }
    }

    private void handleRangeUpdate() {
        try {
            int  l     = rangeStartBox.getValue();
            int  r     = rangeEndBox.getValue();
            long delta = Long.parseLong(rangeDeltaField.getText().trim());
            if (l > r) {
                resultLabel.setText("Error: start row must be less than or equal to end row.");
                return;
            }
            segmentTree.rangeUpdate(l, r, delta);
            refreshAfterOperation();
            resultLabel.setText("Added " + delta + " tickets to every row from "
                    + l + " to " + r + "."
                    + "  [nodes visited: " + OperationCounter.getOperations() + "]");
            System.out.println("[GUI] rangeUpdate(" + l + ", " + r + ", " + delta + ")"
                    + "  [nodes visited: " + OperationCounter.getOperations() + "]");
        } catch (NumberFormatException ex) {
            resultLabel.setText("Error: please enter a valid whole number.");
        }
    }

    // ── Refresh helpers ───────────────────────────────────────────────────────

    /**
     * Call after every tree operation — refreshes the row display,
     * operation counter, and (if visible) the advanced panel.
     */
    private void refreshAfterOperation() {
        refreshRowDisplay();
        opCountLabel.setText("Operations Count: " + OperationCounter.getOperations());
        if (showTreeCheckBox.isSelected() || showLazyCheckBox.isSelected()) {
            refreshAdvancedDisplay();
        }
    }

    /**
     * Rebuild the plain "Row X → Y tickets" display from the current leaf values.
     * Uses getOriginalArray() which reflects all updates (point and range).
     */
    private void refreshRowDisplay() {
        long[] values = segmentTree.getOriginalArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(String.format("Row %d  →  %d tickets%n", i, values[i]));
        }
        rowDisplay.setText(sb.toString());
    }

    /**
     * Rebuild the advanced display based on which checkboxes are ticked.
     *
     * "Show Internal Tree Structure" → tree[] nodes (non-zero, 1-indexed)
     * "Show Lazy Propagation State"  → lazy[] nodes where lazy[i] != 0 only
     */
    private void refreshAdvancedDisplay() {
        if (!showTreeCheckBox.isSelected() && !showLazyCheckBox.isSelected()) {
            advancedDisplay.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (showTreeCheckBox.isSelected()) {
            long[] tree = segmentTree.getTreeArray();
            sb.append("── Internal Tree Nodes ──────────────────────────────\n");
            sb.append("(Each node stores the sum of its segment.)\n\n");
            for (int i = 1; i < tree.length; i++) {
                if (tree[i] != 0) {
                    sb.append(String.format("Node %2d  →  %d%n", i, tree[i]));
                }
            }
            if (showLazyCheckBox.isSelected()) sb.append("\n");
        }

        if (showLazyCheckBox.isSelected()) {
            long[] lazy = segmentTree.getLazyArray();
            sb.append("── Lazy Propagation State ───────────────────────────\n");
            sb.append("(Non-zero = pending update not yet pushed to children.)\n\n");
            boolean anyPending = false;
            for (int i = 1; i < lazy.length; i++) {
                if (lazy[i] != 0) {
                    sb.append(String.format("Lazy Node %2d  →  +%d%n", i, lazy[i]));
                    anyPending = true;
                }
            }
            if (!anyPending) {
                sb.append("(All nodes clean — no pending lazy values.)\n");
            }
        }

        advancedDisplay.setText(sb.toString());
    }

    /** Show or hide the advanced TextArea based on checkbox state. */
    private void syncAdvancedVisibility() {
        boolean show = showTreeCheckBox.isSelected() || showLazyCheckBox.isSelected();
        advancedDisplay.setVisible(show);
        advancedDisplay.setManaged(show);
        if (show) refreshAdvancedDisplay();
    }

    // ── Factory helper ────────────────────────────────────────────────────────

    /** ComboBox pre-loaded with indices 0 – 7 (one per cinema row). */
    private ComboBox<Integer> makeIndexCombo() {
        ComboBox<Integer> cb = new ComboBox<>();
        for (int i = 0; i < INITIAL_TICKETS.length; i++) cb.getItems().add(i);
        cb.setValue(0);
        return cb;
    }
}