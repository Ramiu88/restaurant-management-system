package ma.emsi.restaurant.managers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;
import ma.emsi.restaurant.Constants;

/**
 * JUnit tests for StockManager
 * Tests stock consumption, replenishment, and thread coordination
 *
 * @author Saladin
 */
public class StockManagerTest {

    private StockManager stock;

    @Before
    public void setUp() {
        stock = new StockManager();
    }

    @After
    public void tearDown() {
        stock = null;
    }

    // ===== BASIC FUNCTIONALITY TESTS =====

    @Test
    public void testInitialStock_AllIngredientsAvailable() {
        Map<String, Integer> levels = stock.getStockLevels();

        assertNotNull("Stock levels should not be null", levels);
        assertTrue("Should have Tomato", levels.containsKey("Tomato"));
        assertTrue("Should have Cheese", levels.containsKey("Cheese"));
        assertTrue("Should have Meat", levels.containsKey("Meat"));
        assertTrue("Should have Dough", levels.containsKey("Dough"));
        assertTrue("Should have Milk", levels.containsKey("Milk"));
        assertTrue("Should have Sugar", levels.containsKey("Sugar"));

        // All should start at STOCK_INITIAL (50)
        assertEquals("Tomato should be " + Constants.STOCK_INITIAL,
                    Constants.STOCK_INITIAL, (int) levels.get("Tomato"));
        assertEquals("Cheese should be " + Constants.STOCK_INITIAL,
                    Constants.STOCK_INITIAL, (int) levels.get("Cheese"));
    }

    @Test
    public void testInitialStock_NotLow() {
        assertFalse("Stock should not be low initially",
                   stock.isStockLow());
    }

    @Test
    public void testConsumeIngredients_Success() {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 5);
        recipe.put("Tomato", 3);

        boolean success = stock.consumeIngredients(recipe);

        assertTrue("Should successfully consume ingredients", success);

