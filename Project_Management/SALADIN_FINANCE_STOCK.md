# SALADIN - Finance & Stock Management Module

## Overview
You are responsible for two critical systems:
1. **FinanceManager**: Demonstrates race conditions on shared revenue counter
2. **StockManager**: Background thread for automatic ingredient replenishment

Both modules showcase important concurrency problems and their solutions.

## Part A: Finance Manager (Race Condition)

### 1. managers/FinanceManager.java
**Description:** Monitor managing restaurant revenue - demonstrates race condition

**The Problem:**
Without proper synchronization, multiple cashiers updating `totalRevenue` simultaneously causes lost updates.

**Attributes:**
- `totalRevenue` (double) - Shared counter
- `customersServed` (int) - Number of customers paid
- `unsafeRevenue` (double) - For demonstration purposes

**Critical Methods:**
```java
public synchronized void processPayment(double amount)
public synchronized double getTotalRevenue()
public synchronized int getCustomersServed()
public void processPaymentUNSAFE(double amount)  // For demo only
```

### Detailed Implementation - FinanceManager.java

```java
package ma.emsi.restaurant.managers;

public class FinanceManager {
    private double totalRevenue = 0.0;
    private int customersServed = 0;

    // For demonstration: UNSAFE version
    private double unsafeRevenue = 0.0;

    /**
     * CORRECT: Synchronized method prevents race condition
     */
    public synchronized void processPayment(double amount) {
        // Read-Modify-Write is atomic because of synchronized
        totalRevenue += amount;
        customersServed++;

        System.out.println("[Finance] Payment processed: $" + amount +
                          " | Total: $" + totalRevenue);
    }

    /**
     * WRONG: Demonstrates race condition
     * DO NOT USE IN PRODUCTION - FOR TESTING ONLY
     */
    public void processPaymentUNSAFE(double amount) {
        // Race condition: Read-Modify-Write is NOT atomic
        // Multiple threads can interleave:
        // Thread1 reads 100, Thread2 reads 100
        // Thread1 writes 115, Thread2 writes 120
        // Result: 120 (should be 135!)
        unsafeRevenue += amount;

        System.out.println("[Finance UNSAFE] Payment: $" + amount +
                          " | Total: $" + unsafeRevenue);
    }

    public synchronized double getTotalRevenue() {
        return totalRevenue;
    }

    public synchronized double getUnsafeRevenue() {
        return unsafeRevenue;
    }

    public synchronized int getCustomersServed() {
        return customersServed;
    }

    /**
     * Demonstrates the race condition
     */
    public void demonstrateRaceCondition() {
        System.out.println("\n=== RACE CONDITION DEMONSTRATION ===");

        // Reset counters
        totalRevenue = 0;
        unsafeRevenue = 0;

        // Create multiple cashiers using UNSAFE method
        Thread[] cashiers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int id = i;
            cashiers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    processPaymentUNSAFE(10.0);
                }
            }, "Cashier-" + id);
            cashiers[i].start();
        }

        // Wait for all to finish
        for (Thread cashier : cashiers) {
            try {
                cashier.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nExpected revenue: $" + (10 * 100 * 10.0));
        System.out.println("Actual (UNSAFE): $" + unsafeRevenue);
        System.out.println("Money lost: $" + (10000.0 - unsafeRevenue));
    }
}
```

### 2. actors/Cashier.java
**Description:** Thread that processes customer payments

```java
package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;

public class Cashier implements Runnable {
    private final int cashierId;

    public Cashier(int cashierId) {
        this.cashierId = cashierId;
    }

    @Override
    public void run() {
        FinanceManager finance = Restaurant.getInstance().getFinanceManager();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for a customer to pay (simplified)
                // In real system, Client calls this
                Thread.sleep(500 + (int)(Math.random() * 1000));

                // Process payment (random amount between $10-$50)
                double amount = 10.0 + (Math.random() * 40.0);
                finance.processPayment(amount);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

## Part B: Stock Manager (Background Thread)

### 3. managers/StockManager.java
**Description:** Monitor managing ingredient inventory with automatic replenishment

**Attributes:**
- `stock` (Map<String, Integer>) - Current ingredient levels
- `lowThreshold` (int) - When to trigger replenishment
- Background thread continuously monitors levels

**Critical Methods:**
```java
public synchronized boolean consumeIngredients(Map<String, Integer> needed)
public synchronized void replenish()
public synchronized boolean isStockLow()
```

### Detailed Implementation - StockManager.java

```java
package ma.emsi.restaurant.managers;

import java.util.HashMap;
import java.util.Map;

public class StockManager {
    private final Map<String, Integer> stock;
    private final int lowThreshold;
    private final int replenishAmount;

    public StockManager() {
        this.stock = new HashMap<>();
        this.lowThreshold = Constants.STOCK_LOW_THRESHOLD;
        this.replenishAmount = Constants.STOCK_REPLENISH_AMOUNT;

        // Initialize stock
        stock.put("Tomato", Constants.STOCK_INITIAL);
        stock.put("Cheese", Constants.STOCK_INITIAL);
        stock.put("Meat", Constants.STOCK_INITIAL);
        stock.put("Dough", Constants.STOCK_INITIAL);
        stock.put("Milk", Constants.STOCK_INITIAL);
        stock.put("Sugar", Constants.STOCK_INITIAL);
    }

