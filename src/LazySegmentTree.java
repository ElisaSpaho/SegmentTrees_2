/**
 * LazySegmentTree
 * ─────────────────────────────────────────────────────────────────────────────
 * A Range Sum Segment Tree with Lazy Propagation for efficient range updates.
 *
 * WHY "LAZY"?
 *   A standard Segment Tree requires O(n) time to apply a value to every
 *   element in a range (you must update every leaf). Lazy propagation defers
 *   these updates: instead of pushing a change all the way to the leaves
 *   immediately, we record the pending change in a lazy[] array at each
 *   covering node and only propagate it downward when we actually need to
 *   read or further update a child. This keeps BOTH range updates AND range
 *   queries at O(log n) — a crucial optimization for large datasets.
 *
 * HOW THE LAZY ARRAY WORKS:
 *   lazy[i] holds an additive value that has been applied to node i's
 *   tree[] sum but has NOT YET been pushed to node i's children.
 *   lazy[i] == 0 means node i is "clean" (no pending work).
 *   When we push down, we:
 *     1. Apply lazy[i] * childSize to each child's tree[] value.
 *     2. Add lazy[i] to each child's lazy[] value (accumulate, don't replace).
 *     3. Clear lazy[i] = 0.
 *
 * SUPPORTED OPERATIONS (all with consistent names):
 *   rangeQuery(l, r)        — sum  of elements in [l..r]    O(log n)
 *   rangeUpdate(l, r, val)  — add val to every element in [l..r]  O(log n)
 *   pointUpdate(idx, val)   — set arr[idx] = val            O(log n)
 *
 * ACCESSORS FOR VISUALIZATION:
 *   getTreeArray()     — internal node sums    (for Visualizer / GUI)
 *   getLazyArray()     — pending lazy values   (for Lazy Toggle View)
 *   getOriginalArray() — current leaf values   (cinema row ticket counts)
 */
public class LazySegmentTree {

    // ── Fields ───────────────────────────────────────────────────────────────

    /**
     * Internal sum tree (1-indexed flat array).
     * tree[i] = sum of all elements in the segment that node i covers,
     *           INCLUDING any lazy[i] already folded into this node.
     */
    private final long[] tree;

    /**
     * Lazy array (1-indexed flat array).
     * lazy[i] = additive delta that has been applied to tree[i] but NOT yet
     *           propagated to node i's children.
     * Zero = clean (no pending update for this node's children).
     */
    private final long[] lazy;

    /** Number of elements in the original input array. */
    private final int n;

    /**
     * Working copy of the leaf values, kept in sync after every operation.
     * Used by getOriginalArray() so the GUI can display per-row ticket counts.
     */
    private final long[] original;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Build a Lazy Segment Tree from the given array in O(n).
     *
     * @param arr non-null, non-empty array of long values
     * @throws IllegalArgumentException if arr is null or empty
     */
    public LazySegmentTree(long[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Input array must be non-null and non-empty.");
        }
        n        = arr.length;
        original = arr.clone();
        tree     = new long[4 * n];    // 4n is a safe upper bound for node count
        lazy     = new long[4 * n];    // initialised to 0 by Java (all clean)

        build(1, 0, n - 1);
    }

    // ── Build — O(n) ─────────────────────────────────────────────────────────

    /**
     * Recursively build the sum tree bottom-up.
     * The lazy array starts entirely at zero (no pending updates).
     */
    private void build(int node, int start, int end) {
        if (start == end) {
            tree[node] = original[start];   // leaf stores the element directly
            return;
        }
        int mid = (start + end) / 2;
        build(2 * node,     start, mid);
        build(2 * node + 1, mid + 1, end);
        tree[node] = tree[2 * node] + tree[2 * node + 1];   // merge children
    }

    // ── Lazy Push-Down ───────────────────────────────────────────────────────

