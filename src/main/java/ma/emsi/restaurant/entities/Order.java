package ma.emsi.restaurant.entities;

import java.util.List;

/**
 * Entity representing an Order (Commande).
 * Implements Comparable for PriorityQueue usage.
 */
public class Order implements Comparable<Order> {
    private final int id;
    private final List<Dish> dishes;
    private final int priority; // 1 = High/VIP, 2 = Normal, etc.

    public Order(int id, List<Dish> dishes, int priority) {
        this.id = id;
        this.dishes = dishes;
        this.priority = priority;
    }

    public int getId() { return id; }
    public List<Dish> getDishes() { return dishes; }
    public int getPriority() { return priority; }

    @Override
    public int compareTo(Order other) {
        // Lower number = Higher priority
        return Integer.compare(this.priority, other.priority);
    }
}
