# SALADIN - Task Relationships & Management Architecture

## Overall Architecture: Mediator Pattern

The **management approach** for the entire system is the **Mediator Pattern** with the `Restaurant` class as the central coordinator.

```text
┌─────────────────────────────────────────────────────────────┐
│                     RESTAURANT                              │
│                    (MEDIATOR/HUB)                           │
│                                                             │
│  "Mother Class" - Singleton that holds all managers        │
│  All actors access managers THROUGH this class             │
│  No direct communication between modules                    │
└───────┬─────────┬──────────┬──────────┬───────────────────┘
        │         │          │          │
        │         │          │          │
   ┌────▼────┐ ┌─▼──────┐ ┌─▼───────┐ ┌▼──────────┐
   │ WALID   │ │ MARWAN │ │ CRANKY  │ │ SALADIN   │
   │ Tables  │ │Equipmt │ │ Orders  │ │Finance+   │
   │ Manager │ │Manager │ │ Queue   │ │Stock      │
   └────┬────┘ └────┬───┘ └────┬────┘ └┬─────────┬┘
        │           │          │        │         │
        │           │          │        │         │
   Used by     Used by    Used by   Used by   Used by
        │           │          │        │         │
        v           v          v        v         v
    Client      Cook        Cook    Client   Cook &
                                            StockThread
```

## Why Mediator Pattern?

**Without Mediator (BAD):**
```java
// Clients would need to know about everything
public class Client {
    private TableManager tables;
    private OrderQueue orders;
    private FinanceManager finance;
    private StockManager stock;
    // TOO MANY DEPENDENCIES!
}
```

**With Mediator (GOOD):**
```java
// Clients only know about Restaurant
public class Client {
    Restaurant restaurant = Restaurant.getInstance();
    // Get what you need when you need it
    restaurant.getTableManager();
    restaurant.getFinanceManager();
}
```

---

## Your Position in the System

```text
===== RESTAURANT SIMULATION FLOW =====

1. CLIENT ARRIVES (Walid)
   ├─> Requests table from TableManager
   └─> Waits or gets seated

2. CLIENT ORDERS (Cranky)
   ├─> Server takes order
   └─> Adds to OrderQueue

3. COOK PREPARES (Cranky + Marwan + YOU)
   ├─> Takes order from OrderQueue
   ├─> Checks YOUR StockManager ◄────── YOU ARE HERE!
   │   ├─> consumeIngredients()
   │   └─> If low, YOUR StockManagerThread replenishes
   ├─> Acquires equipment (Marwan)
   └─> Cooks dish

4. CLIENT EATS (Walid)
   └─> Simulated with sleep()

5. CLIENT PAYS (Walid + YOU)
   ├─> Calls YOUR FinanceManager ◄────── YOU ARE HERE!
   └─> processPayment()

6. CLIENT LEAVES (Walid)
   └─> Releases table
```

---

## Task Dependency Graph

```text
                    START SIMULATION
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        v                  v                  v
    ┌───────┐         ┌────────┐        ┌─────────┐
    │WALID  │         │CRANKY  │        │SALADIN  │
    │Tables │         │Orders  │        │Finance  │
    └───┬───┘         └───┬────┘        │& Stock  │
        │                 │              └────┬────┘
        │ Creates         │ Creates           │ Creates
        │ Client          │ Server            │ Cashier &
        │ Thread          │ Cook              │ StockThread
        │                 │ Thread            │
        v                 v                   v
    Client.run()      Cook.run()         Cashier.run()
        │                 │              StockThread.run()
        │                 │                   │
        │                 │                   │
   ┌────┴─────┐     ┌────┴──────┐      ┌─────┴─────┐
   │ USES:    │     │ USES:     │      │ USES:     │
   │          │     │           │      │           │
   │ • Table  │     │ • Order   │      │ • Finance │
   │   Manager│     │   Queue   │      │   Manager │
   │          │     │ • Stock ──┼──────┼─► (YOURS) │
   │ • Finance├─────┼─► Manager │      │           │
   │   Manager│     │   (YOURS) │      │ • Stock   │
   │  (YOURS) │     │ • Kitchen │      │   Manager │
   │          │     │   Manager │      │  (YOURS)  │
   └──────────┘     │ (Marwan)  │      └───────────┘
                    └───────────┘
```

