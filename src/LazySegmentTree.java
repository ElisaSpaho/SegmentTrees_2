public class LazySegmentTree {

    private final long[] tree;
    private final long[] lazy;
    private final int n;
    private final long[] original;

    public LazySegmentTree(long[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("Input array must be non-null and non-empty.");
        }
        n        = arr.length;
        original = arr.clone();
        tree     = new long[4 * n];  
        lazy     = new long[4 * n]; 

        build(1, 0, n - 1);
    }

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

    private void pushDown(int node, int start, int end) {
        if (lazy[node] == 0) return;   // node is clean — nothing to propagate

        int mid        = (start + end) / 2;
        int leftChild  = 2 * node;
        int rightChild = 2 * node + 1;

        int leftSize          = mid - start + 1;
        tree[leftChild]      += lazy[node] * leftSize;
        lazy[leftChild]      += lazy[node];

        int rightSize         = end - mid;
        tree[rightChild]     += lazy[node] * rightSize;
        lazy[rightChild]     += lazy[node];
        lazy[node] = 0;
    }

    public void rangeUpdate(int l, int r, long val) {
        validateRange(l, r);
        OperationCounter.reset();
        rangeUpdateHelper(1, 0, n - 1, l, r, val);
        for (int i = l; i <= r; i++) {
            original[i] += val;
        }
    }

    private void rangeUpdateHelper(int node, int start, int end, int l, int r, long val) {
        OperationCounter.increment(); 

        if (r < start || end < l) return; 

        if (l <= start && end <= r) {
            tree[node] += val * (end - start + 1);
            lazy[node] += val;
            return;
        }

        pushDown(node, start, end);
        int mid = (start + end) / 2;
        rangeUpdateHelper(2 * node,     start, mid,   l, r, val);
        rangeUpdateHelper(2 * node + 1, mid + 1, end, l, r, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];   // re-merge
    }

    public long rangeQuery(int l, int r) {
        validateRange(l, r);
        OperationCounter.reset();
        return rangeQueryHelper(1, 0, n - 1, l, r);
    }

    private long rangeQueryHelper(int node, int start, int end, int l, int r) {
        OperationCounter.increment();

        if (r < start || end < l) return 0L;         
        if (l <= start && end <= r) return tree[node];

        pushDown(node, start, end);
        int mid = (start + end) / 2;
        long leftSum  = rangeQueryHelper(2 * node,     start, mid,   l, r);
        long rightSum = rangeQueryHelper(2 * node + 1, mid + 1, end, l, r);
        return leftSum + rightSum;
    }

    public void pointUpdate(int idx, long newVal) {
        if (idx < 0 || idx >= n) {
            throw new IndexOutOfBoundsException(
                "Index " + idx + " out of bounds for array of size " + n);
        }
        long delta    = newVal - original[idx];
        original[idx] = newVal; 
        OperationCounter.reset();
        pointUpdateHelper(1, 0, n - 1, idx, delta);
    }

    private void pointUpdateHelper(int node, int start, int end, int idx, long delta) {
        OperationCounter.increment();
        if (start == end) {
            tree[node] += delta;
            return;
        }
        pushDown(node, start, end);
        int mid = (start + end) / 2;
        if (idx <= mid) {
            pointUpdateHelper(2 * node,     start, mid,   idx, delta);
        } else {
            pointUpdateHelper(2 * node + 1, mid + 1, end, idx, delta);
        }
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    public int size() { return n; }

    public long[] getTreeArray() { return tree.clone(); }

    public long[] getLazyArray() { return lazy.clone(); }

    public long[] getOriginalArray() { return original.clone(); }

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
