public class SegmentTree {

    private final long[] tree;
    private final int n;
    private final long[] original;

    public SegmentTree(long[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Input array must be non-null and non-empty.");
        }
        n        = arr.length;
        original = arr.clone();
        tree     = new long[4 * n];
        build(1, 0, n - 1);
    }

    private void build(int node, int start, int end) {
        if (start == end) {
            tree[node] = original[start];
            return;
        }
        int mid = (start + end) / 2;
        build(2 * node,     start, mid);
        build(2 * node + 1, mid + 1, end);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    public long rangeQuery(int l, int r) {
        validateRange(l, r);
        return querySumHelper(1, 0, n - 1, l, r);
    }

    private long querySumHelper(int node, int start, int end, int l, int r) {
        if (r < start || end < l)   return 0L;
        if (l <= start && end <= r) return tree[node];
        int mid = (start + end) / 2;
        return querySumHelper(2 * node,     start, mid,   l, r)
             + querySumHelper(2 * node + 1, mid + 1, end, l, r);
    }

    public void pointUpdate(int idx, long newVal) {
        if (idx < 0 || idx >= n) {
            throw new IndexOutOfBoundsException(
                "Index " + idx + " out of bounds for array of size " + n);
        }
        original[idx] = newVal;
        updateHelper(1, 0, n - 1, idx, newVal);
    }

    private void updateHelper(int node, int start, int end, int idx, long val) {
        if (start == end) {
            tree[node] = val;
            return;
        }
        int mid = (start + end) / 2;
        if (idx <= mid) updateHelper(2 * node,     start, mid,   idx, val);
        else            updateHelper(2 * node + 1, mid + 1, end, idx, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    public int    size()             { return n; }
    public long[] getTreeArray()     { return tree.clone(); }
    public long[] getOriginalArray() { return original.clone(); }

    private void validateRange(int l, int r) {
        if (l < 0 || r >= n || l > r)
            throw new IllegalArgumentException(
                String.format("Invalid range [%d, %d] for array of size %d", l, r, n));
    }
}