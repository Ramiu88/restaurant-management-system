package ma.emsi.restaurant;

import ma.emsi.restaurant.managers.*;

/**
 * Simple Console Dashboard for Restaurant Simulation
 * Shows real-time status of all components
 */
public class Dashboard {

    private final Restaurant restaurant;

    public Dashboard(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    /**
     * Display dashboard status
     */
    public void refresh() {
        displayHeader();
        displayTables();
        displayOrders();
        displayKitchen();
        displayStock();
        displayFinance();
        System.out.println("--------------------------------------------------");
    }

    private void displayHeader() {
        System.out.println("\n=== RESTAURANT DASHBOARD ===");
        System.out.println("Time: " + java.time.LocalTime.now() + " | Threads: " + Thread.activeCount());
        System.out.println();
    }

    private void displayTables() {
        System.out.println("--- TABLES ---");
        TableManager tableManager = restaurant.getTableManager();

        int available = tableManager.getAvailableNormalTables();
        int total = Constants.NORMAL_TABLES + Constants.VIP_TABLES;

        System.out.println("Available normal: " + available + "/" + Constants.NORMAL_TABLES);
        System.out.println("Total tables: " + total);
        System.out.println("Occupied: " + (total - available));
        System.out.println();
    }

    private void displayOrders() {
        System.out.println("--- ORDERS ---");
        OrderQueue orderQueue = restaurant.getOrderQueue();

        System.out.println("Queue size: " + orderQueue.size());
        System.out.println("Queue empty: " + orderQueue.isEmpty());
        System.out.println("Next order ID: " + orderQueue.generateOrderId());
        System.out.println();
    }

    private void displayKitchen() {
        System.out.println("--- KITCHEN ---");
        System.out.println("Ovens (3): Available");
        System.out.println("Grills (2): Available");
        System.out.println("Fryer (1): Available");
        System.out.println("Locks: ReentrantLock per resource");
        System.out.println();
    }

    private void displayStock() {
        System.out.println("--- STOCK ---");
        StockManager stock = restaurant.getStockManager();

        System.out.println("Status: " + stock.getStockStatus());

        // Show key ingredients
        String[] ingredients = {"Milk", "Sugar", "Meat", "Dough", "Cheese", "Tomato"};
        for (String ing : ingredients) {
            int level = stock.getIngredientLevel(ing);
            String status = level > 20 ? "OK" : (level > 10 ? "LOW" : "CRITICAL");
            System.out.println(ing + ": " + level + " [" + status + "]");
        }
        System.out.println();
    }

    private void displayFinance() {
        System.out.println("--- FINANCE ---");
        FinanceManager finance = restaurant.getFinanceManager();

        System.out.println("Total Revenue: $" + finance.getTotalRevenue());
        System.out.println("Customers Served: " + finance.getCustomersServed());
        System.out.println("Unsafe Revenue: $" + finance.getUnsafeRevenue());
        System.out.println();
    }

    /**
     * Display one snapshot
     */
    public void snapshot() {
        refresh();
    }
}
