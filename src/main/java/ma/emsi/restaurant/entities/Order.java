package ma.emsi.restaurant.entities;

/**
 * Entity representing an Order (Commande).
 * Implements Comparable for PriorityQueue usage.
 * Enhanced by Anakin with timestamp, clientId, and single dish per order.
 */
public class Order implements Comparable<Order> {
    private final int orderId;
    private final int clientId;
    private final Dish dish;
    private final int priority; // 1=URGENT, 2=NORMAL, 3=SLOW
    private final long timestamp; // For FIFO within same priority

    public Order(int orderId, int clientId, Dish dish, int priority, long timestamp) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.dish = dish;
        this.priority = priority;
        this.timestamp = timestamp;
    }

    // Getters
    public int getOrderId() {
        return orderId;
    }

    // Backward compatibility - old code uses getId()
    public int getId() {
        return orderId;
    }

    public int getClientId() {
        return clientId;
    }

    public Dish getDish() {
        return dish;
    }

    public int getPriority() {
        return priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Compare orders for priority queue.
     * First by priority (1 < 2 < 3), then by timestamp (FIFO)
     */
    @Override
    public int compareTo(Order other) {
        // First compare by priority (lower number = higher priority)
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        // If same priority, compare by timestamp (earlier = higher priority)
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + orderId +
                ", client=" + clientId +
                ", dish=" + dish.getName() +
                ", priority=" + priority +
                '}';
    }
}
