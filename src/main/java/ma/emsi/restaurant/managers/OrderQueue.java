package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Order;
import java.util.PriorityQueue;

/**
 * Monitor managing the order queue using Producer-Consumer pattern.
 * Servers (producers) add orders, Cooks/Chef (consumers) take orders.
 * Uses PriorityQueue for priority-based processing.
 *
 * Enhanced by Anakin with utility methods and better logging.
 * Bounded buffer by Saladin to prevent memory issues.
 */
public class OrderQueue {
    private final PriorityQueue<Order> queue;
    private final int MAX_CAPACITY;
    private int orderIdCounter = 1;

    public OrderQueue() {
        this.queue = new PriorityQueue<>();
        this.MAX_CAPACITY = 20; // Bounded buffer
    }

    public OrderQueue(int maxCapacity) {
        this.queue = new PriorityQueue<>();
        this.MAX_CAPACITY = maxCapacity;
    }

    /**
     * Producer method: Server adds order to queue.
     * Blocks if queue is full (bounded buffer pattern).
     * Wakes ONE cook with notify() (more efficient than notifyAll).
     *
     * @param order The order to add
     * @throws InterruptedException if interrupted while waiting for space
     */
    public synchronized void addOrder(Order order) throws InterruptedException {
        while (queue.size() >= MAX_CAPACITY) {
            wait(); // Queue full, server waits
        }
        queue.add(order);
        System.out.println("[OrderQueue] Order #" + order.getOrderId() +
                " added with priority " + order.getPriority() +
                " (" + order.getDish().getName() + ")" +
                " | Queue size: " + queue.size());

        notify(); // Wake ONE cook (not all cooks!)
    }

    /**
     * Consumer method: Cook/Chef takes order from queue.
     * Blocks if queue is empty.
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

        notifyAll(); // Wake waiting servers (queue no longer full)
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
