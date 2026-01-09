# SALADIN - Design Patterns Explained

## Your Classes Overview

You are responsible for **4 classes** implementing **3 major design patterns**:

```text
YOUR MODULE (Saladin):
├── managers/
│   ├── FinanceManager.java        [Monitor Pattern]
│   └── StockManager.java          [Monitor Pattern + Producer-Consumer]
└── actors/
    ├── Cashier.java               [Active Object Pattern]
    └── StockManagerThread.java    [Active Object Pattern + Daemon Thread]
```

---

## Pattern 1: Monitor Pattern

### What is the Monitor Pattern?

The **Monitor Pattern** encapsulates:
1. **Shared mutable state** (data that multiple threads access)
2. **Synchronization mechanism** (locks to protect that data)
3. **Condition variables** (wait/notify for coordination)

**Key Idea:** Instead of having threads manually lock shared variables, wrap everything in a class with synchronized methods.

### Pattern Structure

```java
public class Monitor {
    private SharedState state;  // Protected data

    public synchronized void operation1() {
        // Modify state safely
        // Only ONE thread can execute this at a time
    }

    public synchronized void operation2() {
        // Access state safely
    }
}
```

### Your Implementation: FinanceManager (Monitor)

```java
public class FinanceManager {
    // ===== SHARED STATE (protected by monitor) =====
    private double totalRevenue = 0.0;        // Shared counter
    private int customersServed = 0;          // Shared counter

    // ===== SYNCHRONIZED OPERATIONS =====
    public synchronized void processPayment(double amount) {
        // CRITICAL SECTION - only one thread at a time
        totalRevenue += amount;      // Read-Modify-Write operation
        customersServed++;           // Must be atomic
    }

    public synchronized double getTotalRevenue() {
        // CRITICAL SECTION
        return totalRevenue;         // Thread-safe read
    }
}
```

**Why synchronized?**
```text
WITHOUT synchronized:
Thread1: Read totalRevenue (100)
Thread2: Read totalRevenue (100)  ← Both read same value!
Thread1: Write totalRevenue (115)
Thread2: Write totalRevenue (120)  ← Overwrites Thread1!
Result: 120 (should be 135) - Money lost!

WITH synchronized:
Thread1: Lock → Read (100) → Write (115) → Unlock
Thread2: Lock → Read (115) → Write (135) → Unlock  ← Correct!
Result: 135 ✓
```

**Pattern Elements in FinanceManager:**
- **Monitor Object:** FinanceManager itself
- **Shared State:** totalRevenue, customersServed
- **Synchronized Methods:** processPayment(), getTotalRevenue()
- **Mutual Exclusion:** Only one thread can execute synchronized methods at a time

---

### Your Implementation: StockManager (Monitor + Condition Variables)

```java
public class StockManager {
    // ===== SHARED STATE =====
    private Map<String, Integer> stock;  // Ingredient levels

    // ===== SYNCHRONIZED WITH CONDITION WAIT/NOTIFY =====
    public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
        // Check stock levels
        if (notEnoughStock) {
            notify();    // Wake background thread
            return false;
        }

        // Consume ingredients
        // ...

        if (isStockLow()) {
            notify();    // Wake background thread
        }

        return true;
    }

    public synchronized void replenish() {
        // Add ingredients
        // ...
        notifyAll();     // Wake ALL waiting cooks
    }
}
```

**Pattern Elements in StockManager:**
- **Monitor Object:** StockManager itself
- **Shared State:** stock Map
- **Synchronized Methods:** consumeIngredients(), replenish()
- **Condition Variables:** wait()/notify()/notifyAll()

**Wait/Notify Explained:**

```text
SCENARIO: Stock runs low

Cook Thread:
1. Calls consumeIngredients()
2. Not enough stock
3. StockManager calls notify()  ← Wakes background thread
4. Returns false
5. Cook retries later

StockManagerThread (background):
1. synchronized(stockManager) { wait(); }  ← Sleeping
2. notify() wakes it up  ← Cook woke us!
3. Checks isStockLow() → true
4. Simulates delivery (sleep 3s)
5. Calls replenish()
6. replenish() calls notifyAll()  ← Wakes all cooks
7. Goes back to waiting
```

