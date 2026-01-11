package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Order;
import java.util.PriorityQueue;

/**
 * Monitor managing the order queue using Producer-Consumer pattern.
 * Servers (producers) add orders, Cooks/Chef (consumers) take orders.
 * Uses PriorityQueue for priority-based processing.
 */
public class OrderQueue {
    private final PriorityQueue<Order> queue;
    private int orderIdCounter = 1;

    public OrderQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Producer method: Server adds order to queue.
     * Wakes ONE cook with notify() (not notifyAll()).
     *
     * @param order The order to add
     */
    public synchronized void addOrder(Order order) {
        queue.add(order);
        System.out.println("[OrderQueue] Order #" + order.getOrderId() +
                " added with priority " + order.getPriority() +
                " (" + order.getDish().getName() + ")" +
                " | Queue size: " + queue.size());

        notify(); // Wake ONE cook (not all cooks!)
    }

    /**
     * Consumer method: Cook/Chef takes order from queue.
     * Waits if queue is empty (blocks until notified).
     *
     * @return The highest priority order
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized Order takeOrder() throws InterruptedException {
        while (queue.isEmpty()) {
            System.out.println("[" + Thread.currentThread().getName() +
                    "] Waiting for orders...");
            wait(); // Sleep until a server adds an order
        }

        Order order = queue.poll();
        System.out.println("[" + Thread.currentThread().getName() +
                "] Took Order #" + order.getOrderId() +
                " (" + order.getDish().getName() + ")");

        return order;
    }

    /**
     * Check if queue is empty.
     *
     * @return true if no orders in queue
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Get current queue size.
     *
     * @return number of orders waiting
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Generate unique order ID.
     * Thread-safe increment.
     *
     * @return next order ID
     */
    public synchronized int generateOrderId() {
        return orderIdCounter++;
    }
}