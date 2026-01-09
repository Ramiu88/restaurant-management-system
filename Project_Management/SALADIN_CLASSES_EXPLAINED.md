# SALADIN - Your Classes Explained Simply

## Overview of Your 4 Classes

You have **2 Managers** (the brains) and **2 Actors** (the workers):

```text
MANAGERS (The Brains):
├── FinanceManager     → Tracks money (like a cash register)
└── StockManager       → Tracks ingredients (like a warehouse)

ACTORS (The Workers):
├── Cashier            → Takes payments from customers
└── StockManagerThread → Watches warehouse and orders deliveries
```

---

## Class 1: FinanceManager (The Cash Register)

### What It Does
Keeps track of how much money the restaurant has made.

### The Problem It Solves
Imagine 2 cashiers using the same cash register:
- Cashier 1 sees: $100, adds $15 → writes $115
- Cashier 2 sees: $100, adds $20 → writes $120
- **Result:** $120 (but should be $135!)
- **Lost money:** $15

This is called a **RACE CONDITION** - two threads "racing" to update the same variable.

### Your Code Explained

```java
public class FinanceManager {
    // SHARED DATA - Multiple threads access this
    private double totalRevenue = 0.0;
    private int customersServed = 0;

    // THE FIX: synchronized keyword
    public synchronized void processPayment(double amount) {
        totalRevenue += amount;      // Only ONE thread can do this at a time
        customersServed++;           // Protected from race condition

        System.out.println("[Finance] Payment: $" + amount);
    }
}
```

### Key Concepts

**1. `synchronized` keyword**
```java
public synchronized void processPayment(double amount)
```
- When a thread enters this method, it LOCKS the door
- Other threads must WAIT outside
- When done, it UNLOCKS the door
- Next thread can enter

**Think of it like a bathroom:**
- Only ONE person can use it at a time
- Others wait in line
- When you're done, next person goes in

**2. Why we need it**
```java
totalRevenue += amount;  // This is actually 3 steps:
// Step 1: READ totalRevenue
// Step 2: ADD amount
// Step 3: WRITE back to totalRevenue

// Without synchronized, threads can interrupt each other between steps!
```

**3. The demonstration method**
```java
public void processPaymentUNSAFE(double amount) {
    // NO synchronized - shows the problem
    unsafeRevenue += amount;  // Race condition happens here!
}

public void demonstrateRaceCondition() {
    // Creates 10 threads all updating unsafeRevenue
    // Shows how money gets lost without synchronization
}
```

### When Is It Used?
- **Client (Walid)** calls it when customer pays bill
- **Cashier (your actor)** calls it to process payments

```java
// Client pays $25.50
FinanceManager finance = Restaurant.getInstance().getFinanceManager();
finance.processPayment(25.50);
```

---

## Class 2: StockManager (The Warehouse)

### What It Does
Keeps track of ingredients and automatically reorders when low.

### The Problem It Solves
- Cooks need ingredients to prepare dishes
- If ingredients run out, cooks must wait
- A background worker watches the warehouse
- When stock is low, it orders more automatically

### Your Code Explained

```java
public class StockManager {
    // SHARED DATA - The warehouse
    private Map<String, Integer> stock;
    private int lowThreshold = 10;

    public StockManager() {
        // Initialize warehouse
        stock.put("Tomato", 50);
        stock.put("Cheese", 50);
        stock.put("Meat", 50);
        // ... etc
    }
}
```

### Key Methods

**1. consumeIngredients() - Cook uses this**
```java
public synchronized boolean consumeIngredients(Map<String, Integer> needed) {
    // STEP 1: Check if we have enough
    for (Map.Entry<String, Integer> entry : needed.entrySet()) {
        String ingredient = entry.getKey();
        int amount = entry.getValue();

        if (stock.get(ingredient) < amount) {
            notify();  // Wake up background thread!
            return false;  // Sorry, not enough
        }
    }

    // STEP 2: We have enough, take it from warehouse
    for (Map.Entry<String, Integer> entry : needed.entrySet()) {
        String ingredient = entry.getKey();
        int amount = entry.getValue();
        stock.put(ingredient, stock.get(ingredient) - amount);
    }

    // STEP 3: Check if now low
    if (isStockLow()) {
        notify();  // Wake up background thread!
    }

    return true;  // Success!
}
```

**Example usage by Cook:**
```java
// Cook needs ingredients for pizza
Map<String, Integer> recipe = new HashMap<>();
recipe.put("Cheese", 2);    // Need 2 cheese
recipe.put("Tomato", 3);    // Need 3 tomatoes
recipe.put("Dough", 1);     // Need 1 dough

if (stockManager.consumeIngredients(recipe)) {
    System.out.println("Got ingredients, cooking pizza!");
} else {
    System.out.println("Not enough ingredients, waiting...");
}
```