---

## Pattern 2: Producer-Consumer Pattern

### What is Producer-Consumer?

**Producers** create work items and add to shared queue.
**Consumers** take work items from queue and process them.

**Challenge:** Coordinate when queue is empty (consumers wait) or full (producers wait).

### Pattern Structure

```java
public class SharedQueue {
    private Queue<Item> queue;

    // Producer adds item
    public synchronized void add(Item item) {
        queue.add(item);
        notify();  // Wake ONE consumer
    }

    // Consumer takes item
    public synchronized Item take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Sleep until producer adds item
        }
        return queue.poll();
    }
}
```

### Your Implementation: StockManager (Special Case)

Your StockManager implements a **modified Producer-Consumer**:

```text
PRODUCERS: Cooks (consume ingredients → make stock low)
CONSUMER:  StockManagerThread (detects low stock → replenishes)
QUEUE:     The stock Map itself (tracked by isStockLow())
```

**Flow:**

```java
// PRODUCERS (Cooks) - Produce "work" by depleting stock
public synchronized boolean consumeIngredients(...) {
    // Consume ingredients
    stock.put("Cheese", stock.get("Cheese") - 2);

    if (isStockLow()) {
        notify();  // Producer signals consumer
    }
}

// CONSUMER (StockManagerThread) - Consumes "work" by replenishing
public void run() {
    synchronized(stockManager) {
        while (!stockManager.isStockLow()) {
            wait();  // Consumer waits for producers
        }
        // Stock low, replenish it
    }
}
```

**Pattern Elements:**
- **Producers:** Cooks calling consumeIngredients()
- **Consumer:** StockManagerThread
- **Shared Resource:** stock Map
- **Signaling:** notify() when low, notifyAll() when replenished

---

## Pattern 3: Active Object Pattern

### What is Active Object?

The **Active Object Pattern** decouples method execution from method invocation.

**Key Idea:** Create a separate thread that executes methods asynchronously.

### Pattern Structure

```java
public class ActiveObject implements Runnable {
    private boolean running = true;

    @Override
    public void run() {
        // This runs in its own thread
        while (running) {
            // Do work asynchronously
        }
    }

    public void stop() {
        running = false;  // Signal thread to stop
    }
}
```

### Your Implementation: Cashier (Active Object)

```java
public class Cashier implements Runnable {
    private volatile boolean running = true;

    @Override
    public void run() {
        // Runs in separate thread
        while (running) {
            // Process payments asynchronously
            Thread.sleep(1000);
            financeManager.processPayment(amount);
        }
    }

    public void stop() {
        running = false;  // Graceful shutdown
    }
}
```

**Pattern Elements:**
- **Active Object:** Cashier
- **Scheduler:** Java Thread
- **Activation Queue:** Implicit (processes payments continuously)
- **Method Invocation:** processPayment() called from thread

**Usage:**
```java
// Create active object
Cashier cashier = new Cashier(1);

// Start in separate thread
Thread thread = new Thread(cashier);
thread.start();  // Now runs independently

// Later, stop it
cashier.stop();
```

---

### Your Implementation: StockManagerThread (Active Object + Daemon)

```java
public class StockManagerThread implements Runnable {
    private volatile boolean running = true;

    @Override
    public void run() {
        // Runs in BACKGROUND thread
        while (running) {
            synchronized(stockManager) {
                while (!stockManager.isStockLow()) {
                    wait();  // Sleep until notified
                }
                // Replenish stock
            }
        }
    }
}
```

**Special Feature: Daemon Thread**

```java
// In Main.java
Thread stockDaemon = new Thread(new StockManagerThread());
stockDaemon.setDaemon(true);  // Background thread
stockDaemon.start();

// Daemon threads automatically terminate when all
// non-daemon threads finish
```

**Pattern Elements:**
- **Active Object:** StockManagerThread
- **Scheduler:** Java Thread (daemon)
- **Activation:** Triggered by notify() from cooks
- **Method Invocation:** replenish() called from background

---

## Pattern 4: Singleton Pattern (Used by Restaurant)

You don't implement this, but you USE it to access managers.

### What is Singleton?

