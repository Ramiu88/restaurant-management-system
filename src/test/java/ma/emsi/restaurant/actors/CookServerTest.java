package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Constants;
import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.entities.Dish;
import ma.emsi.restaurant.entities.Order;
import ma.emsi.restaurant.managers.KitchenManager;
import ma.emsi.restaurant.managers.OrderQueue;
import ma.emsi.restaurant.managers.StockManager;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Map;

/**
 * JUnit integration tests for Cook and Server actors
 * Tests the complete order workflow: Server creates order → Cook processes it
 */
public class CookServerTest {

    private OrderQueue orderQueue;
    private KitchenManager kitchenManager;
    private StockManager stockManager;

    @Before
    public void setUp() {
        // Reset singleton for clean test state
        Restaurant.resetForTesting();

        orderQueue = Restaurant.getInstance().getOrderQueue();
        kitchenManager = Restaurant.getInstance().getKitchenManager();
        stockManager = Restaurant.getInstance().getStockManager();
    }

    @After
    public void tearDown() {
        orderQueue = null;
        kitchenManager = null;
        stockManager = null;
    }

    // ===== ORDERQUEUE UTILITY TESTS =====

    @Test
    public void testOrderQueue_GenerateUniqueId() {
        int id1 = orderQueue.generateOrderId();
        int id2 = orderQueue.generateOrderId();
        int id3 = orderQueue.generateOrderId();

        assertEquals("First ID should be 1", 1, id1);
        assertEquals("Second ID should be 2", 2, id2);
        assertEquals("Third ID should be 3", 3, id3);
    }

    @Test
    public void testOrderQueue_InitialState() {
        assertTrue("Queue should be empty initially", orderQueue.isEmpty());
        assertEquals("Size should be 0", 0, orderQueue.size());
    }

    // ===== SERVER WORKFLOW TESTS =====

    @Test
    public void testServerWorkflow_CreateOrder() throws InterruptedException {
        Server server = new Server(1);

        // Simulate server creating an order
        Dish dish = Dish.DESSERT;
        int priority = Constants.PRIORITY_URGENT;

        Order order = new Order(
            orderQueue.generateOrderId(),
            1,
            dish,
            priority,
            System.currentTimeMillis()
        );

        orderQueue.addOrder(order);

        assertFalse("Queue should not be empty", orderQueue.isEmpty());
        assertEquals("Size should be 1", 1, orderQueue.size());
    }

    @Test
    public void testServerWorkflow_MultipleOrders() throws InterruptedException {
        Server server = new Server(1);
        Dish[] menu = {Dish.DESSERT, Dish.STEAK, Dish.PIZZA};

        for (Dish dish : menu) {
            Order order = new Order(
                orderQueue.generateOrderId(),
                1,
                dish,
                dish.getPreparationTime() <= 500 ? Constants.PRIORITY_URGENT :
                dish.getPreparationTime() <= 3000 ? Constants.PRIORITY_NORMAL :
                Constants.PRIORITY_SLOW,
                System.currentTimeMillis()
            );
            orderQueue.addOrder(order);
        }

        assertEquals("All orders should be in queue", 3, orderQueue.size());
    }

    // ===== COOK WORKFLOW TESTS =====

    @Test
    public void testCookWorkflow_ProcessOrder() throws InterruptedException {
        Cook cook = new Cook(1);

        // Add an order with simple requirements (Dessert has no equipment)
        Order order = new Order(
            orderQueue.generateOrderId(),
            1,
            Dish.DESSERT,
            Constants.PRIORITY_URGENT,
            System.currentTimeMillis()
        );
        orderQueue.addOrder(order);

        // Cook should be able to take the order
        Order taken = orderQueue.takeOrder();

        assertNotNull("Order should be taken", taken);
        assertEquals("Order ID should match", order.getOrderId(), taken.getOrderId());
    }

