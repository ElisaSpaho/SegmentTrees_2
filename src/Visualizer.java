/**
 * Visualizer
 * ─────────────────────────────────────────────────────────────────────────────
 * Static utility class for rendering Segment Tree state to the console.
 *
 * All methods are pure console output (System.out). No JavaFX dependency.
 * The GUI (DashboardController) produces its own text by calling the same
 * accessor methods (getTreeArray(), getLazyArray(), getOriginalArray()) —
 * this keeps the two output channels independent but consistent.
 *
 * METHODS:
 *   printArray(arr, label)                  — pretty-print a flat array
 *   printTree(tree, n, label)               — level-order segment tree view
 *   printLazyState(tree, lazy, n, label)    — side-by-side tree vs lazy view
 *   printRangeHighlight(arr, l, r, label)   — highlight a query range
 *   printUpdateDiff(before, after, idx, op) — before/after point update
 *   printSummaryBox(operation, result, complexity) — framed result box
 *   divider(title)                          — section separator
 *   pause(millis)                           — sleep helper for demos
 */
public class Visualizer {

    // ── ANSI colour codes ────────────────────────────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";

    /** Arrays longer than this are shown in compact form (first + last few values). */
    private static final int DETAIL_THRESHOLD = 32;

    // ── 1. Array Display ─────────────────────────────────────────────────────

    /**
     * Print a flat array with values in a bordered row and 0-based indices below.
     *
     * Example output (arr = {3, 1, 4, 1, 5}):
     *   Initial Data
     *   ┌────┬────┬────┬────┬────┐
     *   │  3 │  1 │  4 │  1 │  5 │
     *   └────┴────┴────┴────┴────┘
     *    [0]  [1]  [2]  [3]  [4]
     *
     * @param arr   array to display
     * @param label section title
     */
    public static void printArray(long[] arr, String label) {
        System.out.println("\n" + BOLD + CYAN + label + RESET);

        if (arr == null || arr.length == 0) {
            System.out.println("  (empty)");
            return;
        }

        // Compact display for large arrays
        if (arr.length > DETAIL_THRESHOLD) {
            System.out.printf("  [%d elements]  [ ", arr.length);
            for (int i = 0; i < Math.min(6, arr.length); i++) {
                System.out.printf("%d, ", arr[i]);
            }
            System.out.printf("... %d, %d ]%n", arr[arr.length - 2], arr[arr.length - 1]);
            return;
        }

        // Compute column width to fit the widest value
        int cellW = 4;
        for (long v : arr) {
            cellW = Math.max(cellW, String.valueOf(v).length() + 2);
        }
        String h = "─".repeat(cellW);

        // Top border
        System.out.print("  ┌");
        for (int i = 0; i < arr.length; i++) System.out.print(h + (i < arr.length - 1 ? "┬" : "┐"));
        System.out.println();

        // Values
        System.out.print("  │");
        for (long v : arr) System.out.printf("%" + cellW + "s│", v);
        System.out.println();

        // Bottom border
        System.out.print("  └");
        for (int i = 0; i < arr.length; i++) System.out.print(h + (i < arr.length - 1 ? "┴" : "┘"));
        System.out.println();

        // Index labels
        System.out.print("   ");
        for (int i = 0; i < arr.length; i++) {
            System.out.printf("%-" + cellW + "s", "[" + i + "]");
        }
        System.out.println();
    }

    // ── 2. Segment Tree Level-Order Display ───────────────────────────────────