---

## Direct Dependencies on YOUR Code

### 1. Walid's Client → YOUR FinanceManager

**Dependency:** Client MUST call your FinanceManager to pay

```java
// In Client.java (Walid writes this)
public void run() {
    // ... get table, order, eat ...

    // PAY - Depends on YOUR code
    FinanceManager finance = Restaurant.getInstance().getFinanceManager();
    finance.processPayment(billAmount);  // CALLS YOUR METHOD

    // ... leave ...
}
```

**What Walid needs from YOU:**
- `processPayment(double amount)` method exists
- Method is thread-safe (synchronized)
- Method handles concurrent calls from multiple clients

**Integration Contract:**
```java
// YOUR responsibility
public synchronized void processPayment(double amount) {
    // Implementation MUST be thread-safe
    totalRevenue += amount;
    customersServed++;
}
```

---

### 2. Cranky's Cook → YOUR StockManager

**Dependency:** Cook MUST check your StockManager before cooking

```java
// In Cook.java (Cranky writes this)
public void run() {
    while (running) {
        Order order = orderQueue.takeOrder();

        // CHECK STOCK - Depends on YOUR code
        StockManager stock = Restaurant.getInstance().getStockManager();
        Map<String, Integer> needed = order.getDish().getIngredients();

        if (!stock.consumeIngredients(needed)) {  // CALLS YOUR METHOD
            // Not enough stock, wait or retry
            continue;
        }

        // Stock consumed successfully, cook the dish
        kitchenManager.acquireEquipment(...);
        Thread.sleep(preparationTime);
        kitchenManager.releaseEquipment(...);
    }
}
```

**What Cranky needs from YOU:**
- `consumeIngredients(Map<String, Integer>)` method exists
- Returns `true` if consumed, `false` if not enough
- Automatically triggers replenishment when low
- Thread-safe for multiple cooks

**Integration Contract:**
```java
// YOUR responsibility
public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
    // Check all ingredients available
    // If not, notify background thread and return false
    // If yes, consume and return true
}
```

---

### 3. YOUR StockManagerThread → YOUR StockManager

**Dependency:** Background thread monitors YOUR StockManager

```java
// In StockManagerThread.java (YOU write this)
public void run() {
    StockManager stockManager = Restaurant.getInstance().getStockManager();

    while (running) {
        synchronized(stockManager) {
            // Wait until stock low
            while (!stockManager.isStockLow()) {
                stockManager.wait();  // Sleep
            }
        }

        Thread.sleep(3000);  // Delivery time

        synchronized(stockManager) {
            stockManager.replenish();  // Replenish stock
        }
    }
}
```

**Self-dependency - both classes are YOURS!**

---

## Task Interaction Timeline

Here's how your code is called during ONE client's visit:

```text
TIME  │ ACTOR         │ ACTION                    │ CALLS YOUR CODE
──────┼───────────────┼───────────────────────────┼─────────────────────
00:00 │ Client (W)    │ Arrives                   │ (none)
00:01 │ Client (W)    │ Gets table                │ (none)
00:02 │ Client (W)    │ Orders food               │ (none)
00:03 │ Server (C)    │ Takes order               │ (none)
00:04 │ Cook (C)      │ Takes order from queue    │ (none)
00:05 │ Cook (C)      │ CHECK STOCK               │ stock.consumeIngredients() ◄── YOU
      │               │                           │
      │               │ ┌─ If return FALSE:       │
      │               │ │  Cook waits             │
      │               │ │  YOUR StockThread       │
      │               │ │  detects low stock      │
      │               │ │  calls replenish()      │ ◄── YOU
      │               │ │  Cook retries           │
      │               │ └─                        │
      │               │                           │
00:06 │ Cook (C)      │ Acquires equipment        │ (none - Marwan)
00:09 │ Cook (C)      │ Finishes cooking          │ (none)
00:10 │ Client (W)    │ Eats food                 │ (none)
00:15 │ Client (W)    │ PAYS BILL                 │ finance.processPayment() ◄── YOU
00:16 │ Client (W)    │ Leaves                    │ (none)
```

