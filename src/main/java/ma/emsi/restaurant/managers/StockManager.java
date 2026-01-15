package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitor for Inventory Management.
 * Handles stock levels with automatic replenishment via background thread.
 * Demonstrates wait/notify pattern with dedicated background thread.
 *
 * @author Saladin
 */
public class StockManager {

    private final Map<String, Integer> stock;
    private final int lowThreshold;
    private final int replenishAmount;

    public StockManager() {
        this.stock = new HashMap<>();
        this.lowThreshold = Constants.STOCK_LOW_THRESHOLD;
        this.replenishAmount = Constants.STOCK_REPLENISH_AMOUNT;

        // Initialize stock with all ingredients
        stock.put("Tomato", Constants.STOCK_INITIAL);
        stock.put("Cheese", Constants.STOCK_INITIAL);
        stock.put("Meat", Constants.STOCK_INITIAL);
        stock.put("Dough", Constants.STOCK_INITIAL);
        stock.put("Milk", Constants.STOCK_INITIAL);
        stock.put("Sugar", Constants.STOCK_INITIAL);

        System.out.println("[Stock] Initialized with " + Constants.STOCK_INITIAL + " units per ingredient");
    }

    /**
     * Cook tries to consume ingredients for a dish.
     * Returns false if not enough stock (Cook should wait and retry).
     *
     * Integration point: Called by Cook (Cranky's module)
     */
    public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
        // Handle null or empty input gracefully
        if (needed == null || needed.isEmpty()) {
            return true; // Nothing to consume is a success
        }

        // First check if we have enough of everything
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            String ingredient = entry.getKey();
            int amount = entry.getValue();

            int available = stock.getOrDefault(ingredient, 0);
            if (available < amount) {
                System.out.println("[Stock] Not enough " + ingredient +
                                 " (need " + amount + ", have " + available + ")");
                notify(); // Wake stock manager thread
                return false;
            }
        }

        // We have enough, consume all ingredients
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            String ingredient = entry.getKey();
            int amount = entry.getValue();
            int newAmount = stock.get(ingredient) - amount;
            stock.put(ingredient, newAmount);

            System.out.println("[Stock] Consumed " + amount + " " + ingredient +
                             " (remaining: " + newAmount + ")");
        }

        // Check if stock is now low and signal background thread
        if (isStockLow()) {
            System.out.println("[Stock] WARNING: Stock is running low!");
            notify(); // Wake stock manager to replenish
        }

        return true;
    }

    /**
     * Replenish all ingredients.
     * Called by StockManagerThread after delivery simulation.
     */
    public synchronized void replenish() {
        System.out.println("[Stock] ===== REPLENISHMENT STARTING =====");

        for (String ingredient : stock.keySet()) {
            int oldAmount = stock.get(ingredient);
            int newAmount = oldAmount + replenishAmount;
            stock.put(ingredient, newAmount);

            System.out.println("[Stock] Replenished " + ingredient +
                             " (" + oldAmount + " -> " + newAmount + ")");
        }

        System.out.println("[Stock] ===== REPLENISHMENT COMPLETE =====");
        notifyAll(); // Wake all waiting cooks
    }

    /**
     * Check if any ingredient is below threshold.
     * Used by StockManagerThread to decide when to replenish.
     */
    public synchronized boolean isStockLow() {
        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            if (entry.getValue() < lowThreshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get current stock levels (for dashboard/monitoring).
     * Returns a copy to avoid external modification.
     */
    public synchronized Map<String, Integer> getStockLevels() {
        return new HashMap<>(stock);
    }

    /**
     * Get detailed stock status
     */
    public synchronized String getStockStatus() {
        StringBuilder sb = new StringBuilder("[Stock Status]\n");
        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            sb.append(String.format("  %s: %d%s\n",
                entry.getKey(),
                entry.getValue(),
                entry.getValue() < lowThreshold ? " (LOW!)" : ""));
        }
        return sb.toString();
    }

    /**
     * Get specific ingredient level
     */
    public synchronized int getIngredientLevel(String ingredient) {
        return stock.getOrDefault(ingredient, 0);
    }
}