    /**
     * Print the Segment Tree level by level, showing each node's covered
     * segment and its stored sum value.
     *
     * Only practical for n ≤ 16 (larger trees just print a compact summary).
     *
     * @param treeArr the internal tree array (1-indexed, size 4n)
     * @param n       number of leaf elements in the original array
     * @param label   section title
     */
    public static void printTree(long[] treeArr, int n, String label) {
        System.out.println("\n" + BOLD + YELLOW + label + RESET);

        if (n > 16) {
            System.out.printf("  (Tree too large for full display — n=%d)%n", n);
            System.out.printf("  Root covers [0..%d], sum = %d%n%n", n - 1, treeArr[1]);
            return;
        }

        int depth    = (int) Math.ceil(Math.log(n) / Math.log(2)) + 1;
        int maxNodes = (int) Math.pow(2, depth);

        // Parallel arrays to track (node, start, end) across BFS levels
        int[] starts = new int[maxNodes];
        int[] ends   = new int[maxNodes];
        for (int i = 0; i < maxNodes; i++) { starts[i] = -1; ends[i] = -1; }
        starts[1] = 0;
        ends[1]   = n - 1;

        int level     = 0;
        int levelSize = 1;
        int baseIdx   = 1;

        System.out.println();
        while (baseIdx < maxNodes && level < depth) {
            int indent = (int) Math.pow(2, depth - level) - 1;
            int gap    = (int) Math.pow(2, depth - level + 1) - 1;

            System.out.print(" ".repeat(indent * 3));
            boolean anyPrinted = false;

            for (int pos = 0; pos < levelSize; pos++) {
                int nd = baseIdx + pos;
                if (nd >= maxNodes || nd >= treeArr.length) break;

                int s = starts[nd];
                int e = ends[nd];

                if (s >= 0 && e >= 0 && s <= e && e < n) {
                    long val = treeArr[nd];
                    if (s == e) {
                        // Leaf node — green
                        System.out.printf(GREEN + "[%d]=%d" + RESET, s, val);
                    } else {
                        // Internal node — blue
                        System.out.printf(BLUE + "[%d-%d]=%d" + RESET, s, e, val);
                    }
                    anyPrinted = true;

                    // Enqueue children with their segments
                    int mid = (s + e) / 2;
                    if (2 * nd < maxNodes) {
                        starts[2 * nd] = s;     ends[2 * nd] = mid;
                    }
                    if (2 * nd + 1 < maxNodes) {
                        starts[2 * nd + 1] = mid + 1; ends[2 * nd + 1] = e;
                    }
                }
                if (pos < levelSize - 1) System.out.print(" ".repeat(gap * 3));
            }

            if (!anyPrinted) break;
            System.out.println();
            baseIdx   += levelSize;
            levelSize *= 2;
            level++;
        }
        System.out.println();
    }

    // ── 3. Lazy State Display ────────────────────────────────────────────────

    /**
     * Print tree[] and lazy[] side by side for the first few levels of the tree.
     * Non-zero lazy entries are highlighted in YELLOW to show pending work.
     *
     * This is the console equivalent of the GUI's Lazy Toggle View.
     *
     * @param treeArr internal tree array (1-indexed)
     * @param lazyArr internal lazy array (1-indexed)
     * @param n       original array size
     * @param label   section title
     */
    public static void printLazyState(long[] treeArr, long[] lazyArr, int n, String label) {
        System.out.println("\n" + BOLD + YELLOW + label + RESET);
        System.out.printf("  %-6s  %-14s  %-14s  %s%n",
                "Node", "tree[node]", "lazy[node]", "Status");
        System.out.println("  " + "─".repeat(55));

        // Show at most the first ~4 levels (nodes 1..31) — enough for demonstration
        int limit = Math.min(4 * n, 31);
        for (int i = 1; i <= limit; i++) {
            if (i >= treeArr.length) break;
            String status;
            if (lazyArr[i] != 0) {
                status = YELLOW + "PENDING  +" + lazyArr[i] + RESET;
            } else {
                status = "clean";
            }
            System.out.printf("  %-6d  %-14d  %-14d  %s%n",
                    i, treeArr[i], lazyArr[i], status);
        }
        System.out.println();
    }

    // ── 4. Range Highlight ───────────────────────────────────────────────────

