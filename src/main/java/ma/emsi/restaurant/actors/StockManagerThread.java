package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.managers.StockManager;
import ma.emsi.restaurant.Constants;

/**
 * Background daemon thread that continuously monitors stock levels
 * and automatically replenishes when ingredients run low.
 *
 * This demonstrates the wait/notify pattern with a dedicated background thread.
 * The thread sleeps when stock is sufficient and wakes when notified by cooks.
 *
 * Integration: Works with StockManager, notified by Cook (Cranky's module)
 *
 * @author Saladin
 */
public class StockManagerThread implements Runnable {

    private volatile boolean running = true;

    @Override
    public void run() {
        StockManager stockManager = Restaurant.getInstance().getStockManager();

        System.out.println("[StockManagerThread] Background monitoring started");
        System.out.println("[StockManagerThread] Will replenish when stock drops below " +
                          Constants.STOCK_LOW_THRESHOLD);

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                synchronized (stockManager) {
                    // Wait until stock is low
                    while (!stockManager.isStockLow() && running) {
                        System.out.println("[StockManagerThread] Stock levels OK, sleeping...");
                        stockManager.wait(); // Sleep until notified by cook or consumeIngredients
                    }

                    if (!running) break; // Exit if stopped

                    // Stock is low, time to order delivery
                    System.out.println("[StockManagerThread] LOW STOCK DETECTED!");
                    System.out.println(stockManager.getStockStatus());
                    System.out.println("[StockManagerThread] Ordering delivery...");
                }

                // Simulate delivery time (outside synchronized block)
                System.out.println("[StockManagerThread] Waiting for delivery (" +
                                 Constants.STOCK_DELIVERY_TIME + "ms)...");
                Thread.sleep(Constants.STOCK_DELIVERY_TIME);

                // Replenish stock
                synchronized (stockManager) {
                    stockManager.replenish();
                    // replenish() calls notifyAll() to wake waiting cooks
                }

                System.out.println("[StockManagerThread] Delivery complete!");
                System.out.println(stockManager.getStockStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[StockManagerThread] Interrupted, shutting down");
                break;
            }
        }

        System.out.println("[StockManagerThread] Background monitoring stopped");
    }

    /**
     * Stop the background thread gracefully
     */
    public void stop() {
        running = false;
        // Wake up the thread if it's waiting
        StockManager stockManager = Restaurant.getInstance().getStockManager();
        synchronized (stockManager) {
            stockManager.notifyAll();
        }
    }
}
