public class Benchmark {

    private static final int OPS = 10_000;

    public static void runAll() {

        int[] sizes = {1_000, 10_000, 100_000};

        System.out.printf("%-10s %-12s %-12s %-10s%n",
                "Size", "Naive(ms)", "Lazy(ms)", "Speedup");

        for (int n : sizes) {

            long naiveTime = benchmarkNaive(n);
            long lazyTime  = benchmarkLazy(n);

            double speedup = (double) naiveTime / lazyTime;

            System.out.printf("%-10d %-12d %-12d %-10.2fx%n",
                    n, naiveTime, lazyTime, speedup);
        }
    }

    private static long benchmarkNaive(int n) {

        long[] arr = new long[n];

        long start = System.currentTimeMillis();
           long dummy = 0;

        for (int op = 0; op < OPS; op++) {

            int l = 0, r = n / 2;

            for (int i = l; i <= r; i++) {
                arr[i] += 1;
            }

            long sum = 0;
            for (int i = l; i <= r; i++) {
                sum += arr[i];
            }
            dummy += sum;
        }
        if (dummy == -1) System.out.println();

        return System.currentTimeMillis() - start;
    }

    private static long benchmarkLazy(int n) {

        long[] arr = new long[n];
        LazySegmentTree tree = new LazySegmentTree(arr);

        long start = System.currentTimeMillis();

        for (int op = 0; op < OPS; op++) {

            int l = 0, r = n / 2;

            tree.rangeUpdate(l, r, 1);
            tree.rangeQuery(l, r);
        }

        return System.currentTimeMillis() - start;
    }
}