    /**
     * Print the array with the query range [l..r] highlighted in YELLOW.
     *
     * @param arr   current array values
     * @param l     left  boundary of highlighted range
     * @param r     right boundary of highlighted range
     * @param label section title
     */
    public static void printRangeHighlight(long[] arr, int l, int r, String label) {
        System.out.println("\n" + BOLD + CYAN + label + RESET);
        System.out.printf("  Highlighted range: [%d .. %d]%n", l, r);

        if (arr.length > DETAIL_THRESHOLD) {
            System.out.printf("  (Array too large to display fully — n=%d)%n", arr.length);
            return;
        }

        System.out.print("  [ ");
        for (int i = 0; i < arr.length; i++) {
            if (i >= l && i <= r) {
                System.out.print(YELLOW + BOLD + arr[i] + RESET);
            } else {
                System.out.print(arr[i]);
            }
            if (i < arr.length - 1) System.out.print(", ");
        }
        System.out.println(" ]");

        // Marker row: L … R below the range
        System.out.print("    ");
        for (int i = 0; i < arr.length; i++) {
            String marker = (i == l) ? "L" : (i == r) ? "R" : (i > l && i < r) ? "~" : " ";
            System.out.printf("%-" + (String.valueOf(arr[i]).length() + 2) + "s", marker);
        }
        System.out.println();
    }

    // ── 5. Before / After Update Comparison ──────────────────────────────────

    /**
     * Show the array state before and after a point update, with the changed
     * index highlighted in RED (before) and GREEN (after).
     *
     * @param before     array snapshot taken before the update
     * @param after      array snapshot taken after the update
     * @param changedIdx index that was modified (-1 for range)
     * @param operation  human-readable description of the operation
     */
    public static void printUpdateDiff(long[] before, long[] after, int changedIdx, String operation) {
        System.out.println("\n" + BOLD + GREEN + "◆ UPDATE: " + operation + RESET);

        if (before.length > DETAIL_THRESHOLD) {
            System.out.printf("  Before[%d] = %d%n", changedIdx, before[changedIdx]);
            System.out.printf("  After [%d] = %d%n", changedIdx, after[changedIdx]);
            return;
        }

        System.out.print("  BEFORE → ");
        printInlineHighlighted(before, changedIdx, RED);

        System.out.print("  AFTER  → ");
        printInlineHighlighted(after, changedIdx, GREEN);

        System.out.println();
    }

    /** Helper: print array inline, colouring one index differently. */
    private static void printInlineHighlighted(long[] arr, int highlightIdx, String colour) {
        System.out.print("[ ");
        for (int i = 0; i < arr.length; i++) {
            if (i == highlightIdx) {
                System.out.print(colour + BOLD + arr[i] + RESET);
            } else {
                System.out.print(arr[i]);
            }
            if (i < arr.length - 1) System.out.print(", ");
        }
        System.out.println(" ]");
    }

    // ── 6. Summary Box ───────────────────────────────────────────────────────

    /**
     * Print a double-bordered summary box displaying the operation result.
     *
     * Example:
     *   ╔════════════════════════════════════════════════╗
     *   ║  Operation : Range Sum Query [2..5]            ║
     *   ║  Result    : 17                                ║
     *   ║  Complexity: O(log n)                          ║
     *   ╚════════════════════════════════════════════════╝
     *
     * @param operation  description string
     * @param result     computed result as a string
     * @param complexity complexity class (e.g. "O(log n)")
     */
    public static void printSummaryBox(String operation, String result, String complexity) {
        int width  = Math.max(50, operation.length() + result.length() + 16);
        String bar = "═".repeat(width);

        System.out.println();
        System.out.println("  ╔" + bar + "╗");
        System.out.printf( "  ║  %-" + (width - 2) + "s║%n", "Operation : " + operation);
        System.out.printf( "  ║  %-" + (width - 2) + "s║%n",
                BOLD + GREEN + "Result    : " + result + RESET);
        System.out.printf( "  ║  %-" + (width - 2) + "s║%n", "Complexity: " + complexity);
        System.out.println("  ╚" + bar + "╝");
        System.out.println();
    }

    // ── 7. Section Divider ───────────────────────────────────────────────────

    /**
     * Print a section divider with a centred title.
     *
     * @param title section name
     */
    public static void divider(String title) {
        int padLen = Math.max(0, 56 - title.length());
        System.out.println("\n" + BOLD + "──── " + title + " " + "─".repeat(padLen) + RESET);
    }

    // ── 8. Pause Helper ──────────────────────────────────────────────────────

    /**
     * Sleep for {@code millis} milliseconds.
     * Useful for paced console demos.
     *
     * @param millis duration in milliseconds
     */
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
