# SALADIN - Integration Vector Guide

## Overview

Your module has 4 classes that integrate with other engineers' code:
1. **FinanceManager** (Manager) - Used by Cashier and Client
2. **StockManager** (Manager) - Used by StockManagerThread and Cook
3. **Cashier** (Actor) - Uses FinanceManager
4. **StockManagerThread** (Actor) - Uses StockManager

## Integration Architecture

```text
┌─────────────────────────────────────────────────────────┐
│              RESTAURANT (Mediator/Singleton)            │
│  All modules access your managers through this class    │
└──────────────────┬──────────────────┬───────────────────┘
                   │                  │
       ┌───────────┴──────┐   ┌──────┴──────────┐
       │                  │   │                  │
       v                  v   v                  v
┌─────────────┐   ┌─────────────┐   ┌────────────────┐
│FinanceManager│   │StockManager │   │Other Managers  │
│ (Saladin)   │   │ (Saladin)   │   │(Walid,Marwan,  │
└──────┬──────┘   └──────┬──────┘   │ Cranky)        │
       │                  │          └────────────────┘
       │                  │
    Used by           Used by
       │                  │
┌──────┴──────┐   ┌──────┴──────────┐
│   Cashier   │   │StockManagerThread│
│  (Saladin)  │   │    (Saladin)     │
└─────────────┘   └──────────────────┘
       ▲                  ▲
       │                  │
    Called by         Notified by
       │                  │
┌──────┴──────┐   ┌──────┴──────┐
│   Client    │   │    Cook     │
│  (Walid)    │   │  (Cranky)   │
└─────────────┘   └─────────────┘
```

---

## 1. How Other Modules Use YOUR FinanceManager

### A. Client (Walid) → FinanceManager

**When:** Client finishes eating and wants to pay

**How Client calls your code:**
```java
// In Client.java (Walid's code)
public void run() {
    // ... client gets table, orders food, eats ...

    // Pay at cashier
    double billAmount = calculateBill(); // e.g., $25.50
    FinanceManager finance = Restaurant.getInstance().getFinanceManager();
    finance.processPayment(billAmount);

    System.out.println("Client-" + id + " paid $" + billAmount);

    // ... release table ...
}
```

**Your FinanceManager handles this:**
```java
public synchronized void processPayment(double amount) {
    totalRevenue += amount;        // Thread-safe increment
    customersServed++;             // Count customers
    System.out.println("[Finance] Payment processed: $" + amount);
}
```

### B. Cashier (Your Actor) → FinanceManager

**Your own Cashier actor also uses FinanceManager:**
```java
// In Cashier.java (YOUR code)
public void run() {
    FinanceManager finance = Restaurant.getInstance().getFinanceManager();

    while (running) {
        // Wait for customer or simulate
        Thread.sleep(1000);

        double amount = 10.0 + Math.random() * 40.0;
        finance.processPayment(amount); // Uses YOUR manager
    }
}
```

---

## 2. How Other Modules Use YOUR StockManager

### A. Cook (Cranky) → StockManager

**When:** Cook prepares a dish and needs ingredients

**How Cook calls your code:**
```java
// In Cook.java (Cranky's code)
public void run() {
    StockManager stock = Restaurant.getInstance().getStockManager();
    KitchenManager kitchen = Restaurant.getInstance().getKitchenManager();

    while (running) {
        Order order = orderQueue.takeOrder();

        // Get ingredients needed for this dish
        Map<String, Integer> needed = order.getDish().getIngredients();
        // e.g., {"Cheese": 2, "Tomato": 3}

        // Try to consume ingredients from YOUR StockManager
        if (!stock.consumeIngredients(needed)) {
            System.out.println("Cook-" + id + " waiting for stock...");
            // Your StockManager returned false (not enough stock)
            // Your StockManagerThread will replenish
            continue; // Retry
        }

        // Ingredients consumed successfully, continue cooking
        kitchen.acquireEquipment(...);
        Thread.sleep(preparationTime);
        kitchen.releaseEquipment(...);
    }
}
```

