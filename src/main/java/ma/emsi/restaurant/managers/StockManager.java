package ma.emsi.restaurant.managers;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitor for Inventory.
 * Handles stock levels and re-supply triggers.
 */
public class StockManager {
    
    private final Map<String, Integer> ingredients = new HashMap<>();

    public StockManager() {
        ingredients.put("Cheese", 20);
        ingredients.put("Tomato", 20);
        ingredients.put("Dough", 20);
    }

    public synchronized void consumeIngredient(String ingredient) throws InterruptedException {
        while (ingredients.getOrDefault(ingredient, 0) <= 0) {
            System.out.println("Stock EMPTY for " + ingredient + ". Cook waiting for resupply...");
            wait();
        }
        ingredients.put(ingredient, ingredients.get(ingredient) - 1);
    }

    public synchronized void resupply(String ingredient, int amount) {
        ingredients.put(ingredient, ingredients.getOrDefault(ingredient, 0) + amount);
        System.out.println("Resupplied " + ingredient + ". New stock: " + ingredients.get(ingredient));
        notifyAll();
    }
}
