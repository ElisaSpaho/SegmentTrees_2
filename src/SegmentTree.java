/**
 * SegmentTree
 * ─────────────────────────────────────────────────────────────────────────────
 * A standard (non-lazy) Segment Tree that answers Range Sum and Range Minimum
 * queries in O(log n) and supports Point Updates in O(log n).
 *
 * WHAT IS A SEGMENT TREE?
 *   A Segment Tree is a binary tree where:
 *     • Each LEAF stores one element of the original array.
 *     • Each INTERNAL NODE stores an aggregate (sum, min, max …) of the
 *       elements covered by its subtree.
 *   This lets us answer range queries by combining at most O(log n) nodes
 *   instead of scanning O(n) elements.
 *
 * INTERNAL STORAGE (1-indexed flat array):
 *   Node 1      = root (covers entire array)
 *   Node 2k     = left  child of node k  (covers left half of k's range)
 *   Node 2k+1   = right child of node k  (covers right half of k's range)
 *   Leaf nodes  = nodes where start == end
 *
 * SUPPORTED OPERATIONS:
 *   • rangeQuery(l, r)     — sum  of elements in [l..r]  O(log n)
 *   • queryMin(l, r)       — min  of elements in [l..r]  O(log n)
 *   • pointUpdate(idx, v)  — set  arr[idx] = v           O(log n)
 *   • update(idx, v)       — alias for pointUpdate (backward compat.)
 *   • querySum(l, r)       — alias for rangeQuery  (backward compat.)
 *
 * NOTE ON LAZY PROPAGATION:
 *   This class does NOT support range updates efficiently. For range updates
 *   use LazySegmentTree, which defers propagation and achieves O(log n) per
 *   range update rather than O(n).
 */
public class SegmentTree {

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Sum tree stored as a 1-indexed flat array. tree[i] = sum of node i's segment. */
    private final long[] tree;

    /** Minimum tree. minTree[i] = minimum value in node i's segment. */
    private final long[] minTree;

    /** Number of elements in the original input array. */
    private final int n;

    /**
     * Working copy of the original values.
     * Updated by pointUpdate so getOriginalArray() always reflects current state.
     */
    private final long[] original;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Build a Segment Tree from {@code arr} in O(n) time.
     *
     * COMPLEXITY:
     *   Time  O(n) — every node is initialised exactly once during build().
     *   Space O(n) — the flat array uses at most 4n entries (safe upper bound).
     *
     * @param arr non-null, non-empty array of long values
     * @throws IllegalArgumentException if arr is null or empty
     */
    public SegmentTree(long[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Input array must be non-null and non-empty.");
        }
        n        = arr.length;
        original = arr.clone();   // keep a pristine working copy
        tree     = new long[4 * n];
        minTree  = new long[4 * n];

        build(1, 0, n - 1);      // start recursive build at root (node 1)
    }

    // ── Build — O(n) ─────────────────────────────────────────────────────────

    /**
     * Recursively initialise every node of both the sum tree and the min tree.
     *
     * BASE CASE  (start == end):  leaf node — store original[start] directly.
     * RECURSIVE STEP:
     *   1. Split segment at mid = (start + end) / 2.
     *   2. Build left  child node (2*node) covering [start .. mid].
     *   3. Build right child node (2*node+1) covering [mid+1 .. end].
     *   4. Merge: parent.sum = left.sum + right.sum
     *             parent.min = min(left.min, right.min)
     *
     * @param node  current node index (1-indexed)
     * @param start left boundary of this node's segment (0-indexed)
     * @param end   right boundary of this node's segment (0-indexed, inclusive)
     */
    private void build(int node, int start, int end) {
        if (start == end) {
            // Leaf: both trees store the single element value
            tree[node]    = original[start];
            minTree[node] = original[start];
            return;
        }
        int mid = (start + end) / 2;
        build(2 * node,     start, mid);       // build left subtree
        build(2 * node + 1, mid + 1, end);     // build right subtree

        // Merge children into this internal node
        tree[node]    = tree[2 * node]    + tree[2 * node + 1];
        minTree[node] = Math.min(minTree[2 * node], minTree[2 * node + 1]);
    }

    // ── Range Sum Query — O(log n) ────────────────────────────────────────────

    /**
     * Return the sum of all elements in the range [l..r].
     *
     * Public entry point — validates the range then delegates to the
     * recursive helper.
     *
     * @param l left  boundary (0-indexed, inclusive)
     * @param r right boundary (0-indexed, inclusive)
     * @return  sum of arr[l] + arr[l+1] + … + arr[r]
     * @throws IllegalArgumentException for invalid ranges
     */
    public long rangeQuery(int l, int r) {
        validateRange(l, r);
        return querySumHelper(1, 0, n - 1, l, r);
    }

