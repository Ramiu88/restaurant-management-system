package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Dish;
import ma.emsi.restaurant.entities.Order;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * JUnit tests for OrderQueue
 * Tests producer-consumer pattern, bounded buffer, and utility methods
 */
public class OrderQueueTest {

    private OrderQueue orderQueue;
    private Dish testDish;

    @Before
    public void setUp() {
        orderQueue = new OrderQueue(5); // Small capacity for testing
        testDish = Dish.PIZZA;
    }

    @After
    public void tearDown() {
        orderQueue = null;
    }

    // ===== BASIC FUNCTIONALITY TESTS =====

    @Test
    public void testInitialState() {
        assertTrue("Queue should be empty initially", orderQueue.isEmpty());
        assertEquals("Size should be 0", 0, orderQueue.size());
    }

    @Test
    public void testGenerateOrderId() {
        int id1 = orderQueue.generateOrderId();
        int id2 = orderQueue.generateOrderId();
        int id3 = orderQueue.generateOrderId();

        assertEquals("First ID should be 1", 1, id1);
        assertEquals("Second ID should be 2", 2, id2);
        assertEquals("Third ID should be 3", 3, id3);
    }

    @Test
    public void testGenerateOrderId_ThreadSafe() throws InterruptedException {
        final int[] ids = new int[100];
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    ids[threadIndex * 10 + j] = orderQueue.generateOrderId();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Check all IDs are unique
        for (int i = 0; i < ids.length; i++) {
            for (int j = i + 1; j < ids.length; j++) {
                assertNotEquals("IDs should be unique", ids[i], ids[j]);
            }
        }
    }

    // ===== PRODUCER-CONSUMER TESTS =====

    @Test
    public void testAddOrder() throws InterruptedException {
        Order order = new Order(1, 1, testDish, 1, System.currentTimeMillis());
        orderQueue.addOrder(order);

        assertFalse("Queue should not be empty", orderQueue.isEmpty());
        assertEquals("Size should be 1", 1, orderQueue.size());
    }

    @Test
    public void testTakeOrder() throws InterruptedException {
        Order order = new Order(1, 1, testDish, 1, System.currentTimeMillis());
        orderQueue.addOrder(order);

        Order taken = orderQueue.takeOrder();

        assertNotNull("Taken order should not be null", taken);
        assertEquals("Taken order should have same ID", 1, taken.getOrderId());
        assertTrue("Queue should be empty after taking", orderQueue.isEmpty());
    }

    @Test
    public void testTakeOrder_BlocksWhenEmpty() throws InterruptedException {
        Thread consumer = new Thread(() -> {
            try {
                // This should block until an order is added
                Order order = orderQueue.takeOrder();
                assertEquals("Order ID should match", 1, order.getOrderId());
            } catch (InterruptedException e) {
                fail("Consumer should not be interrupted");
            }
        });

        consumer.start();

        // Wait a bit to ensure consumer is waiting
        Thread.sleep(100);

        // Now add an order
        Order order = new Order(1, 1, testDish, 1, System.currentTimeMillis());
        orderQueue.addOrder(order);

        // Wait for consumer to finish
        consumer.join(1000);

        assertFalse("Consumer should have completed", consumer.isAlive());
    }

    // ===== BOUNDED BUFFER TESTS =====

    @Test
    public void testBoundedBuffer_BlocksWhenFull() throws InterruptedException {
        // Fill the queue to capacity
        for (int i = 0; i < 5; i++) {
            orderQueue.addOrder(new Order(i, i, testDish, 1, System.currentTimeMillis()));
        }

        assertEquals("Queue should be full", 5, orderQueue.size());

        final boolean[] producerBlocked = {false};
        Thread producer = new Thread(() -> {
            try {
                // This should block until space is available
                producerBlocked[0] = true; // Mark that we entered the blocking call
                orderQueue.addOrder(new Order(99, 99, testDish, 1, System.currentTimeMillis()));
            } catch (InterruptedException e) {
                fail("Producer should not be interrupted");
            }
        });

        producer.start();

        // Wait a bit to ensure producer is blocked
        Thread.sleep(500);

        // Producer should still be alive (blocked)
        assertTrue("Producer should be blocked waiting for space", producer.isAlive());

        // Consume one order to free space
        orderQueue.takeOrder();

        // Wait for producer to complete
        producer.join(2000);

        assertFalse("Producer should have completed", producer.isAlive());
        assertEquals("Queue should be full again", 5, orderQueue.size());
    }

