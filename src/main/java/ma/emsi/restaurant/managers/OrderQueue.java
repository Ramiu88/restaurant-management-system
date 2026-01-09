package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Order;
import java.util.PriorityQueue;

/**
 * Monitor for Orders.
 * Acts as the bounded buffer between Producers (Servers) and Consumers (Cooks).
 */
public class OrderQueue {
    
    private final PriorityQueue<Order> queue = new PriorityQueue<>();

    public synchronized void addOrder(Order order) {
        // TODO: Add to queue and notify() waiting cooks
        queue.add(order);
    }

    public synchronized Order takeOrder() throws InterruptedException {
        // TODO: While queue is empty, wait()
        // Then poll() and return
        return null;
    }
}