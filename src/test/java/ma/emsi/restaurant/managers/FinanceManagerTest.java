package ma.emsi.restaurant.managers;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * JUnit tests for FinanceManager
 * Tests both single-threaded and multi-threaded scenarios
 *
 * @author Saladin
 */
public class FinanceManagerTest {

    private FinanceManager finance;

    @Before
    public void setUp() {
        finance = new FinanceManager();
    }

    @After
    public void tearDown() {
        finance = null;
    }

    // ===== BASIC FUNCTIONALITY TESTS =====

    @Test
    public void testInitialState() {
        assertEquals("Initial revenue should be 0",
                    0.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Initial customers should be 0",
                    0, finance.getCustomersServed());
    }

    @Test
    public void testProcessPayment_AddsToRevenue() {
        finance.processPayment(25.50);
        assertEquals("Revenue should be 25.50",
                    25.50, finance.getTotalRevenue(), 0.01);
    }

    @Test
    public void testProcessPayment_IncrementsCustomerCount() {
        finance.processPayment(10.0);
        assertEquals("Customer count should be 1",
                    1, finance.getCustomersServed());
    }

    @Test
    public void testMultiplePayments_AccumulatesRevenue() {
        finance.processPayment(10.0);
        finance.processPayment(20.0);
        finance.processPayment(30.0);

        assertEquals("Total revenue should be 60",
                    60.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Customer count should be 3",
                    3, finance.getCustomersServed());
    }

    @Test
    public void testProcessPayment_WithZero() {
        finance.processPayment(0.0);
        assertEquals("Revenue should be 0",
                    0.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Customer count should be 1",
                    1, finance.getCustomersServed());
    }

    @Test
    public void testProcessPayment_WithLargeAmount() {
        finance.processPayment(9999.99);
        assertEquals("Should handle large amounts",
                    9999.99, finance.getTotalRevenue(), 0.01);
    }

    @Test
    public void testProcessPayment_WithSmallAmount() {
        finance.processPayment(0.01);
        assertEquals("Should handle small amounts",
                    0.01, finance.getTotalRevenue(), 0.01);
    }

    @Test
    public void testGetStatistics_Format() {
        finance.processPayment(25.50);
        finance.processPayment(34.50);

        String stats = finance.getStatistics();
        assertNotNull("Statistics should not be null", stats);
        assertTrue("Should contain 'Revenue'", stats.contains("Revenue"));
        assertTrue("Should contain 'Customers'", stats.contains("Customers"));
        assertTrue("Should contain 'Avg'", stats.contains("Avg"));
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test(timeout = 5000)
    public void testConcurrentPayments_TwoThreads() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                finance.processPayment(10.0);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                finance.processPayment(10.0);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // 2 threads × 100 payments × $10 = $2000
        assertEquals("Should not lose money with 2 threads",
                    2000.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Should count all customers",
                    200, finance.getCustomersServed());
    }

    @Test(timeout = 10000)
    public void testConcurrentPayments_TenThreads() throws InterruptedException {
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    finance.processPayment(10.0);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // 10 threads × 100 payments × $10 = $10,000
        assertEquals("Should not lose money with 10 threads",
                    10000.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Should count all 1000 customers",
                    1000, finance.getCustomersServed());
    }

    @Test(timeout = 10000)
    public void testConcurrentPayments_VariableAmounts() throws InterruptedException {
        Thread[] threads = new Thread[5];

        // Each thread pays different amounts
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    finance.processPayment((threadNum + 1) * 10.0);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Thread 0: 50 × $10 = $500
        // Thread 1: 50 × $20 = $1000
        // Thread 2: 50 × $30 = $1500
        // Thread 3: 50 × $40 = $2000
        // Thread 4: 50 × $50 = $2500
        // Total: $7500
        assertEquals("Should calculate correct total with variable amounts",
                    7500.0, finance.getTotalRevenue(), 0.01);
        assertEquals("Should count all 250 customers",
                    250, finance.getCustomersServed());
    }

    @Test(timeout = 5000)
    public void testGetRevenue_WhileProcessing() throws InterruptedException {
        // Thread that continuously processes payments
        Thread processor = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                finance.processPayment(10.0);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        processor.start();

        // Thread that continuously reads revenue
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                double revenue = finance.getTotalRevenue();
                assertTrue("Revenue should be non-negative", revenue >= 0);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        reader.start();

        processor.join();
        reader.join();

        // Just verify no exceptions occurred and final state is correct
        assertTrue("Final revenue should be positive",
                  finance.getTotalRevenue() > 0);
    }

    // ===== RACE CONDITION DEMONSTRATION TEST =====

    @Test(timeout = 15000)
    public void testDemonstrateRaceCondition_ShowsMoneyLoss() {
        // This test verifies that the demonstration method works
        // It should show that the unsafe version loses money

        finance.demonstrateRaceCondition();

        // The unsafe revenue should be less than 10,000
        // because of race conditions
        double unsafeRevenue = finance.getUnsafeRevenue();

        assertTrue("Unsafe version should lose money (be less than 10000)",
                  unsafeRevenue < 10000.0);
        assertTrue("Unsafe version should still have some money",
                  unsafeRevenue > 0);

        System.out.println("Race condition demo showed loss of: $" +
                          (10000.0 - unsafeRevenue));
    }

    // ===== STRESS TESTS =====

    @Test(timeout = 30000)
    public void stressTest_ManyThreadsManyPayments() throws InterruptedException {
        final int NUM_THREADS = 20;
        final int PAYMENTS_PER_THREAD = 500;
        final double PAYMENT_AMOUNT = 5.0;

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < PAYMENTS_PER_THREAD; j++) {
                    finance.processPayment(PAYMENT_AMOUNT);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        double expected = NUM_THREADS * PAYMENTS_PER_THREAD * PAYMENT_AMOUNT;
        assertEquals("Stress test: Should not lose money",
                    expected, finance.getTotalRevenue(), 0.01);
        assertEquals("Stress test: Should count all customers",
                    NUM_THREADS * PAYMENTS_PER_THREAD,
                    finance.getCustomersServed());
    }
}