    @Test
    public void testCookWorkflow_WithStockCheck() {
        Cook cook = new Cook(1);

        // Dessert ingredients
        assertTrue("Dessert should have ingredients", Dish.DESSERT.getIngredients() != null);
        assertFalse("Dessert ingredients should not be empty", Dish.DESSERT.getIngredients().isEmpty());

        // Check if stock manager can consume ingredients
        boolean consumed = stockManager.consumeIngredients(Dish.DESSERT.getIngredients());

        // First consume should succeed (stock starts at 50)
        assertTrue("Should consume dessert ingredients", consumed);
    }

    @Test
    public void testCookWorkflow_WithEquipment() {
        Cook cook = new Cook(1);

        // Steak needs Grill1
        List<String> equipment = Dish.STEAK.getRequiredEquipment();
        assertFalse("Steak should require equipment", equipment.isEmpty());

        boolean acquired = kitchenManager.acquireEquipment(equipment, (int) Constants.EQUIPMENT_TIMEOUT_SECONDS);

        assertTrue("Should acquire grill for steak", acquired);

        // Release it
        kitchenManager.releaseEquipment(equipment);
    }

    // ===== INTEGRATION: SERVER TO COOK WORKFLOW =====

    @Test(timeout = 10000)
    public void testIntegration_ServerToCook() throws InterruptedException {
        // Simulate server adding order
        Dish dish = Dish.DESSERT; // No equipment needed, simplest case
        Order order = new Order(
            orderQueue.generateOrderId(),
            100,
            dish,
            Constants.PRIORITY_URGENT,
            System.currentTimeMillis()
        );

        orderQueue.addOrder(order);

        // Simulate cook taking order
        Order taken = orderQueue.takeOrder();

        assertNotNull("Cook should receive order", taken);
        assertEquals("Order should match", order.getOrderId(), taken.getOrderId());
        assertEquals("Client should match", 100, taken.getClientId());
    }

