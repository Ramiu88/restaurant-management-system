package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.Constants;
import ma.emsi.restaurant.entities.Dish;
import ma.emsi.restaurant.entities.Order;
import ma.emsi.restaurant.managers.OrderQueue;

/**
 * Server thread (Producer) that takes orders and adds them to the queue.
 * In a real system, clients would call the server.
 * For simulation, server generates random orders.
 *
 * Implementation by Anakin, integrated by Bazza.
 */
public class Server implements Runnable {
    private final int serverId;
    private static final Dish[] MENU = {Dish.DESSERT, Dish.STEAK, Dish.PIZZA};

    public Server(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public void run() {
        OrderQueue orderQueue = Restaurant.getInstance().getOrderQueue();

        System.out.println("[Server-" + serverId + "] Started serving");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for a "client" to order (simulated with random delay)
                Thread.sleep(1000 + (int)(Math.random() * 2000)); // 1-3 seconds

                // Get random dish from menu
                Dish dish = getRandomDish();

                // Determine priority based on preparation time
                int priority;
                if (dish.getPreparationTime() <= Constants.PREP_TIME_URGENT) {
                    priority = Constants.PRIORITY_URGENT;
                } else if (dish.getPreparationTime() <= Constants.PREP_TIME_NORMAL) {
                    priority = Constants.PRIORITY_NORMAL;
                } else {
                    priority = Constants.PRIORITY_SLOW;
                }

                // Create order
                Order order = new Order(
                        orderQueue.generateOrderId(),
                        -1, // Client ID not important for now
                        dish,
                        priority,
                        System.currentTimeMillis()
                );

                // Add to queue (Producer action)
                System.out.println("[Server-" + serverId + "] Taking order: " +
                        dish.getName() + " (Priority " + priority + ")");
                orderQueue.addOrder(order);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[Server-" + serverId + "] Stopped");
                break;
            }
        }
    }

    /**
     * Get random dish from menu.
     * In real system, client would choose.
     */
    private Dish getRandomDish() {
        int index = (int)(Math.random() * MENU.length);
        return MENU[index];
    }
}