**2. replenish() - Background thread uses this**
```java
public synchronized void replenish() {
    // Add 50 to each ingredient
    for (String ingredient : stock.keySet()) {
        int current = stock.get(ingredient);
        stock.put(ingredient, current + 50);
    }

    notifyAll();  // Wake ALL waiting cooks!
}
```

**3. isStockLow() - Background thread checks this**
```java
public synchronized boolean isStockLow() {
    // Check if ANY ingredient is below 10
    for (int level : stock.values()) {
        if (level < 10) {
            return true;  // Yes, stock is low!
        }
    }
    return false;  // Nope, we're good
}
```

### Key Concepts

**1. wait() and notify()**

Think of it like a sleeping guard:

```java
// Background thread
synchronized(stockManager) {
    while (!stockManager.isStockLow()) {
        wait();  // 😴 Guard sleeps
    }
    // Guard woke up! Stock must be low
}

// Cook thread
synchronized(stockManager) {
    if (notEnough) {
        notify();  // 📢 Wake up the guard!
    }
}
```

**Visual representation:**
```text
TIME 0: Stock is OK
        Background thread → wait() → 😴 sleeping

TIME 1: Cook uses ingredients
        Stock still OK → 😴 still sleeping

TIME 2: Cook uses more ingredients
        Stock now LOW → notify() → 👁️ Guard wakes up!

TIME 3: Guard orders delivery
        wait 3 seconds... 🚚

TIME 4: Delivery arrives
        replenish() → notifyAll() → 📢 Wake all cooks!
```

**2. notifyAll() vs notify()**

```java
notify();     // Wakes ONE waiting thread
notifyAll();  // Wakes ALL waiting threads

// Use notify() when only one thread needs to wake up
if (isStockLow()) {
    notify();  // Wake stock manager (only one)
}

// Use notifyAll() when multiple threads might be waiting
replenish();
notifyAll();  // Wake ALL cooks who were waiting
```

### When Is It Used?
- **Cook (Cranky)** calls consumeIngredients() before cooking
- **StockManagerThread (your background thread)** monitors and replenishes

---

## Class 3: Cashier (The Payment Processor)

### What It Does
A worker thread that processes customer payments independently.

### Your Code Explained

```java
public class Cashier implements Runnable {
    private int cashierId;
    private volatile boolean running = true;

    @Override
    public void run() {
        // This runs in its own thread
        while (running) {
            // Wait for customer (simulate)
            Thread.sleep(1000);

            // Random payment $10-$50
            double amount = 10.0 + (Math.random() * 40.0);

            // Process through FinanceManager
            Restaurant.getInstance()
                      .getFinanceManager()
                      .processPayment(amount);
        }
    }
}
```

### Key Concepts

**1. implements Runnable**
```java
public class Cashier implements Runnable {
    @Override
    public void run() {
        // This method runs in separate thread
    }
}
```

This means Cashier CAN run in its own thread:
```java
Cashier cashier = new Cashier(1);
Thread thread = new Thread(cashier);  // Wrap in Thread
thread.start();  // Start the thread → run() executes
```

**2. volatile boolean running**
```java
private volatile boolean running = true;
```

`volatile` means:
- Multiple threads can see changes to this variable
- When one thread changes `running`, others see it immediately
- Used for stopping the thread gracefully

```java
public void stop() {
    running = false;  // Thread will see this and exit loop
}
```

**3. The run() loop**
```java
while (running) {  // Keep going until stopped
    Thread.sleep(1000);  // Wait 1 second
    // Process payment
}
```

### How It's Used

```java
// In Main.java
Cashier cashier1 = new Cashier(1);
Cashier cashier2 = new Cashier(2);

Thread t1 = new Thread(cashier1, "Cashier-1");
Thread t2 = new Thread(cashier2, "Cashier-2");

t1.start();  // Cashier 1 starts working
t2.start();  // Cashier 2 starts working

// Both process payments independently in parallel!

// Later...
cashier1.stop();  // Stop cashier 1
cashier2.stop();  // Stop cashier 2
```

---

## Class 4: StockManagerThread (The Warehouse Watcher)

### What It Does
A background thread that continuously watches the warehouse and orders deliveries when stock is low.

### Your Code Explained