    /**
     * Cook tries to consume ingredients
     * Returns false if not enough stock
     */
    public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
        // First check if we have enough
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            String ingredient = entry.getKey();
            int amount = entry.getValue();

            if (!stock.containsKey(ingredient) ||
                stock.get(ingredient) < amount) {
                System.out.println("[Stock] Not enough " + ingredient);
                notify(); // Wake stock manager thread
                return false;
            }
        }

        // We have enough, consume it
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            String ingredient = entry.getKey();
            int amount = entry.getValue();
            stock.put(ingredient, stock.get(ingredient) - amount);
        }

        // Check if stock is now low
        if (isStockLow()) {
            notify(); // Wake stock manager to replenish
        }

        return true;
    }

    /**
     * Replenish all ingredients
     */
    public synchronized void replenish() {
        System.out.println("[Stock] Replenishing...");

        for (String ingredient : stock.keySet()) {
            stock.put(ingredient, stock.get(ingredient) + replenishAmount);
        }

        System.out.println("[Stock] Replenishment complete!");
        notifyAll(); // Wake all waiting cooks
    }

    /**
     * Check if any ingredient is below threshold
     */
    public synchronized boolean isStockLow() {
        for (int level : stock.values()) {
            if (level < lowThreshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get current stock levels (for dashboard)
     */
    public synchronized Map<String, Integer> getStockLevels() {
        return new HashMap<>(stock);
    }
}
```

### 4. actors/StockManagerThread.java
**Description:** Background daemon thread that monitors and replenishes stock

```java
package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.managers.StockManager;
import ma.emsi.restaurant.Constants;

/**
 * Background thread that continuously monitors stock
 * and replenishes when low
 */
public class StockManagerThread implements Runnable {

    @Override
    public void run() {
        StockManager stockManager = Restaurant.getInstance().getStockManager();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (stockManager) {
                    // Wait until stock is low
                    while (!stockManager.isStockLow()) {
                        System.out.println("[StockThread] Stock OK, sleeping...");
                        stockManager.wait(); // Sleep until notified
                    }

                    // Stock is low, simulate delivery
                    System.out.println("[StockThread] Stock low! Ordering delivery...");
                    Thread.sleep(Constants.STOCK_DELIVERY_TIME);

                    // Replenish
                    stockManager.replenish();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[StockThread] Shutting down...");
                break;
            }
        }
    }
}
```

## Flow Diagram: Cook-Stock Interaction

```text
COOK:
1. Take order from queue
2. synchronized(stockManager) {
       if (!stockManager.consumeIngredients(needed)) {
           stockManager.wait(); // Wait for replenishment
       }
   }
3. Ingredients consumed, proceed with cooking

STOCK MANAGER THREAD (background):
1. synchronized(stockManager) {
       while (!stockManager.isStockLow()) {
           stockManager.wait(); // Sleep
       }
   }
2. Stock low detected!
3. sleep(3000); // Simulate delivery
4. stockManager.replenish();
5. stockManager.notifyAll(); // Wake waiting cooks
```

## Tests to Implement

### Finance Tests:
1. **Test Race Condition:** Run demonstrateRaceCondition() and show money loss
2. **Test Synchronized:** Verify correct total with multiple cashiers
3. **Test Concurrent Payments:** 100+ payments, verify no loss

### Stock Tests:
1. **Test Consumption:** Verify ingredients decrease correctly
2. **Test Low Threshold:** Verify notification when low
3. **Test Replenishment:** Verify stock increases after delivery
4. **Test Wait/Notify:** Cooks wait when stock insufficient
5. **Test Background Thread:** Verify automatic replenishment

## Files to Create

```
src/main/java/ma/emsi/restaurant/
├── managers/
│   ├── FinanceManager.java       [TODO]
│   └── StockManager.java         [TODO]
└── actors/
    ├── Cashier.java              [TODO]
    └── StockManagerThread.java   [TODO]
```

## Implementation Order

1. **First:** `FinanceManager.java` with race condition demo - 2 hours
2. **Then:** `Cashier.java` - 30 minutes
3. **Then:** `StockManager.java` - 2 hours
4. **Finally:** `StockManagerThread.java` - 1 hour

## Integration Points

- **Called by Client** (Walid): Client calls `processPayment()` after eating
- **Called by Cook** (Cranky): Cook calls `consumeIngredients()` before cooking

## Critical Concepts

### Race Condition (Finance)
**Problem:** Non-atomic Read-Modify-Write on shared variable
**Solution:** `synchronized` keyword makes the operation atomic

### Producer-Consumer with Background Thread (Stock)
**Problem:** Cooks need ingredients, stock needs replenishment
**Solution:** Background thread waits and responds to low stock signals

## Checklist

- [ ] FinanceManager.java implemented
- [ ] processPayment() properly synchronized
- [ ] Race condition demonstration working
- [ ] Lost update scenario documented
- [ ] Cashier.java implemented
- [ ] StockManager.java implemented
- [ ] consumeIngredients() checks before consuming
- [ ] isStockLow() threshold logic correct
- [ ] replenish() with notifyAll() implemented
- [ ] StockManagerThread.java as daemon
- [ ] Background thread wait/notify working
- [ ] Integration with Restaurant.java completed
- [ ] Unit tests for both modules
- [ ] Documentation completed