**Your code is called 2 times per customer:**
1. Stock check before cooking
2. Payment when leaving

**Plus continuous background monitoring by StockManagerThread**

---

## Module Communication Rules

### Rule 1: No Direct Actor-to-Actor Communication

```java
// WRONG - Actors don't talk directly
public class Client {
    private Cook cook;  // NO!
    cook.prepareDish(); // NO!
}

// CORRECT - Go through Managers
public class Client {
    OrderQueue orders = Restaurant.getInstance().getOrderQueue();
    orders.addOrder(myOrder);  // Cook will pick it up
}
```

### Rule 2: All Access Through Restaurant Singleton

```java
// ALWAYS use this pattern:
Restaurant restaurant = Restaurant.getInstance();
YourManager manager = restaurant.getYourManager();
manager.yourMethod();
```

### Rule 3: Managers Are Thread-Safe

All YOUR managers MUST be thread-safe:
- Use `synchronized` methods
- Use proper wait/notify
- No data races

---

## Testing Integration with Others

### Test 1: Client → FinanceManager (Walid + YOU)

```java
public static void testClientPayment() {
    Restaurant restaurant = Restaurant.getInstance();

    // Simulate Walid's client
    Runnable clientBehavior = () -> {
        // Client finishes eating, pays
        double bill = 25.50;
        restaurant.getFinanceManager().processPayment(bill);
    };

    // Run 10 clients
    for (int i = 0; i < 10; i++) {
        new Thread(clientBehavior, "Client-" + i).start();
    }

    // Check final revenue
    Thread.sleep(2000);
    System.out.println(restaurant.getFinanceManager().getStatistics());
}
```

### Test 2: Cook → StockManager (Cranky + YOU)

```java
public static void testCookStock() {
    Restaurant restaurant = Restaurant.getInstance();
    StockManager stock = restaurant.getStockManager();

    // Start background thread
    new Thread(new StockManagerThread()).start();

    // Simulate Cranky's cook
    Runnable cookBehavior = () -> {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("Cheese", 2);
        recipe.put("Tomato", 3);

        for (int i = 0; i < 10; i++) {
            if (stock.consumeIngredients(recipe)) {
                System.out.println("Cook prepared dish");
            } else {
                System.out.println("Cook waiting for stock");
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }
    };

    // Run 3 cooks
    for (int i = 0; i < 3; i++) {
        new Thread(cookBehavior, "Cook-" + i).start();
    }
}
```

---

## Summary

### Management Architecture
- **Pattern:** Mediator (Restaurant class)
- **Purpose:** Centralize communication between modules
- **Benefit:** Modules don't need to know about each other

### Your Role in System
1. **FinanceManager:** Called by Client (Walid) for payments
2. **StockManager:** Called by Cook (Cranky) for ingredients
3. **Cashier:** Active object that uses FinanceManager
4. **StockManagerThread:** Background daemon that monitors StockManager

### Dependencies on You
- **Walid needs:** processPayment() to work
- **Cranky needs:** consumeIngredients() to work
- **Both need:** Thread-safe implementation

### Integration Points
```java
// Walid calls:
Restaurant.getInstance().getFinanceManager().processPayment(amount);

// Cranky calls:
Restaurant.getInstance().getStockManager().consumeIngredients(ingredients);
```

### Your Deliverables
- [x] FinanceManager with synchronized methods
- [x] StockManager with wait/notify
- [x] Cashier actor
- [x] StockManagerThread daemon
- [ ] Integration testing with Walid and Cranky