    /**
     * Push the pending lazy value at {@code node} down to its two children.
     *
     * This is the heart of lazy propagation. It must be called before we
     * descend into either child during a partial-overlap query or update.
     *
     * STEP BY STEP:
     *   If lazy[node] == 0 there is nothing to do — return immediately.
     *   Otherwise:
     *     LEFT  child covers (mid - start + 1) elements.
     *     RIGHT child covers (end - mid) elements.
     *     Each child's tree[] increases by lazy[node] * its element count.
     *     Each child's lazy[] accumulates lazy[node] for future push-downs.
     *     Finally clear lazy[node] = 0.
     *
     * @param node  current node whose lazy value should be propagated
     * @param start left boundary of node's segment
     * @param end   right boundary of node's segment
     */
    private void pushDown(int node, int start, int end) {
        if (lazy[node] == 0) return;   // node is clean — nothing to propagate

        int mid        = (start + end) / 2;
        int leftChild  = 2 * node;
        int rightChild = 2 * node + 1;

        // ── Apply to left child ──────────────────────────────────────────────
        int leftSize          = mid - start + 1;
        tree[leftChild]      += lazy[node] * leftSize;
        lazy[leftChild]      += lazy[node];   // accumulate, don't overwrite

        // ── Apply to right child ─────────────────────────────────────────────
        int rightSize         = end - mid;
        tree[rightChild]     += lazy[node] * rightSize;
        lazy[rightChild]     += lazy[node];

        // ── Clear this node — pending work has been passed to children ────────
        lazy[node] = 0;
    }

    // ── Range Update — O(log n) ──────────────────────────────────────────────

    /**
     * Add {@code val} to every element in the range [l..r].
     *
     * COMPLEXITY: O(log n) — only O(log n) nodes are fully covered and tagged;
     * the rest are left for future pushDown() calls (lazy = deferred work).
     *
     * This method:
     *   1. Resets OperationCounter (so GUI shows count for THIS operation only).
     *   2. Delegates to the recursive helper.
     *   3. Keeps original[] in sync so getOriginalArray() reflects reality.
     *
     * @param l   left  boundary (0-indexed, inclusive)
     * @param r   right boundary (0-indexed, inclusive)
     * @param val additive delta to apply to every element in [l..r]
     */
    public void rangeUpdate(int l, int r, long val) {
        validateRange(l, r);
        OperationCounter.reset();
        rangeUpdateHelper(1, 0, n - 1, l, r, val);
        // Sync the original[] array so the GUI can display updated leaf values
        for (int i = l; i <= r; i++) {
            original[i] += val;
        }
    }

    /**
     * Recursive range-update helper.
     *
     * THREE CASES:
     *   NO OVERLAP    — this node's segment [start..end] is entirely outside
     *                   [l..r]. Do nothing.
     *   TOTAL OVERLAP — this node's segment is entirely inside [l..r].
     *                   Apply the update to tree[node] and record it in lazy[node].
     *                   Do NOT recurse further (lazy = defer to children).
     *   PARTIAL OVERLAP — push any pending lazy value down first, then recurse
     *                   on both children and re-merge.
     */
    private void rangeUpdateHelper(int node, int start, int end, int l, int r, long val) {
        OperationCounter.increment();    // count every node we enter

        if (r < start || end < l) return;   // no overlap — skip entirely

        if (l <= start && end <= r) {
            // Total overlap — apply here and record for children
            tree[node] += val * (end - start + 1);
            lazy[node] += val;
            return;
        }

        // Partial overlap — must descend; push down first so children are accurate
        pushDown(node, start, end);
        int mid = (start + end) / 2;
        rangeUpdateHelper(2 * node,     start, mid,   l, r, val);
        rangeUpdateHelper(2 * node + 1, mid + 1, end, l, r, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];   // re-merge
    }

    // ── Range Query — O(log n) ───────────────────────────────────────────────

