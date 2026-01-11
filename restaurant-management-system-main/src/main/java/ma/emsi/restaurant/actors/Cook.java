package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.Constants;
import ma.emsi.restaurant.entities.Order;
import ma.emsi.restaurant.managers.KitchenManager;
import ma.emsi.restaurant.managers.OrderQueue;
import ma.emsi.restaurant.managers.StockManager;
import java.util.List;
import java.util.Map;

/**
 * Cook thread (Consumer) that takes orders from queue and prepares dishes.
 * Workflow:
 * 1. Take order from OrderQueue (blocks if empty)
 * 2. Check stock availability (Saladin's StockManager)
 * 3. Acquire equipment (Marwan's KitchenManager)
 * 4. Cook dish (sleep for preparationTime)
 * 5. Release equipment
 */
public class Cook implements Runnable {
    private final int cookId;

    public Cook(int cookId) {
        this.cookId = cookId;
    }

    @Override
    public void run() {
        OrderQueue orderQueue = Restaurant.getInstance().getOrderQueue();
        KitchenManager kitchen = Restaurant.getInstance().getKitchenManager();
        StockManager stock = Restaurant.getInstance().getStockManager();

        System.out.println("[Cook-" + cookId + "] Ready to cook");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // STEP 1: Take order from queue (blocks if empty)
                Order order = orderQueue.takeOrder();

                System.out.println("[Cook-" + cookId + "] Processing Order #" +
                        order.getOrderId() + " - " + order.getDish().getName());

                // STEP 2: Check stock (Saladin's module)
                Map<String, Integer> ingredients = order.getDish().getIngredients();
                if (!stock.consumeIngredients(ingredients)) {
                    System.out.println("[Cook-" + cookId +
                            "] Waiting for stock replenishment...");
                    // Stock is low, wait a bit and retry
                    Thread.sleep(1000);
                    // Put order back? Or retry? For now, skip this order
                    continue;
                }

                System.out.println("[Cook-" + cookId + "] Ingredients acquired");

                // STEP 3: Acquire equipment (Marwan's module)
                List<String> equipment = order.getDish().getRequiredEquipment();
                if (!kitchen.acquireEquipment(equipment, Constants.EQUIPMENT_TIMEOUT_SECONDS)) {
                    System.out.println("[Cook-" + cookId +
                            "] Could not acquire equipment, skipping order");
                    // TODO: Should return ingredients to stock
                    continue;
                }

                // STEP 4: Cook the dish
                try {
                    System.out.println("[Cook-" + cookId + "] Cooking " +
                            order.getDish().getName() + "...");
                    Thread.sleep(order.getDish().getPreparationTime());
                    System.out.println("[Cook-" + cookId + "] ✓ Finished Order #" +
                            order.getOrderId());
                } finally {
                    // STEP 5: Always release equipment (even if interrupted)
                    kitchen.releaseEquipment(equipment);
                    System.out.println("[Cook-" + cookId + "] Equipment released");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[Cook-" + cookId + "] Stopped");
                break;
            }
        }
    }
}