package ma.emsi.restaurant;

import ma.emsi.restaurant.managers.*;

/**
 * The "Mother Class" / Mediator.
 * 
 * This class acts as the central orchestration point (Mediator Pattern).
 * It initializes and holds references to all shared resources (Monitors),
 * ensuring that Actors (Threads) access state through controlled access points
 * rather than global variables.
 * 
 * Design Pattern: Singleton (Thread-Safe Initialization)
 */
public class Restaurant {
    private static Restaurant instance;
    
    // The Monitors (Shared Resources with Synchronization)
    private final TableManager tableManager;
    private final OrderQueue orderQueue;
    private final KitchenManager kitchenManager;
    private final FinanceManager financeManager;
    private final StockManager stockManager;

    private Restaurant() {
        // Initialize Monitors
        this.tableManager = new TableManager();
        this.orderQueue = new OrderQueue();
        this.kitchenManager = new KitchenManager();
        this.financeManager = new FinanceManager();
        this.stockManager = new StockManager();
    }

    /**
     * Thread-safe Singleton accessor.
     */
    public static synchronized Restaurant getInstance() {
        if (instance == null) {
            instance = new Restaurant();
        }
        return instance;
    }

    // Accessors for Monitors
    public TableManager getTableManager() { return tableManager; }
    public OrderQueue getOrderQueue() { return orderQueue; }
    public KitchenManager getKitchenManager() { return kitchenManager; }
    public FinanceManager getFinanceManager() { return financeManager; }
    public StockManager getStockManager() { return stockManager; }

    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        // TODO: Initialize and start Actor threads (Servers, Cooks, etc.)
        System.out.println("Restaurant Simulation Started...");
    }
}
