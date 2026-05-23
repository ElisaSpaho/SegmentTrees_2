public class Visualizer {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";

    // ── 1. Tree Structure Display ─────────────────────────────────────────────
    // Called by the GUI "Print Tree" button via DashboardController.

    public static void printTree(long[] treeArr, int n, String label) {
        System.out.println("\n" + BOLD + YELLOW + label + RESET);

        if (n > 16) {
            System.out.printf("  (Tree too large for full display — n=%d)%n", n);
            System.out.printf("  Root covers [0..%d], sum = %d%n%n", n - 1, treeArr[1]);
            return;
        }

        int depth    = (int) Math.ceil(Math.log(n) / Math.log(2)) + 1;
        int maxNodes = (int) Math.pow(2, depth);

        int[] starts = new int[maxNodes];
        int[] ends   = new int[maxNodes];
        for (int i = 0; i < maxNodes; i++) { starts[i] = -1; ends[i] = -1; }
        starts[1] = 0;
        ends[1]   = n - 1;

        int level = 0, levelSize = 1, baseIdx = 1;

        System.out.println();
        while (baseIdx < maxNodes && level < depth) {
            int indent = (int) Math.pow(2, depth - level) - 1;
            int gap    = (int) Math.pow(2, depth - level + 1) - 1;

            System.out.print(" ".repeat(indent * 3));
            boolean anyPrinted = false;

            for (int pos = 0; pos < levelSize; pos++) {
                int nd = baseIdx + pos;
                if (nd >= maxNodes || nd >= treeArr.length) break;

                int s = starts[nd], e = ends[nd];
                if (s >= 0 && e >= 0 && s <= e && e < n) {
                    if (s == e)
                        System.out.printf(GREEN + "[%d]=%d" + RESET, s, treeArr[nd]);
                    else
                        System.out.printf(BLUE + "[%d-%d]=%d" + RESET, s, e, treeArr[nd]);
                    anyPrinted = true;

                    int mid = (s + e) / 2;
                    if (2 * nd < maxNodes) {
                        starts[2 * nd] = s; ends[2 * nd] = mid;
                    }
                    if (2 * nd + 1 < maxNodes) {
                        starts[2 * nd + 1] = mid + 1; ends[2 * nd + 1] = e;
                    }
                }
                if (pos < levelSize - 1) System.out.print(" ".repeat(gap * 3));
            }

            if (!anyPrinted) break;
            System.out.println();
            baseIdx += levelSize;
            levelSize *= 2;
            level++;
        }
        System.out.println();
    }

}