    /**
     * Return the sum of all elements in the range [l..r].
     *
     * Lazy push-downs happen on-the-fly as we descend into partial-overlap
     * nodes, ensuring every node we read is up-to-date.
     *
     * @param l left  boundary (0-indexed, inclusive)
     * @param r right boundary (0-indexed, inclusive)
     * @return  sum of arr[l..r] (including all pending lazy updates)
     */
    public long rangeQuery(int l, int r) {
        validateRange(l, r);
        OperationCounter.reset();
        return rangeQueryHelper(1, 0, n - 1, l, r);
    }

    /**
     * Recursive range-query helper.
     * Mirrors rangeUpdateHelper's three-case logic but accumulates sums.
     */
    private long rangeQueryHelper(int node, int start, int end, int l, int r) {
        OperationCounter.increment();

        if (r < start || end < l) return 0L;           // no overlap → identity
        if (l <= start && end <= r) return tree[node]; // total overlap → done

        // Partial overlap — push down so children reflect this node's lazy value
        pushDown(node, start, end);
        int mid = (start + end) / 2;
        long leftSum  = rangeQueryHelper(2 * node,     start, mid,   l, r);
        long rightSum = rangeQueryHelper(2 * node + 1, mid + 1, end, l, r);
        return leftSum + rightSum;
    }

    // ── Point Update — O(log n) ──────────────────────────────────────────────

    /**
     * Set the element at position {@code idx} to the absolute value {@code newVal}.
     *
     * Internally converts to an additive delta so the lazy structure remains
     * consistent: delta = newVal - original[idx], then treats it like a
     * rangeUpdate([idx..idx], delta).
     *
     * @param idx    index to update (0-indexed)
     * @param newVal new absolute ticket count to store at arr[idx]
     */
    public void pointUpdate(int idx, long newVal) {
        if (idx < 0 || idx >= n) {
            throw new IndexOutOfBoundsException(
                "Index " + idx + " out of bounds for array of size " + n);
        }
        long delta    = newVal - original[idx];   // compute delta BEFORE updating original
        original[idx] = newVal;                   // sync original[]
        OperationCounter.reset();
        pointUpdateHelper(1, 0, n - 1, idx, delta);
    }

    /**
     * Recursive point-update helper.
     * Navigates to the target leaf using pushDown at each level, then
     * re-merges ancestors on the way back up.
     */
    private void pointUpdateHelper(int node, int start, int end, int idx, long delta) {
        OperationCounter.increment();
        if (start == end) {
            tree[node] += delta;
            return;
        }
        pushDown(node, start, end);   // keep lazy state consistent as we descend
        int mid = (start + end) / 2;
        if (idx <= mid) {
            pointUpdateHelper(2 * node,     start, mid,   idx, delta);
        } else {
            pointUpdateHelper(2 * node + 1, mid + 1, end, idx, delta);
        }
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return number of elements in the original input array */
    public int size() { return n; }

    /**
     * @return cloned snapshot of the internal tree[] array (1-indexed).
     *         tree[i] is the sum of node i's segment, with lazy already folded in.
     *         Used by Visualizer.printTree() and the GUI TreeArea display.
     */
    public long[] getTreeArray() { return tree.clone(); }

    /**
     * @return cloned snapshot of the lazy[] array (1-indexed).
     *         Non-zero entries indicate nodes with PENDING updates not yet pushed
     *         to their children — this is what the GUI Lazy Toggle View shows.
     */
    public long[] getLazyArray() { return lazy.clone(); }

    /**
     * @return cloned copy of the current leaf values (original array with all
     *         pointUpdate and rangeUpdate deltas applied).
     *         Used by the GUI to display per-row ticket counts.
     */
    public long[] getOriginalArray() { return original.clone(); }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validateRange(int l, int r) {
        if (l < 0 || r >= n || l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range [%d, %d] for array of size %d", l, r, n));
        }
    }

    @Override
    public String toString() {
        return String.format("LazySegmentTree { n=%d, root_sum=%d }", n, tree[1]);
    }
}
