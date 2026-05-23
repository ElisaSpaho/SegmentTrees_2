/**
 * Benchmark
 * ─────────────────────────────────────────────────────────────────────────────
 * Compares the performance of two approaches to range updates:
 *
 *   NAIVE APPROACH  — iterate every element in [l..r] and add the value.
 *                     Time per update: O(n).  Bad for large arrays.
 *
 *   LAZY SEGMENT TREE — record the update at covering nodes only; propagate
 *                     lazily later.  Time per update: O(log n).  Scalable.
 *
 * The benchmark runs 100 full-range updates on increasing array sizes and
 * measures wall-clock time for each approach.
 *
 * HOW TO USE FROM THE GUI:
 *   Button "Run Performance Benchmark" calls Benchmark.runAll().
 *   The full table is printed to the console (stdout).
 *   After runAll() returns, call getSummary() to get a short one-line result
 *   suitable for a GUI label.
 *
 * NOTE: Do not add networking, databases, or file I/O to this class.
 *       Keep it simple — time two loops, print a table, done.
 */
public class Benchmark {

    // ── Configuration ────────────────────────────────────────────────────────

    /** Array sizes used for each benchmark round. */
    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};

    /** Number of full-range updates performed per timing run. */
    private static final int OPS_PER_RUN = 100;

    // ── State ────────────────────────────────────────────────────────────────

    /**
     * Short summary of the last completed benchmark.
     * Returned by getSummary() for the GUI label.
     */
    private static String lastSummary = "Benchmark not run yet.";

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Run all benchmark rounds and print a formatted table to stdout.
     * Also stores a compact summary accessible via getSummary().
     *
     * Called directly by DashboardController's benchmark button handler.
     * Do NOT modify this method signature — the GUI depends on it.
     */
    public static void runAll() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║       PERFORMANCE BENCHMARK — Naive O(n) vs Lazy O(log n)       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  %-12s  %-18s  %-16s  %-8s  ║%n",
                "Array Size", "Naive Update (ms)", "Lazy Update (ms)", "Speedup");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        StringBuilder summaryBuilder = new StringBuilder();

        for (int size : SIZES) {
            long naiveMs = benchmarkNaive(size);
            long lazyMs  = benchmarkLazy(size);
            String speedup = (lazyMs == 0)
                    ? "∞×"
                    : String.format("%.1f×", (double) naiveMs / lazyMs);

            System.out.printf("║  %-12d  %-18d  %-16d  %-8s  ║%n",
                    size, naiveMs, lazyMs, speedup);

            summaryBuilder.append(String.format("n=%,d: Naive=%dms / Lazy=%dms (%s)   ",
                    size, naiveMs, lazyMs, speedup));
        }

        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.printf("  Each round: %d full-range updates on array of given size.%n%n",
                OPS_PER_RUN);

        lastSummary = summaryBuilder.toString().trim();
    }

    /**
     * Return a compact one-line summary of the last benchmark run.
     * Intended for display in the GUI result label after runAll() completes.
     *
     * @return summary string, or a "not run yet" message if runAll() has never been called
     */
    public static String getSummary() {
        return lastSummary;
    }

    // ── Naive Implementation — O(n) per update ───────────────────────────────

    /**
     * Simulates a brute-force range update: iterate every element in [0..n-1]
     * and add 1 to it.  This is what you would write without a Segment Tree.
     *
     * Complexity per update: O(n).
     * Total for OPS_PER_RUN updates: O(n * OPS_PER_RUN).
     *
     * @param size number of elements in the array
     * @return elapsed wall-clock time in milliseconds
     */
    private static long benchmarkNaive(int size) {
        long[] arr   = buildArray(size);
        long   start = System.currentTimeMillis();

        for (int op = 0; op < OPS_PER_RUN; op++) {
            // O(n): visit every element
            for (int i = 0; i < arr.length; i++) {
                arr[i] += 1;
            }
        }

        return System.currentTimeMillis() - start;
    }

    // ── Lazy Segment Tree — O(log n) per update ──────────────────────────────

    /**
     * Performs the same OPS_PER_RUN full-range updates using LazySegmentTree.
     * Expected to be significantly faster for large n because each update
     * tags O(log n) covering nodes rather than touching O(n) leaves.
     *
     * A single rangeQuery at the end forces any remaining lazy values to be
     * flushed, ensuring a fair comparison (all work is done before timing stops).
     *
     * @param size number of elements in the array
     * @return elapsed wall-clock time in milliseconds
     */
    private static long benchmarkLazy(int size) {
        long[]          arr   = buildArray(size);
        LazySegmentTree lst   = new LazySegmentTree(arr);
        long            start = System.currentTimeMillis();

        for (int op = 0; op < OPS_PER_RUN; op++) {
            // O(log n): tag covering nodes only
            lst.rangeUpdate(0, size - 1, 1);
        }
        // Force a flush of all lazy values (fair — ensures all work is counted)
        lst.rangeQuery(0, size - 1);

        return System.currentTimeMillis() - start;
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /** Build a simple ascending array of given size: [1, 2, 3, … size]. */
    private static long[] buildArray(int size) {
        long[] arr = new long[size];
        for (int i = 0; i < size; i++) arr[i] = i + 1L;
        return arr;
    }
}