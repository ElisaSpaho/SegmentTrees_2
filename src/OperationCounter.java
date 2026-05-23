/**
 * OperationCounter
 * ─────────────────────────────────────────────────────────────────────────────
 * Tracks how many tree nodes are visited during a single Segment Tree
 * operation (rangeQuery, rangeUpdate, pointUpdate).
 *
 * Used by LazySegmentTree (incremented during recursion) and by
 * DashboardController (read after each operation to update the GUI label).
 */
public class OperationCounter {

    private static long count = 0;

    /** Reset to zero — call before each new operation. */
    public static void reset() {
        count = 0;
    }

    /** Increment by one — call once per tree node visited. */
    public static void increment() {
        count++;
    }

    /** Return the current count (does not reset). */
    public static long get() {
        return count;
    }

    /**
     * Alias for get() — keeps the GUI code readable.
     * Both names refer to the same value.
     */
    public static long getOperations() {
        return count;
    }

    /** Print a labelled summary line to stdout. */
    public static void print(String operationLabel) {
        System.out.printf("  [OperationCounter] %-35s → %d node(s) visited%n",
                operationLabel, count);
    }
}