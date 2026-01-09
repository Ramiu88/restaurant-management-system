package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Order;
import java.util.PriorityQueue;

/**
 * Monitor for Orders.
 * Acts as the bounded buffer between Producers (Servers) and Consumers (Cooks).
 */
public class OrderQueue {
    
    private final PriorityQueue<Order> queue = new PriorityQueue<>();
    private final int MAX_CAPACITY = 20;

    public synchronized void addOrder(Order order) throws InterruptedException {
        while (queue.size() >= MAX_CAPACITY) {
            wait(); // Queue full, server waits
        }
        queue.add(order);
        System.out.println("Order #" + order.getId() + " added to queue. (Priority: " + order.getPriority() + ")");
        notifyAll(); // Wake up waiting cooks
    }

    public synchronized Order takeOrder() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // Queue empty, cook waits
        }
        Order order = queue.poll();
        notifyAll(); // Wake up waiting servers
        return order;
    }
}