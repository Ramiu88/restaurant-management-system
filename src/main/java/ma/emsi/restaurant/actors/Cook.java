package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.managers.KitchenManager;
import ma.emsi.restaurant.managers.OrderQueue;

public class Cook extends Thread {
    private final OrderQueue orderQueue;
    private final KitchenManager kitchenManager;

    public Cook(String name) {
        super(name);
        this.orderQueue = Restaurant.getInstance().getOrderQueue();
        this.kitchenManager = Restaurant.getInstance().getKitchenManager();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 1. Get Order
                // Order order = orderQueue.takeOrder();
                
                // 2. Prepare (use KitchenManager)
                // kitchenManager.useOven(500);

            } catch (Exception e) { // Change to InterruptedException once implemented
                break;
            }
        }
    }
}
