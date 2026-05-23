/**
 * LazySegmentTreeTest
 * ─────────────────────────────────────────────────────────────────────────────
 * Lightweight correctness tests for LazySegmentTree.
 *
 * Called from Main.main() after the demo so the console output always includes
 * a green/red pass/fail summary.  Kept intentionally simple — no JUnit
 * dependency, no reflection, no test frameworks.  University-exam-friendly.
 *
 * TESTS COVERED:
 *   1. Build correctness        — initial sums match expected values
 *   2. Range query              — query sub-ranges before any updates
 *   3. Range update + query     — lazy propagation applied correctly
 *   4. Boundary elements        — elements outside the update range unchanged
 *   5. Point update             — absolute value assignment
 *   6. Nested range updates     — overlapping lazy tags accumulated correctly
 *   7. Single-element range     — edge case: l == r
 */
public class LazySegmentTreeTest {

    // Track pass / fail counts across all assertions
    private static int passed = 0;
    private static int failed = 0;

    // ── Entry Point ──────────────────────────────────────────────────────────

    /**
     * Run all tests and print a summary to stdout.
     * Called by Main.main() — do not rename this method.
     */
    public static void runTests() {
        passed = 0;
        failed = 0;

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         CORRECTNESS TESTS — LazySegmentTree              ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        // ── Setup ────────────────────────────────────────────────────────────
        long[] arr = {2, 4, 3, 1, 6, 7, 2, 5};   // sum = 30
        LazySegmentTree lst = new LazySegmentTree(arr);

        // ── Test 1: Build correctness ─────────────────────────────────────
        assertEq("Build: full range query [0..7]",   30L, lst.rangeQuery(0, 7));
        assertEq("Build: partial range [2..5]",      17L, lst.rangeQuery(2, 5));
        assertEq("Build: single element [0..0]",      2L, lst.rangeQuery(0, 0));
        assertEq("Build: last element [7..7]",         5L, lst.rangeQuery(7, 7));

        // ── Test 2: Range update — lazy propagation ───────────────────────
        // Add +5 to [1..5]: elements become {2, 9, 8, 6, 11, 12, 2, 5}
        // Sum [1..5] was 4+3+1+6+7=21, now 21 + 5*5 = 46... wait
        // Actually original [1..5] = 4,3,1,6,7 → sum = 21; +5*5 = 46
        lst.rangeUpdate(1, 5, 5);
        assertEq("rangeUpdate [1..5] +5, then query [1..5]", 46L, lst.rangeQuery(1, 5));

        // ── Test 3: Boundary elements unchanged ───────────────────────────
        assertEq("Element [0..0] unchanged after rangeUpdate [1..5]", 2L, lst.rangeQuery(0, 0));
        assertEq("Element [6..6] unchanged after rangeUpdate [1..5]", 2L, lst.rangeQuery(6, 6));
        assertEq("Element [7..7] unchanged after rangeUpdate [1..5]", 5L, lst.rangeQuery(7, 7));

        // ── Test 4: Full range sum after partial update ───────────────────
        // Full sum was 30; +5 applied to 5 elements → 30 + 25 = 55
        assertEq("Full range sum after rangeUpdate [1..5] +5",        55L, lst.rangeQuery(0, 7));

        // ── Test 5: Point update ──────────────────────────────────────────
        // arr[3] is currently 1+5=6. Set it to 100.
        // Sum [2..4] was (3+5)+(1+5)+(6+5)=8+6+11=25. After pointUpdate(3,100): 8+100+11=119
        lst.pointUpdate(3, 100);
        assertEq("pointUpdate(3, 100): query [2..4]", 119L, lst.rangeQuery(2, 4));
        // Confirm element 3 is exactly 100
        assertEq("pointUpdate(3, 100): query [3..3]", 100L, lst.rangeQuery(3, 3));

        // ── Test 6: Nested range updates ──────────────────────────────────
        LazySegmentTree lst2 = new LazySegmentTree(new long[]{1, 1, 1, 1, 1});
        lst2.rangeUpdate(0, 4, 2);   // all become 3
        lst2.rangeUpdate(1, 3, 3);   // indices 1-3 become 6
        // Expected: [3, 6, 6, 6, 3] → sum = 24
        assertEq("Nested updates: full range sum", 24L, lst2.rangeQuery(0, 4));
        assertEq("Nested updates: outer element [0]", 3L, lst2.rangeQuery(0, 0));
        assertEq("Nested updates: inner element [2]", 6L, lst2.rangeQuery(2, 2));

        // ── Test 7: Single-element range update ───────────────────────────
        LazySegmentTree lst3 = new LazySegmentTree(new long[]{10, 20, 30});
        lst3.rangeUpdate(1, 1, 5);   // only index 1: 20 → 25
        assertEq("Single-element rangeUpdate [1..1] +5", 25L, lst3.rangeQuery(1, 1));
        assertEq("Neighbours unchanged: [0..0]",         10L, lst3.rangeQuery(0, 0));
        assertEq("Neighbours unchanged: [2..2]",         30L, lst3.rangeQuery(2, 2));

        // ── Summary ───────────────────────────────────────────────────────
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Results: %2d passed,  %2d failed" +
                "                            ║%n", passed, failed);
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }

    // ── Assertion Helper ─────────────────────────────────────────────────────

    /**
     * Compare expected and actual values, print a PASS or FAIL line, and
     * update the running counters.
     *
     * @param label    short description of what is being tested
     * @param expected expected value
     * @param actual   actual value returned by the tree
     */
    private static void assertEq(String label, long expected, long actual) {
        if (expected == actual) {
            System.out.printf("║  ✓ PASS  %-52s║%n", label);
            passed++;
        } else {
            System.out.printf("║  ✗ FAIL  %-40s  exp=%-5d got=%-5d║%n",
                    label, expected, actual);
            failed++;
        }
    }
}