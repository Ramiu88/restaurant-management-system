package ma.emsi.restaurant.managers;

/**
 * Monitor for Finance Management.
 * Demonstrates race condition and its solution with synchronized methods.
 *
 * @author Saladin
 */
public class FinanceManager {

    private double totalRevenue = 0.0;
    private int customersServed = 0;

    // For demonstration: UNSAFE version to show race condition
    private double unsafeRevenue = 0.0;

    /**
     * CORRECT: Synchronized method prevents race condition.
     * Multiple cashiers can call this simultaneously without losing money.
     */
    public synchronized void processPayment(double amount) {
        // Read-Modify-Write is atomic because of synchronized
        totalRevenue += amount;
        customersServed++;

        System.out.println("[Finance] Payment processed: $" + String.format("%.2f", amount) +
                          " | Total: $" + String.format("%.2f", totalRevenue) +
                          " | Customers: " + customersServed);
    }

    /**
     * WRONG: Demonstrates race condition - DO NOT USE IN PRODUCTION.
     * This is only for educational purposes to show what happens without synchronization.
     */
    public void processPaymentUNSAFE(double amount) {
        // Race condition: Multiple threads can interleave here
        // Thread1 reads 100, Thread2 reads 100
        // Thread1 writes 115, Thread2 writes 120
        // Result: 120 (should be 135!) - Money lost!
        unsafeRevenue += amount;

        System.out.println("[Finance UNSAFE] Payment: $" + String.format("%.2f", amount) +
                          " | Total: $" + String.format("%.2f", unsafeRevenue));
    }

    /**
     * Get total revenue (thread-safe)
     */
    public synchronized double getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Get unsafe revenue for comparison
     */
    public synchronized double getUnsafeRevenue() {
        return unsafeRevenue;
    }

    /**
     * Get number of customers served
     */
    public synchronized int getCustomersServed() {
        return customersServed;
    }

    /**
     * Demonstrates the race condition problem.
     * Run this to see how money is lost without synchronization.
     */
    public void demonstrateRaceCondition() {
        System.out.println("\n=== RACE CONDITION DEMONSTRATION ===");
        System.out.println("Running 10 cashiers processing 100 payments each ($10 each)");
        System.out.println("Expected total: $" + (10 * 100 * 10.0));

        // Reset counters
        unsafeRevenue = 0;

        // Create 10 cashiers using UNSAFE method
        Thread[] cashiers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int id = i;
            cashiers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    processPaymentUNSAFE(10.0);
                }
            }, "UnsafeCashier-" + id);
            cashiers[i].start();
        }

        // Wait for all to finish
        for (Thread cashier : cashiers) {
            try {
                cashier.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        double expected = 10 * 100 * 10.0;
        System.out.println("\n=== RESULTS ===");
        System.out.println("Expected revenue: $" + expected);
        System.out.println("Actual (UNSAFE):  $" + String.format("%.2f", unsafeRevenue));
        System.out.println("Money LOST:       $" + String.format("%.2f", (expected - unsafeRevenue)));
        System.out.println("\nThis is why we need synchronized methods!");
        System.out.println("===================================\n");
    }

    /**
     * Get statistics summary
     */
    public synchronized String getStatistics() {
        return String.format("Revenue: $%.2f | Customers: %d | Avg: $%.2f",
            totalRevenue, customersServed,
            customersServed > 0 ? totalRevenue / customersServed : 0.0);
    }
}