    /**
     * Backward-compatible alias — identical to rangeQuery().
     * Kept so Main.java console demo does not need changing.
     */
    public long querySum(int l, int r) {
        return rangeQuery(l, r);
    }

    /**
     * Recursive range sum helper.
     *
     * THREE CASES at each node:
     *   NO OVERLAP    — node's segment [start..end] is entirely outside [l..r].
     *                   Return 0 (the additive identity).
     *   TOTAL OVERLAP — node's segment is entirely inside [l..r].
     *                   Return the stored sum directly (this is the O(log n) win).
     *   PARTIAL OVERLAP — split and recurse on both children, then add results.
     */
    private long querySumHelper(int node, int start, int end, int l, int r) {
        if (r < start || end < l)          return 0L;           // no overlap
        if (l <= start && end <= r)        return tree[node];   // total overlap

        int mid = (start + end) / 2;                            // partial overlap
        long leftSum  = querySumHelper(2 * node,     start, mid,   l, r);
        long rightSum = querySumHelper(2 * node + 1, mid + 1, end, l, r);
        return leftSum + rightSum;
    }

    // ── Range Minimum Query — O(log n) ───────────────────────────────────────

    /**
     * Return the minimum value in the range [l..r].
     *
     * @param l left  boundary (0-indexed, inclusive)
     * @param r right boundary (0-indexed, inclusive)
     * @return  minimum of arr[l..r]
     */
    public long queryMin(int l, int r) {
        validateRange(l, r);
        return queryMinHelper(1, 0, n - 1, l, r);
    }

    /**
     * Recursive range minimum helper.
     * Mirrors querySumHelper but uses Long.MAX_VALUE as the no-overlap identity.
     */
    private long queryMinHelper(int node, int start, int end, int l, int r) {
        if (r < start || end < l)    return Long.MAX_VALUE;      // no overlap
        if (l <= start && end <= r)  return minTree[node];       // total overlap

        int mid = (start + end) / 2;
        long leftMin  = queryMinHelper(2 * node,     start, mid,   l, r);
        long rightMin = queryMinHelper(2 * node + 1, mid + 1, end, l, r);
        return Math.min(leftMin, rightMin);
    }

    // ── Point Update — O(log n) ──────────────────────────────────────────────

    /**
     * Set the element at position {@code idx} to {@code newVal}, then propagate
     * the change upward through all ancestor nodes.
     *
     * HOW IT WORKS:
     *   1. Descend to the leaf at position idx.
     *   2. Set the leaf value.
     *   3. On the way back up (via the call stack), re-merge each ancestor:
     *        sum[parent] = sum[left] + sum[right]
     *        min[parent] = min(min[left], min[right])
     *
     * @param idx    position to update (0-indexed)
     * @param newVal new value to store at arr[idx]
     * @throws IndexOutOfBoundsException for invalid index
     */
    public void pointUpdate(int idx, long newVal) {
        if (idx < 0 || idx >= n) {
            throw new IndexOutOfBoundsException(
                "Index " + idx + " out of bounds for array of size " + n);
        }
        original[idx] = newVal;
        updateHelper(1, 0, n - 1, idx, newVal);
    }

    /**
     * Backward-compatible alias — identical to pointUpdate().
     * Kept so Main.java console demo does not need changing.
     */
    public void update(int idx, long newVal) {
        pointUpdate(idx, newVal);
    }

    /**
     * Recursive point update helper.
     * Navigates to the target leaf then re-merges ancestors on the way back up.
     */
    private void updateHelper(int node, int start, int end, int idx, long val) {
        if (start == end) {
            tree[node]    = val;
            minTree[node] = val;
            return;
        }
        int mid = (start + end) / 2;
        if (idx <= mid) {
            updateHelper(2 * node,     start, mid,   idx, val);
        } else {
            updateHelper(2 * node + 1, mid + 1, end, idx, val);
        }
        // Re-merge this internal node after child was updated
        tree[node]    = tree[2 * node]    + tree[2 * node + 1];
        minTree[node] = Math.min(minTree[2 * node], minTree[2 * node + 1]);
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return number of elements in the original array */
    public int size() { return n; }

    /**
     * @return a cloned snapshot of the internal sum-tree array (1-indexed).
     *         Useful for Visualizer.printTree() and the GUI TextArea.
     */
    public long[] getTreeArray() { return tree.clone(); }

    /**
     * @return a cloned snapshot of the internal min-tree array (1-indexed).
     */
    public long[] getMinTreeArray() { return minTree.clone(); }

    /**
     * @return the current working array values (original with updates applied).
     *         Cloned so callers cannot corrupt internal state.
     */
    public long[] getOriginalArray() { return original.clone(); }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validateRange(int l, int r) {
        if (l < 0 || r >= n || l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range [%d, %d] for array of size %d", l, r, n));
        }
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("SegmentTree { n=%d, root_sum=%d, root_min=%d }",
                n, tree[1], minTree[1]);
    }
}