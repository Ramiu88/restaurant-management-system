package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.entities.Table;
import ma.emsi.restaurant.managers.TableManager;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Actor representing a restaurant client (Thread)
 * Complete lifecycle: Arrive → Get Table → Browse Menu → Order → Eat → Pay → Leave
 * 
 * CONCURRENCY: Each client runs as an independent thread
 * INTEGRATION POINTS: OrderQueue (Cranky), FinanceManager (Saladin)
 * 
 * @author Walid - Table Management Module
 */
public class Client extends Thread {
    private final boolean isVip;
    private final TableManager tableManager;
    private final Random random;

    /**
     * Constructor - Uses Restaurant singleton to get TableManager
     * 
     * @param name Client identifier (e.g., "Client-1-VIP")
     * @param isVip Whether this is a VIP client
     */
    public Client(String name, boolean isVip) {
        super(name);
        this.isVip = isVip;
        this.tableManager = Restaurant.getInstance().getTableManager();
        this.random = ThreadLocalRandom.current();
    }

    /**
     * Main thread execution - Complete client lifecycle
     */
    @Override
    public void run() {
        try {
            // ═══════════════════════════════════════════════════════
            // PHASE 1: Arrival
            // ═══════════════════════════════════════════════════════
            arriveAtRestaurant();
            
            // ═══════════════════════════════════════════════════════
            // PHASE 2: Acquire Table (VIP priority, waiting queue)
            // ═══════════════════════════════════════════════════════
            Table table = tableManager.acquireTable(isVip);
            
            if (table == null) {
                System.err.println( getName() + " couldn't acquire a table!");
                return;
            }

            // ═══════════════════════════════════════════════════════
            // PHASE 3: Browse Menu
            // ═══════════════════════════════════════════════════════
            browseMenu();

            // ═══════════════════════════════════════════════════════
            // PHASE 4: Place Order (Integration with Cranky's OrderQueue)
            // ═══════════════════════════════════════════════════════
            placeOrder();

            // ═══════════════════════════════════════════════════════
            // PHASE 5: Wait for Food and Eat
            // ═══════════════════════════════════════════════════════
            waitAndEat();

            // ═══════════════════════════════════════════════════════
            // PHASE 6: Pay at Cashier (Integration with Saladin's FinanceManager)
            // ═══════════════════════════════════════════════════════
            payAtCashier();

            // ═══════════════════════════════════════════════════════
            // PHASE 7: Release Table and Leave
            // ═══════════════════════════════════════════════════════
            tableManager.releaseTable(table);
            leaveRestaurant();

        } catch (InterruptedException e) {
            System.err.println( getName() + " was interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LIFECYCLE METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * PHASE 1: Client arrives at restaurant
     */
    private void arriveAtRestaurant() {
        System.out.println( getName() + " arrived at the restaurant");
    }

    /**
     * PHASE 3: Client browses the menu (1-2 seconds)
     */
    private void browseMenu() throws InterruptedException {
        int browseTime = random.nextInt(1000, 2001); // 1-2 seconds
        System.out.println(getName() + " browsing menu (" + browseTime + "ms)...");
        Thread.sleep(browseTime);
    }

    /**
     * PHASE 4: Client places order
     * 
     * INTEGRATION POINT: Cranky's OrderQueue
     * /TODO: Replace with actual OrderQueue when Cranky's module is ready
     */
    private void placeOrder() {
        System.out.println(getName() + " placing order...");


        /* TODO: INTEGRATION WITH CRANKY'S MODULE */
        // ═══════════════════════════════════════════════════════════
        // Uncomment when OrderQueue is ready:
        
        String dishName = selectRandomDish();
        int priority = isVip ? 1 : 2; // VIP orders are URGENT
        
        Order order = new Order(
            getName(),
            new Dish(dishName, 3000), // 3 second prep time
            priority
        );
        
        Restaurant.getInstance().getOrderQueue().addOrder(order);
        System.out.println(getName() + " ordered: " + dishName);
        
        
        // STUB for now
        System.out.println("   [STUB] Order placed - waiting for Cranky's OrderQueue module");
    }

    /**
     * PHASE 5: Client waits for food and eats (3-5 seconds)
     */
    private void waitAndEat() throws InterruptedException {
        int eatTime = random.nextInt(3000, 5001); // 3-5 seconds
        System.out.println(getName() + " eating (" + eatTime + "ms)...");
        Thread.sleep(eatTime);
        System.out.println( getName() + " finished eating");
    }

    /**
     * PHASE 6: Client pays at cashier
     * 
     * INTEGRATION POINT: Saladin's FinanceManager
     /* TODO: Replace with actual FinanceManager when Saladin's module is ready
     */
    private void payAtCashier() {
        double amount = 15.0 + random.nextDouble() * 35.0; // €15-50
        System.out.println("💳 " + getName() + " paying €" + String.format("%.2f", amount) + "...");
        
        /* TODO: INTEGRATION WITH SALADIN'S MODULE */
        // Uncomment when FinanceManager is ready:
        
        Restaurant.getInstance().getFinanceManager().processPayment(amount);
        System.out.println( getName() + " payment processed");
        
        
        // STUB for now
        System.out.println("   [STUB] Payment processed - waiting for Saladin's FinanceManager module");
    }

    /**
     * PHASE 7: Client leaves restaurant
     */
    private void leaveRestaurant() {
        System.out.println(getName() + " left the restaurant\n");
    }

    // HELPER METHODS

    /**
     * Select random dish from menu (for future integration)
     */
    private String selectRandomDish() {
        String[] dishes = {"Pasta", "Pizza", "Burger", "Salad", "Steak"};
        return dishes[random.nextInt(dishes.length)];
    }

    // GETTERS (for testing)

    public boolean isVipClient() {
        return isVip;
    }
}