```java
public class StockManagerThread implements Runnable {
    private volatile boolean running = true;

    @Override
    public void run() {
        StockManager stockManager = Restaurant.getInstance().getStockManager();

        while (running) {
            // STEP 1: Wait until stock is low
            synchronized(stockManager) {
                while (!stockManager.isStockLow()) {
                    stockManager.wait();  // 😴 Sleep
                }
                // Woke up! Stock must be low
            }

            // STEP 2: Order delivery (outside synchronized!)
            System.out.println("Ordering delivery...");
            Thread.sleep(3000);  // Simulate 3 second delivery

            // STEP 3: Delivery arrived, replenish
            synchronized(stockManager) {
                stockManager.replenish();
                // replenish() calls notifyAll() to wake cooks
            }
        }
    }
}
```

### Key Concepts

**1. wait() inside while loop**
```java
synchronized(stockManager) {
    while (!stockManager.isStockLow()) {  // while, not if!
        stockManager.wait();
    }
}
```

**Why `while` and not `if`?**

```java
// WRONG:
if (!stockManager.isStockLow()) {
    wait();
}
// Problem: Might wake up spuriously (randomly) and skip the check!

// CORRECT:
while (!stockManager.isStockLow()) {
    wait();
}
// Always re-checks condition after waking up
```

**2. Synchronization strategy**
```java
// Synchronized when checking/waiting
synchronized(stockManager) {
    while (!isLow()) wait();
}

// NOT synchronized during delivery
Thread.sleep(3000);  // Don't hold lock while waiting!

// Synchronized when replenishing
synchronized(stockManager) {
    replenish();
}
```

**Why?** We don't want to hold the lock during the 3-second delivery simulation. Other threads (cooks) need to access the StockManager!

**3. Daemon thread**
```java
// In Main.java
Thread daemon = new Thread(new StockManagerThread());
daemon.setDaemon(true);  // Mark as daemon
daemon.start();

// Daemon threads automatically stop when main program ends
// Normal threads keep program alive even if main() finishes
```

### Complete Flow Example

```text
TIME 0:
StockManagerThread starts
→ synchronized(stockManager) { wait(); } → 😴

TIME 1:
Cook1 uses 5 cheese → Stock: Cheese=45
→ Still OK, thread sleeps → 😴

TIME 2:
Cook2 uses 5 cheese → Stock: Cheese=40
Cook3 uses 5 cheese → Stock: Cheese=35
...
Cook8 uses 5 cheese → Stock: Cheese=5 (LOW!)
→ consumeIngredients() calls notify() → 📢

TIME 3:
StockManagerThread wakes up → 👁️
→ Checks isStockLow() → TRUE
→ "Ordering delivery..."
→ Thread.sleep(3000) → 🚚

TIME 6:
Delivery arrives!
→ synchronized(stockManager) { replenish(); }
→ Stock: Cheese=55, Tomato=60, etc.
→ notifyAll() → 📢 Wake all waiting cooks
→ Goes back to wait() → 😴
```

---

## How They All Work Together

```text
1. CLIENT arrives and eats
        ↓
2. CLIENT pays bill
        ↓
   [Cashier] or [Client] calls FinanceManager.processPayment()
        ↓
   [FinanceManager] synchronized: totalRevenue += amount ✓

3. COOK takes order
        ↓
   [Cook] calls StockManager.consumeIngredients()
        ↓
   [StockManager] synchronized: Check stock
        ↓
        ├─→ Not enough? → notify() → Wake StockManagerThread
        └─→ Enough? → Consume ingredients

4. [StockManagerThread] (background)
        ↓
   wait() until notified → 😴
        ↓
   Wakes up → isStockLow()? → YES
        ↓
   Order delivery (sleep 3s) → 🚚
        ↓
   replenish() + notifyAll() → Wake all cooks
        ↓
   Back to wait() → 😴
```

---

## Common Questions

### Q1: Why synchronized?
**A:** Without it, multiple threads can corrupt shared data (like totalRevenue).

### Q2: What's the difference between notify() and notifyAll()?
**A:**
- `notify()` wakes ONE waiting thread
- `notifyAll()` wakes ALL waiting threads

### Q3: Why wait() inside a while loop?
**A:** Because threads can wake up spuriously (randomly). Always re-check the condition.

### Q4: What's a daemon thread?
**A:** Background thread that automatically stops when program ends.

### Q5: Why volatile for running?
**A:** So all threads see changes to `running` immediately.

---

## Summary

| Class | Purpose | Key Method | Pattern |
|-------|---------|------------|---------|
| **FinanceManager** | Track money | `processPayment()` | Monitor |
| **StockManager** | Track ingredients | `consumeIngredients()` | Monitor + Producer-Consumer |
| **Cashier** | Process payments | `run()` | Active Object |
| **StockManagerThread** | Auto-replenish | `run()` | Active Object + Daemon |

All working together to create a thread-safe restaurant simulation!