**Your StockManager handles this:**
```java
public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
    // Check if enough stock
    for (Map.Entry<String, Integer> entry : needed.entrySet()) {
        if (stock.get(entry.getKey()) < entry.getValue()) {
            notify(); // Wake StockManagerThread
            return false; // Not enough
        }
    }

    // Consume ingredients
    for (Map.Entry<String, Integer> entry : needed.entrySet()) {
        stock.put(entry.getKey(), stock.get(entry.getKey()) - entry.getValue());
    }

    if (isStockLow()) {
        notify(); // Wake StockManagerThread
    }

    return true; // Success
}
```

### B. StockManagerThread (Your Actor) → StockManager

**Your background thread monitors and replenishes:**
```java
// In StockManagerThread.java (YOUR code)
public void run() {
    StockManager stockManager = Restaurant.getInstance().getStockManager();

    while (running) {
        synchronized (stockManager) {
            // Wait until stock is low
            while (!stockManager.isStockLow()) {
                stockManager.wait(); // Sleep until notified
            }

            // Stock low, order delivery
        }

        Thread.sleep(3000); // Simulate delivery

        synchronized (stockManager) {
            stockManager.replenish(); // Replenish all ingredients
            // replenish() calls notifyAll() to wake waiting cooks
        }
    }
}
```

---

## 3. How To Use YOUR Classes in Main.java

Here's how to start the simulation in Main.java using your modules:

```java
package ma.emsi.restaurant;

import ma.emsi.restaurant.actors.*;
import ma.emsi.restaurant.managers.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Restaurant Simulation Starting ===\n");

        // Get the Restaurant singleton (Mediator)
        Restaurant restaurant = Restaurant.getInstance();

        // ===== SALADIN'S MODULES =====

        // 1. Start StockManagerThread (background daemon)
        StockManagerThread stockThread = new StockManagerThread();
        Thread stockDaemon = new Thread(stockThread, "StockManager-Daemon");
        stockDaemon.setDaemon(true); // Background thread
        stockDaemon.start();

        // 2. Start Cashiers (2 cashiers)
        Thread[] cashiers = new Thread[2];
        for (int i = 0; i < 2; i++) {
            Cashier cashier = new Cashier(i + 1);
            cashiers[i] = new Thread(cashier, "Cashier-" + (i + 1));
            cashiers[i].start();
        }

        // 3. Optional: Demonstrate race condition
        FinanceManager finance = restaurant.getFinanceManager();
        System.out.println("\n--- Race Condition Demo ---");
        finance.demonstrateRaceCondition();
        System.out.println("--- End Demo ---\n");

        // ===== OTHER MODULES =====

        // Start Walid's Clients
        Thread[] clients = new Thread[50];
        for (int i = 0; i < 50; i++) {
            boolean isVIP = Math.random() < 0.3; // 30% VIP
            Client client = new Client(i + 1, isVIP);
            clients[i] = new Thread(client, "Client-" + (i + 1));
            clients[i].start();
        }

        // Start Cranky's Servers
        Thread[] servers = new Thread[4];
        for (int i = 0; i < 4; i++) {
            Server server = new Server(i + 1);
            servers[i] = new Thread(server, "Server-" + (i + 1));
            servers[i].start();
        }

        // Start Cranky's Cooks
        Thread[] cooks = new Thread[3];
        for (int i = 0; i < 3; i++) {
            Cook cook = new Cook(i + 1);
            cooks[i] = new Thread(cook, "Cook-" + (i + 1));
            cooks[i].start();
        }

        // ===== SIMULATION RUNTIME =====

        // Run for 30 seconds
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ===== PRINT STATISTICS (Saladin's modules) =====

        System.out.println("\n=== Final Statistics ===");
        System.out.println(finance.getStatistics());
        System.out.println("\n" + restaurant.getStockManager().getStockStatus());

        System.out.println("\n=== Restaurant Simulation Ended ===");
    }
}
```

---

## 4. Access Pattern Summary

### To use FinanceManager from anywhere:
```java
FinanceManager finance = Restaurant.getInstance().getFinanceManager();
finance.processPayment(amount);
```