        // Verify stock decreased
        assertEquals("Cheese should decrease",
                    Constants.STOCK_INITIAL - 5,
                    stock.getIngredientLevel("Cheese"));
        assertEquals("Tomato should decrease",
                    Constants.STOCK_INITIAL - 3,
                    stock.getIngredientLevel("Tomato"));
    }

    @Test
    public void testConsumeIngredients_InsufficientStock() {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 100);  // More than available

        boolean success = stock.consumeIngredients(recipe);

        assertFalse("Should fail when insufficient stock", success);

        // Stock should not change
        assertEquals("Cheese should not change",
                    Constants.STOCK_INITIAL,
                    stock.getIngredientLevel("Cheese"));
    }

    @Test
    public void testConsumeIngredients_ExactAmount() {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", Constants.STOCK_INITIAL);  // Exact amount

        boolean success = stock.consumeIngredients(recipe);

        assertTrue("Should succeed with exact amount", success);
        assertEquals("Cheese should be 0",
                    0, stock.getIngredientLevel("Cheese"));
    }

    @Test
    public void testConsumeIngredients_MultipleIngredients() {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 2);
        recipe.put("Tomato", 3);
        recipe.put("Dough", 1);

        boolean success = stock.consumeIngredients(recipe);

        assertTrue("Should consume multiple ingredients", success);
        assertEquals(Constants.STOCK_INITIAL - 2,
                    stock.getIngredientLevel("Cheese"));
        assertEquals(Constants.STOCK_INITIAL - 3,
                    stock.getIngredientLevel("Tomato"));
        assertEquals(Constants.STOCK_INITIAL - 1,
                    stock.getIngredientLevel("Dough"));
    }

    @Test
    public void testConsumeIngredients_OneInsufficientIngredient() {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 2);      // Available
        recipe.put("Tomato", 1000);   // Not available

        boolean success = stock.consumeIngredients(recipe);

        assertFalse("Should fail if any ingredient insufficient", success);

        // Neither should be consumed (all-or-nothing)
        assertEquals("Cheese should not change",
                    Constants.STOCK_INITIAL,
                    stock.getIngredientLevel("Cheese"));
        assertEquals("Tomato should not change",
                    Constants.STOCK_INITIAL,
                    stock.getIngredientLevel("Tomato"));
    }

    // ===== STOCK LOW DETECTION TESTS =====

    @Test
    public void testIsStockLow_BecomesTrueWhenLow() {
        // Consume until low
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 45);  // Leaves 5, which is below LOW_THRESHOLD (10)

        stock.consumeIngredients(recipe);

        assertTrue("Should detect stock is low", stock.isStockLow());
    }

    @Test
    public void testIsStockLow_AtThreshold() {
        // Consume to exactly the threshold
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", Constants.STOCK_INITIAL - Constants.STOCK_LOW_THRESHOLD);

        stock.consumeIngredients(recipe);

        assertFalse("Should not be low at threshold", stock.isStockLow());
    }

    @Test
    public void testIsStockLow_BelowThreshold() {
        // Consume to below threshold
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", Constants.STOCK_INITIAL - Constants.STOCK_LOW_THRESHOLD + 1);

        stock.consumeIngredients(recipe);

        assertTrue("Should be low below threshold", stock.isStockLow());
    }

    // ===== REPLENISHMENT TESTS =====

    @Test
    public void testReplenish_AddsToAllIngredients() {
        // First consume some
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 10);
        recipe.put("Tomato", 15);
        stock.consumeIngredients(recipe);

        // Then replenish
        stock.replenish();

        // Should add STOCK_REPLENISH_AMOUNT (50) to each
        assertEquals("Cheese should be replenished",
                    Constants.STOCK_INITIAL - 10 + Constants.STOCK_REPLENISH_AMOUNT,
                    stock.getIngredientLevel("Cheese"));
        assertEquals("Tomato should be replenished",
                    Constants.STOCK_INITIAL - 15 + Constants.STOCK_REPLENISH_AMOUNT,
                    stock.getIngredientLevel("Tomato"));
    }

    @Test
    public void testReplenish_MakesStockNotLow() {
        // Consume until low
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 45);
        stock.consumeIngredients(recipe);

        assertTrue("Should be low before replenish", stock.isStockLow());

        // Replenish
        stock.replenish();

        assertFalse("Should not be low after replenish", stock.isStockLow());
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test(timeout = 5000)
    public void testConcurrentConsumption_TwoThreads() throws InterruptedException {
        final int CONSUMPTION_PER_THREAD = 10;

        Thread t1 = new Thread(() -> {
            Map<String, Integer> recipe = new HashMap<>();
            recipe.put("Cheese", 1);
            for (int i = 0; i < CONSUMPTION_PER_THREAD; i++) {
                stock.consumeIngredients(recipe);
            }
        });

        Thread t2 = new Thread(() -> {
            Map<String, Integer> recipe = new HashMap<>();
            recipe.put("Cheese", 1);
            for (int i = 0; i < CONSUMPTION_PER_THREAD; i++) {
                stock.consumeIngredients(recipe);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Should consume exactly 20 total
        assertEquals("Should consume correct amount with 2 threads",
                    Constants.STOCK_INITIAL - 20,
                    stock.getIngredientLevel("Cheese"));
    }

    @Test(timeout = 10000)
    public void testConcurrentConsumption_MultipleThreads() throws InterruptedException {
        final int NUM_THREADS = 10;
        final int CONSUMPTION_PER_THREAD = 3;

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                Map<String, Integer> recipe = new HashMap<>();
                recipe.put("Cheese", 1);
                for (int j = 0; j < CONSUMPTION_PER_THREAD; j++) {
                    stock.consumeIngredients(recipe);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // 10 threads × 3 consumption × 1 cheese = 30 cheese consumed
        assertEquals("Should consume correct amount with 10 threads",
                    Constants.STOCK_INITIAL - 30,
                    stock.getIngredientLevel("Cheese"));
    }

    @Test(timeout = 5000)
    public void testGetStockLevels_WhileConsuming() throws InterruptedException {
        Thread consumer = new Thread(() -> {
            Map<String, Integer> recipe = new HashMap<>();
            recipe.put("Cheese", 1);
            for (int i = 0; i < 30; i++) {
                stock.consumeIngredients(recipe);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        Thread reader = new Thread(() -> {
            for (int i = 0; i < 30; i++) {
                Map<String, Integer> levels = stock.getStockLevels();
                assertNotNull("Levels should not be null", levels);
                assertTrue("Cheese level should be non-negative",
                          levels.get("Cheese") >= 0);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        consumer.start();
        reader.start();
        consumer.join();
        reader.join();

        // Just verify no exceptions and final state is valid
        assertTrue("Final cheese level should be valid",
                  stock.getIngredientLevel("Cheese") >= 0);
    }

    // ===== INTEGRATION TESTS =====

    @Test
    public void testGetStockStatus_ContainsAllIngredients() {
        String status = stock.getStockStatus();

        assertNotNull("Status should not be null", status);
        assertTrue("Should contain Tomato", status.contains("Tomato"));
        assertTrue("Should contain Cheese", status.contains("Cheese"));
        assertTrue("Should contain Meat", status.contains("Meat"));
        assertTrue("Should contain Dough", status.contains("Dough"));
        assertTrue("Should contain Milk", status.contains("Milk"));
        assertTrue("Should contain Sugar", status.contains("Sugar"));
    }

    @Test
    public void testGetStockStatus_ShowsLowWarning() {
        // Consume until low
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 45);
        stock.consumeIngredients(recipe);

        String status = stock.getStockStatus();

        assertTrue("Should show LOW warning", status.contains("LOW"));
    }

    @Test
    public void testGetIngredientLevel_UnknownIngredient() {
        int level = stock.getIngredientLevel("UnknownIngredient");
        assertEquals("Unknown ingredient should return 0", 0, level);
    }

    // ===== STRESS TESTS =====

    @Test(timeout = 15000)
    public void stressTest_HighConcurrency() throws InterruptedException {
        final int NUM_THREADS = 20;
        final int OPERATIONS_PER_THREAD = 100;

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                Map<String, Integer> recipe = new HashMap<>();
                recipe.put("Milk", 1);
                recipe.put("Sugar", 1);

                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    stock.consumeIngredients(recipe);

                    // Occasionally check status
                    if (j % 10 == 0) {
                        stock.getStockLevels();
                        stock.isStockLow();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Verify final state is consistent
        int milkConsumed = Constants.STOCK_INITIAL - stock.getIngredientLevel("Milk");
        int sugarConsumed = Constants.STOCK_INITIAL - stock.getIngredientLevel("Sugar");

        assertEquals("Milk and Sugar should be consumed equally",
                    milkConsumed, sugarConsumed);
    }
}
