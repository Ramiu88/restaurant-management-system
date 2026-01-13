package ma.emsi.restaurant.entities;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * JUnit tests for Order entity
 * Tests FIFO ordering by timestamp and priority-based comparison
 */
public class OrderTest {

    private Dish pizza;
    private Dish steak;
    private Dish dessert;

    @Before
    public void setUp() {
        pizza = Dish.PIZZA;
        steak = Dish.STEAK;
        dessert = Dish.DESSERT;
    }

    // ===== CONSTRUCTOR TESTS =====

    @Test
    public void testConstructor() {
        long timestamp = System.currentTimeMillis();
        Order order = new Order(1, 100, pizza, 2, timestamp);

        assertEquals("Order ID should match", 1, order.getOrderId());
        assertEquals("Client ID should match", 100, order.getClientId());
        assertEquals("Dish should match", pizza, order.getDish());
        assertEquals("Priority should match", 2, order.getPriority());
        assertEquals("Timestamp should match", timestamp, order.getTimestamp());
    }

    @Test
    public void testGetId_BackwardCompatibility() {
        Order order = new Order(42, 100, pizza, 2, System.currentTimeMillis());

        assertEquals("getId() should return orderId for backward compatibility", 42, order.getId());
    }

    // ===== PRIORITY COMPARISON TESTS =====

    @Test
    public void testCompareTo_PriorityOrdering() {
        long now = System.currentTimeMillis();

        Order urgent = new Order(1, 1, dessert, 1, now);      // Priority 1
        Order normal = new Order(2, 1, steak, 2, now);         // Priority 2
        Order slow = new Order(3, 1, pizza, 3, now);          // Priority 3

        // Lower priority number = higher priority
        assertTrue("Urgent should come before Normal", urgent.compareTo(normal) < 0);
        assertTrue("Normal should come before Slow", normal.compareTo(slow) < 0);
        assertTrue("Urgent should come before Slow", urgent.compareTo(slow) < 0);
    }

    @Test
    public void testCompareTo_SamePriority_FIFOByTimestamp() {
        long now = System.currentTimeMillis();

        Order first = new Order(1, 1, steak, 2, now);
        Order second = new Order(2, 1, steak, 2, now + 100);
        Order third = new Order(3, 1, steak, 2, now + 200);

        // Same priority: earlier timestamp comes first
        assertTrue("First should come before Second", first.compareTo(second) < 0);
        assertTrue("Second should come before Third", second.compareTo(third) < 0);
        assertTrue("First should come before Third", first.compareTo(third) < 0);
    }

    @Test
    public void testCompareTo_EqualOrders() {
        long now = System.currentTimeMillis();

        Order order1 = new Order(1, 1, steak, 2, now);
        Order order2 = new Order(2, 1, steak, 2, now);

        assertEquals("Equal orders should have compareTo result of 0", 0, order1.compareTo(order2));
    }

    @Test
    public void testCompareTo_DifferentDishesSamePriority() {
        long now = System.currentTimeMillis();

        Order pizzaOrder = new Order(1, 1, pizza, 2, now);
        Order steakOrder = new Order(2, 1, steak, 2, now);

        assertEquals("Same priority and timestamp should be equal", 0, pizzaOrder.compareTo(steakOrder));
    }

    // ===== PRIORITY THEN TIMESTAMP TESTS =====

    @Test
    public void testCompareTo_PriorityTakesPrecedenceOverTimestamp() {
        long earlier = System.currentTimeMillis();
        long later = earlier + 10000;

        // Low priority (3) but earlier timestamp
        Order slowEarly = new Order(1, 1, pizza, 3, earlier);

        // High priority (1) but later timestamp
        // High priority should still come first despite later timestamp
        Order urgentLate = new Order(2, 1, dessert, 1, later);

        assertTrue("High priority with late timestamp should beat low priority with early timestamp",
                   urgentLate.compareTo(slowEarly) < 0);
    }

    // ===== TO STRING TEST =====

    @Test
    public void testToString() {
        Order order = new Order(42, 100, pizza, 2, System.currentTimeMillis());
        String str = order.toString();

        assertTrue("toString should contain order ID", str.contains("42"));
        assertTrue("toString should contain client ID", str.contains("100"));
        assertTrue("toString should contain dish name", str.contains("Pizza"));
        assertTrue("toString should contain priority", str.contains("2"));
    }

    // ===== GETTER TESTS =====

    @Test
    public void testGetOrderId() {
        Order order = new Order(123, 456, steak, 1, System.currentTimeMillis());
        assertEquals("getOrderId should return 123", 123, order.getOrderId());
    }

    @Test
    public void testGetClientId() {
        Order order = new Order(1, 789, pizza, 2, System.currentTimeMillis());
        assertEquals("getClientId should return 789", 789, order.getClientId());
    }

    @Test
    public void testGetDish() {
        Order order = new Order(1, 1, dessert, 1, System.currentTimeMillis());
        assertEquals("getDish should return dessert", dessert, order.getDish());
    }

    @Test
    public void testGetPriority() {
        Order order = new Order(1, 1, steak, 2, System.currentTimeMillis());
        assertEquals("getPriority should return 2", 2, order.getPriority());
    }

    @Test
    public void testGetTimestamp() {
        long timestamp = System.currentTimeMillis();
        Order order = new Order(1, 1, pizza, 2, timestamp);
        assertEquals("getTimestamp should return the timestamp", timestamp, order.getTimestamp());
    }

    // ===== PRIORITY CONSTANT TESTS =====

    @Test
    public void testPriorityConstants() {
        Order urgent = new Order(1, 1, dessert, 1, System.currentTimeMillis());
        Order normal = new Order(2, 1, steak, 2, System.currentTimeMillis());
        Order slow = new Order(3, 1, pizza, 3, System.currentTimeMillis());

        assertEquals("Urgent priority should be 1", 1, urgent.getPriority());
        assertEquals("Normal priority should be 2", 2, normal.getPriority());
        assertEquals("Slow priority should be 3", 3, slow.getPriority());
    }

    // ===== EDGE CASE TESTS =====

    @Test
    public void testZeroOrderId() {
        Order order = new Order(0, 1, pizza, 2, System.currentTimeMillis());
        assertEquals("Should handle zero order ID", 0, order.getOrderId());
    }

    @Test
    public void testNegativeClientId() {
        Order order = new Order(1, -1, pizza, 2, System.currentTimeMillis());
        assertEquals("Should handle negative client ID", -1, order.getClientId());
    }

    @Test
    public void testZeroTimestamp() {
        Order order = new Order(1, 1, pizza, 2, 0);
        assertEquals("Should handle zero timestamp", 0, order.getTimestamp());
    }

    @Test
    public void testMinimumPriority() {
        Order order = new Order(1, 1, pizza, Integer.MIN_VALUE, System.currentTimeMillis());
        assertEquals("Should handle minimum priority", Integer.MIN_VALUE, order.getPriority());
    }

    @Test
    public void testMaximumPriority() {
        Order order = new Order(1, 1, pizza, Integer.MAX_VALUE, System.currentTimeMillis());
        assertEquals("Should handle maximum priority", Integer.MAX_VALUE, order.getPriority());
    }
}