    @Test(timeout = 10000)
    public void testIntegration_MultipleServersOneCook() throws InterruptedException {
        final int NUM_SERVERS = 3;
        final int ORDERS_PER_SERVER = 2;
        final int TOTAL_ORDERS = NUM_SERVERS * ORDERS_PER_SERVER;

        // Multiple servers adding orders
        for (int i = 0; i < NUM_SERVERS; i++) {
            final int serverId = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < ORDERS_PER_SERVER; j++) {
                        Order order = new Order(
                            orderQueue.generateOrderId(),
                            serverId,
                            Dish.DESSERT,
                            Constants.PRIORITY_URGENT,
                            System.currentTimeMillis()
                        );
                        orderQueue.addOrder(order);
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Single cook processing orders
        int processed = 0;
        long startTime = System.currentTimeMillis();

        while (processed < TOTAL_ORDERS &&
               System.currentTimeMillis() - startTime < 5000) {
            Order order = orderQueue.takeOrder();
            assertNotNull("Order should not be null", order);
            processed++;
        }

        assertEquals("All orders should be processed", TOTAL_ORDERS, processed);
    }

    // ===== PRIORITY ORDERING INTEGRATION =====

    @Test
    public void testIntegration_PriorityOrdering() throws InterruptedException {
        long now = System.currentTimeMillis();

        // Add orders in reverse priority order
        orderQueue.addOrder(new Order(1, 1, Dish.PIZZA, Constants.PRIORITY_SLOW, now));
        orderQueue.addOrder(new Order(2, 1, Dish.DESSERT, Constants.PRIORITY_URGENT, now + 10));
        orderQueue.addOrder(new Order(3, 1, Dish.STEAK, Constants.PRIORITY_NORMAL, now + 20));

        // Orders should come out in priority order
        Order first = orderQueue.takeOrder();
        Order second = orderQueue.takeOrder();
        Order third = orderQueue.takeOrder();

        assertEquals("First should be urgent (DESSERT)", Constants.PRIORITY_URGENT, first.getPriority());
        assertEquals("Second should be normal (STEAK)", Constants.PRIORITY_NORMAL, second.getPriority());
        assertEquals("Third should be slow (PIZZA)", Constants.PRIORITY_SLOW, third.getPriority());
    }

    // ===== EQUIPMENT INTEGRATION =====

    @Test(timeout = 10000)
    public void testIntegration_CookWithEquipment() throws InterruptedException {
        // Create an order that needs equipment
        Order order = new Order(
            orderQueue.generateOrderId(),
            1,
            Dish.STEAK,
            Constants.PRIORITY_NORMAL,
            System.currentTimeMillis()
        );

        orderQueue.addOrder(order);

        // Cook takes order
        Order taken = orderQueue.takeOrder();

        // Cook acquires equipment
        List<String> equipment = taken.getDish().getRequiredEquipment();
        boolean acquired = kitchenManager.acquireEquipment(equipment, 2);

        assertTrue("Should acquire equipment", acquired);

        // Simulate cooking
        Thread.sleep(100);

        // Release equipment
        kitchenManager.releaseEquipment(equipment);
    }

    // ===== STOCK INTEGRATION =====

    @Test
    public void testIntegration_CookWithStock() {
        // Test stock consumption
        Map<String, Integer> ingredients = Dish.DESSERT.getIngredients();
        int initialStock = stockManager.getIngredientLevel("Milk");

        // Consume ingredients
        boolean consumed = stockManager.consumeIngredients(ingredients);

        assertTrue("Should consume ingredients", consumed);

        // Stock should have decreased
        int newStock = stockManager.getIngredientLevel("Milk");
        assertEquals("Stock should decrease by 1", initialStock - 1, newStock);
    }

    // ===== DISH PRE-DEFINED VALUES TESTS =====

    @Test
    public void testPreDefinedDishes_Consistency() {
        // All pre-defined dishes should have non-null values
        assertNotNull("DESSERT should exist", Dish.DESSERT);
        assertNotNull("STEAK should exist", Dish.STEAK);
        assertNotNull("PIZZA should exist", Dish.PIZZA);

        // All should have positive prep times
        assertTrue("DESSERT prep time should be positive", Dish.DESSERT.getPreparationTime() > 0);
        assertTrue("STEAK prep time should be positive", Dish.STEAK.getPreparationTime() > 0);
        assertTrue("PIZZA prep time should be positive", Dish.PIZZA.getPreparationTime() > 0);

        // All should have ingredients
        assertFalse("DESSERT should have ingredients", Dish.DESSERT.getIngredients().isEmpty());
        assertFalse("STEAK should have ingredients", Dish.STEAK.getIngredients().isEmpty());
        assertFalse("PIZZA should have ingredients", Dish.PIZZA.getIngredients().isEmpty());
    }

    // ===== CONCURRENT COOKS TEST =====

    @Test(timeout = 15000)
    public void testConcurrentCooks() throws InterruptedException {
        final int NUM_COOKS = 3;
        final int ORDERS_PER_COOK = 5;
        Thread[] cooks = new Thread[NUM_COOKS];
        final int[] processedCount = {0};

        // Add orders to queue
        for (int i = 0; i < NUM_COOKS * ORDERS_PER_COOK; i++) {
            orderQueue.addOrder(new Order(
                i + 1,
                i % NUM_COOKS,
                Dish.DESSERT,
                Constants.PRIORITY_URGENT,
                System.currentTimeMillis()
            ));
        }

        // Start cooks
        for (int i = 0; i < NUM_COOKS; i++) {
            final int cookId = i;
            cooks[i] = new Thread(() -> {
                int localProcessed = 0;
                while (localProcessed < ORDERS_PER_COOK) {
                    try {
                        Order order = orderQueue.takeOrder();
                        // Simulate quick processing
                        Thread.sleep(50);
                        localProcessed++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                synchronized (processedCount) {
                    processedCount[0] += localProcessed;
                }
            }, "Cook-" + cookId);
            cooks[i].start();
        }

        // Wait for all cooks
        for (Thread cook : cooks) {
            cook.join();
        }

        assertEquals("All orders should be processed", NUM_COOKS * ORDERS_PER_COOK, processedCount[0]);
    }
}