Ensures only **ONE instance** of a class exists in the entire application.

```java
public class Restaurant {
    private static Restaurant instance;  // Single instance

    private Restaurant() {
        // Private constructor - can't create with 'new'
    }

    public static synchronized Restaurant getInstance() {
        if (instance == null) {
            instance = new Restaurant();
        }
        return instance;  // Always same instance
    }
}
```

**Your Usage:**
```java
// Anywhere in code:
Restaurant restaurant = Restaurant.getInstance();  // Same instance
FinanceManager finance = restaurant.getFinanceManager();  // Same manager
```

**Why?** All threads must share the SAME FinanceManager and StockManager.

---

## Summary of Patterns in YOUR Module

| Class | Pattern | Purpose |
|-------|---------|---------|
| **FinanceManager** | Monitor | Thread-safe revenue counter |
| **StockManager** | Monitor + Producer-Consumer | Thread-safe stock with auto-replenishment |
| **Cashier** | Active Object | Independent payment processor |
| **StockManagerThread** | Active Object + Daemon | Background stock monitor |

---

## Key Concepts You're Demonstrating

### 1. Race Condition (FinanceManager)
**Problem:** Multiple threads updating shared variable without synchronization
**Solution:** `synchronized` keyword makes operations atomic

### 2. Wait/Notify (StockManager)
**Problem:** How to coordinate between threads (cooks need stock, thread replenishes)
**Solution:** `wait()` to sleep, `notify()` to wake, `notifyAll()` to wake all

### 3. Critical Section (Both Managers)
**Problem:** Code that accesses shared state must be protected
**Solution:** `synchronized` methods ensure only one thread executes at a time

### 4. Background Thread (StockManagerThread)
**Problem:** Need continuous monitoring without blocking main threads
**Solution:** Daemon thread runs in background, wakes on signal

---

## How Patterns Work Together

```text
CLIENT (Walid) pays bill
       │
       v
   CASHIER (your active object - separate thread)
       │
       v
   FinanceManager.processPayment() (MONITOR - synchronized)
       │
       v
   totalRevenue += amount (CRITICAL SECTION - atomic operation)


COOK (Cranky) prepares dish
       │
       v
   StockManager.consumeIngredients() (MONITOR - synchronized)
       │
       ├──> Not enough → notify() ──────┐
       │                                 v
       └──> Enough → consume      StockManagerThread
                                        │ (ACTIVE OBJECT - background)
                                        │
                                  wait() until signaled
                                        │
                                  isStockLow() → true
                                        │
                                  replenish() (MONITOR)
                                        │
                                  notifyAll() ────> Wakes all cooks
```

---

## Testing Each Pattern

### Test Monitor Pattern (FinanceManager)
```java
FinanceManager finance = new FinanceManager();
finance.demonstrateRaceCondition();  // Shows race condition
// Then test with synchronized version
```

### Test Producer-Consumer (StockManager)
```java
// Start consumer (background thread)
new Thread(new StockManagerThread()).start();

// Producers (cooks) consume ingredients
for (int i = 0; i < 20; i++) {
    stockManager.consumeIngredients(recipe);
}

// Watch automatic replenishment happen
```

### Test Active Object (Cashier)
```java
Cashier cashier = new Cashier(1);
Thread thread = new Thread(cashier);
thread.start();  // Processes payments independently

Thread.sleep(5000);  // Let it run

cashier.stop();  // Graceful shutdown
```

---

## Questions to Understand Patterns

1. **Why is processPayment() synchronized?**
   - Because totalRevenue is shared by multiple cashiers/clients
   - Without sync, race condition loses money

2. **Why does StockManager use wait/notify?**
   - Background thread should sleep when stock is OK (not busy-wait)
   - Cooks should wake it when stock gets low

3. **Why is StockManagerThread a daemon?**
   - It's a background service, not primary work
   - Should terminate automatically when restaurant closes

4. **Why does replenish() call notifyAll() not notify()?**
   - Multiple cooks might be waiting for stock
   - notifyAll() wakes ALL of them (not just one)

---

Your module demonstrates advanced concurrency patterns used in real-world applications like databases, web servers, and operating systems!