### To use StockManager from anywhere:
```java
StockManager stock = Restaurant.getInstance().getStockManager();

// Check and consume
Map<String, Integer> needed = new HashMap<>();
needed.put("Cheese", 2);
needed.put("Tomato", 3);

if (stock.consumeIngredients(needed)) {
    // Success, ingredients consumed
} else {
    // Failed, not enough stock
}
```

### To check stock levels (for monitoring/dashboard):
```java
StockManager stock = Restaurant.getInstance().getStockManager();
Map<String, Integer> levels = stock.getStockLevels();

for (Map.Entry<String, Integer> entry : levels.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

### To get finance statistics:
```java
FinanceManager finance = Restaurant.getInstance().getFinanceManager();
System.out.println(finance.getStatistics());
// Output: "Revenue: $1234.56 | Customers: 42 | Avg: $29.39"
```

---

## 5. Thread Safety Guarantees

### FinanceManager
- All methods are `synchronized`
- Safe for multiple cashiers and clients to call concurrently
- No race conditions on totalRevenue

### StockManager
- All methods are `synchronized`
- Safe for multiple cooks and background thread
- Uses `wait()/notify()` for coordination
- `notifyAll()` wakes all waiting cooks after replenishment

---

## 6. Testing Your Integration

### Test 1: Race Condition Demo
```java
public static void testRaceCondition() {
    FinanceManager finance = Restaurant.getInstance().getFinanceManager();
    finance.demonstrateRaceCondition();
    // Will show money loss with unsynchronized version
}
```

### Test 2: Stock Depletion and Replenishment
```java
public static void testStock() {
    StockManager stock = Restaurant.getInstance().getStockManager();

    // Start background thread
    new Thread(new StockManagerThread()).start();

    // Simulate cooks consuming
    Map<String, Integer> recipe = new HashMap<>();
    recipe.put("Cheese", 5);

    for (int i = 0; i < 15; i++) {
        boolean success = stock.consumeIngredients(recipe);
        System.out.println("Attempt " + i + ": " + success);
        Thread.sleep(500);
    }

    // Watch stock replenishment happen automatically
}
```

### Test 3: Multiple Cashiers
```java
public static void testMultipleCashiers() {
    // Start 5 cashiers processing payments
    for (int i = 0; i < 5; i++) {
        new Thread(new Cashier(i)).start();
    }

    Thread.sleep(10000); // Run for 10 seconds

    // Check final revenue
    FinanceManager finance = Restaurant.getInstance().getFinanceManager();
    System.out.println(finance.getStatistics());
}
```

---

## 7. Integration Checklist

- [x] FinanceManager created with synchronized methods
- [x] FinanceManager has race condition demo
- [x] StockManager created with ingredient management
- [x] StockManager uses wait/notify for coordination
- [x] Cashier actor created
- [x] StockManagerThread background daemon created
- [ ] Test with Walid's Client calling processPayment()
- [ ] Test with Cranky's Cook calling consumeIngredients()
- [ ] Verify stock replenishment triggers correctly
- [ ] Verify no race conditions in concurrent scenario
- [ ] Add to Main.java simulation

---

## 8. Key Integration Points

| Your Class | Used By | Method Called | Purpose |
|------------|---------|---------------|---------|
| **FinanceManager** | Client (Walid) | `processPayment()` | Client pays bill |
| **FinanceManager** | Cashier (You) | `processPayment()` | Cashier processes payment |
| **StockManager** | Cook (Cranky) | `consumeIngredients()` | Cook uses ingredients |
| **StockManager** | StockManagerThread (You) | `isStockLow()`, `replenish()` | Auto-replenishment |

---

## Summary

**Your module is accessed through the Restaurant singleton:**
```java
Restaurant.getInstance().getFinanceManager()  // Your FinanceManager
Restaurant.getInstance().getStockManager()    // Your StockManager
```

**Other modules call your methods:**
- Walid's Client → `finance.processPayment(amount)`
- Cranky's Cook → `stock.consumeIngredients(ingredientsMap)`

**Your background thread monitors and replenishes automatically.**

**Everything is thread-safe due to synchronized methods and proper use of wait/notify.**
