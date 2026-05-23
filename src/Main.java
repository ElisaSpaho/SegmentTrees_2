/**
 * Main
 * ─────────────────────────────────────────────────────────────────────────────
 * Console entry point for the Range Intelligence Engine.
 *
 * Run this class to demonstrate:
 *   1. SegmentTree      — Range Sum, Range Min, Point Update
 *   2. LazySegmentTree  — Range Update (lazy), Range Query, operation counts
 *   3. Visualizer       — all console output helpers
 *   4. OperationCounter — node-visit metrics proving O(log n) complexity
 *   5. Correctness tests (LazySegmentTreeTest)
 *   6. Performance benchmarks (Benchmark)
 *
 * The JavaFX GUI is launched separately via RangeIntelligenceApp.
 * Both share the same class files — consistent behaviour guaranteed.
 *
 * HOW TO COMPILE AND RUN (console only):
 *   javac *.java
 *   java  Main
 */
public class Main {

    public static void main(String[] args) {

        // ──────────────────────────────────────────────────────────────────
        //  PART 1 — STANDARD SEGMENT TREE
        //  Demonstrates: build, range sum query, range min query, point update
        // ──────────────────────────────────────────────────────────────────

        long[] arr = {2, 4, 3, 1, 6, 7, 2, 5};   // 8-element demo array

        SegmentTree st = new SegmentTree(arr);

        Visualizer.divider("ORIGINAL ARRAY");
        Visualizer.printArray(arr, "Initial Data");

        Visualizer.divider("SEGMENT TREE STRUCTURE");
        Visualizer.printTree(st.getTreeArray(), st.size(), "Tree (sum nodes)");

        // -- Range Sum Query [2..5] ----------------------------------------
        int l = 2, r = 5;
        long sum = st.rangeQuery(l, r);           // consistent name: rangeQuery
        Visualizer.printRangeHighlight(arr, l, r, "Range Sum Query [" + l + ".." + r + "]");
        Visualizer.printSummaryBox(
                "rangeQuery(" + l + ", " + r + ")",
                String.valueOf(sum),
                "O(log n)");

        // -- Range Min Query [2..5] ----------------------------------------
        long min = st.queryMin(l, r);
        Visualizer.printSummaryBox(
                "queryMin(" + l + ", " + r + ")",
                String.valueOf(min),
                "O(log n)");

        // -- Point Update at index 3 ---------------------------------------
        long[] before = st.getOriginalArray();
        st.pointUpdate(3, 10);                    // consistent name: pointUpdate
        long[] after = st.getOriginalArray();
        Visualizer.printUpdateDiff(before, after, 3, "pointUpdate(3, 10)");

        // ──────────────────────────────────────────────────────────────────
        //  PART 2 — LAZY SEGMENT TREE
        //  Demonstrates: rangeUpdate (lazy propagation), rangeQuery,
        //                OperationCounter showing O(log n) node visits
        // ──────────────────────────────────────────────────────────────────

        LazySegmentTree lst = new LazySegmentTree(arr);

        Visualizer.divider("LAZY SEGMENT TREE — RANGE UPDATE");
        System.out.println("\n  Applying: rangeUpdate(1, 5, +5)");
        System.out.println("  This adds 5 to every element in positions 1 through 5.");
        System.out.println("  With lazy propagation, only O(log n) nodes are touched.\n");

        // OperationCounter is reset inside rangeUpdate()
        lst.rangeUpdate(1, 5, 5);
        OperationCounter.print("rangeUpdate(1, 5, 5)");

        Visualizer.printLazyState(
                lst.getTreeArray(),
                lst.getLazyArray(),
                lst.size(),
                "Lazy Tree State after rangeUpdate(1, 5, 5)");

        // -- Range Query after lazy update ---------------------------------
        // Reset happens inside rangeQuery()
        long lazySum = lst.rangeQuery(1, 5);
        OperationCounter.print("rangeQuery(1, 5)");

        Visualizer.printSummaryBox(
                "rangeQuery(1, 5) after rangeUpdate(1,5,+5)",
                String.valueOf(lazySum),
                "O(log n)");

        System.out.println("  COMPLEXITY COMPARISON:");
        System.out.println("    Naive range update [1..5]: would visit every leaf → O(n)");
        System.out.println("    Lazy range update  [1..5]: tagged " +
                OperationCounter.get() + " nodes only         → O(log n)\n");

        // ──────────────────────────────────────────────────────────────────
        //  PART 3 — CORRECTNESS TESTS
        // ──────────────────────────────────────────────────────────────────

        LazySegmentTreeTest.runTests();

        // ──────────────────────────────────────────────────────────────────
        //  PART 4 — PERFORMANCE BENCHMARK
        // ──────────────────────────────────────────────────────────────────

        Benchmark.runAll();
    }
}