    // ===== PRIORITY QUEUE TESTS =====

    @Test
    public void testPriorityOrdering_HighPriorityFirst() throws InterruptedException {
        long now = System.currentTimeMillis();

        // Add orders: LOW -> HIGH -> MEDIUM
        orderQueue.addOrder(new Order(1, 1, Dish.PIZZA, 3, now));     // LOW (SLOW)
        orderQueue.addOrder(new Order(2, 1, Dish.DESSERT, 1, now));    // HIGH (URGENT)
        orderQueue.addOrder(new Order(3, 1, Dish.STEAK, 2, now));      // MEDIUM (NORMAL)

        // Should come out in priority order: HIGH -> MEDIUM -> LOW
        Order first = orderQueue.takeOrder();
        Order second = orderQueue.takeOrder();
        Order third = orderQueue.takeOrder();

        assertEquals("First should be HIGH priority (DESSERT)", 1, first.getPriority());
        assertEquals("Second should be MEDIUM priority (STEAK)", 2, second.getPriority());
        assertEquals("Third should be LOW priority (PIZZA)", 3, third.getPriority());
    }

    @Test
    public void testPriorityOrdering_FIFOWithinSamePriority() throws InterruptedException {
        long now = System.currentTimeMillis();

        // Add multiple orders with same priority
        orderQueue.addOrder(new Order(1, 1, Dish.STEAK, 2, now));
        Thread.sleep(10);
        orderQueue.addOrder(new Order(2, 1, Dish.STEAK, 2, now + 10));
        Thread.sleep(10);
        orderQueue.addOrder(new Order(3, 1, Dish.STEAK, 2, now + 20));

        // Should come out in timestamp order (FIFO)
        Order first = orderQueue.takeOrder();
        Order second = orderQueue.takeOrder();
        Order third = orderQueue.takeOrder();

        assertEquals("First should have earliest timestamp", now, first.getTimestamp());
        assertEquals("Second should have middle timestamp", now + 10, second.getTimestamp());
        assertEquals("Third should have latest timestamp", now + 20, third.getTimestamp());
    }

    // ===== CONCURRENT PRODUCER-CONSUMER TESTS =====

    @Test(timeout = 10000)
    public void testMultipleProducersConsumers() throws InterruptedException {
        final int NUM_PRODUCERS = 3;
        final int NUM_CONSUMERS = 2;
        final int ORDERS_PER_PRODUCER = 10;
        final int TOTAL_ORDERS = NUM_PRODUCERS * ORDERS_PER_PRODUCER;

        Thread[] producers = new Thread[NUM_PRODUCERS];
        Thread[] consumers = new Thread[NUM_CONSUMERS];

        // Start consumers
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            final int consumerId = i;
            consumers[i] = new Thread(() -> {
                int consumed = 0;
                while (consumed < (TOTAL_ORDERS / NUM_CONSUMERS)) {
                    try {
                        Order order = orderQueue.takeOrder();
                        assertNotNull("Order should not be null", order);
                        consumed++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Consumer-" + consumerId);
            consumers[i].start();
        }

        // Start producers
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            final int producerId = i;
            producers[i] = new Thread(() -> {
                for (int j = 0; j < ORDERS_PER_PRODUCER; j++) {
                    try {
                        Order order = new Order(
                            orderQueue.generateOrderId(),
                            producerId,
                            testDish,
                            2,
                            System.currentTimeMillis()
                        );
                        orderQueue.addOrder(order);
                        Thread.sleep(10); // Small delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Producer-" + producerId);
            producers[i].start();
        }

        // Wait for all threads
        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        assertTrue("Queue should be empty after all processing", orderQueue.isEmpty());
    }
}
