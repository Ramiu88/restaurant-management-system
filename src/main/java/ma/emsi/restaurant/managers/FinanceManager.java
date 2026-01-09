package ma.emsi.restaurant.managers;

/**
 * Monitor for Finance.
 * Handles thread-safe revenue calculations.
 */
public class FinanceManager {
    
    private double totalRevenue = 0.0;

    public synchronized void addRevenue(double amount) {
        totalRevenue += amount;
        System.out.println("Payment received: " + amount + " | Total Revenue: " + totalRevenue);
    }

    public synchronized double getTotalRevenue() {
        return totalRevenue;
    }
}
