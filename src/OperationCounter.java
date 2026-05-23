
public class OperationCounter {

    private static long count = 0;

    public static void reset() {
        count = 0;
    }
    public static void increment() {
        count++;
    }

    public static long get() {
        return count;
    }

    public static long getOperations() {
        return count;
    }

    public static void print(String operationLabel) {
        System.out.printf("  [OperationCounter] %-35s → %d node(s) visited%n",
                operationLabel, count);
    }
}