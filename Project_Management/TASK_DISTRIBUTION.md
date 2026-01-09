# TASK DISTRIBUTION - 4 ENGINEERS

## Architecture Overview

The system follows the **Mediator Pattern** with the `Restaurant` class (Singleton) as the central mediator coordinating all Monitor objects. Each engineer will implement specific monitors and their associated actors.

---

## ENGINEER 1: Table Management System

### Responsibilities
- **TableManager** (Monitor class)
- **Client** (Actor/Thread)
- **Table** (Entity)

### Implementation Tasks

#### 1. Entity: Table.java
```java
public class Table {
    private final int id;
    private final boolean isVIP;
    private volatile boolean occupied;
    private ReentrantLock vipLock; // Only for VIP tables
}
```

#### 2. Manager: TableManager.java
**Key responsibilities:**
- Maintain array of 15 tables (10 normal, 5 VIP)
- Implement waiting queue for clients
- Handle VIP priority with `tryLock(30, TimeUnit.SECONDS)`
- Use `wait()/notifyAll()` for normal table assignment

**Critical methods:**
```java
public synchronized Table requestTable(boolean isVIP);
public synchronized void releaseTable(Table table);
private void addToWaitingQueue(Client client);
```

#### 3. Actor: Client.java
**Lifecycle:**
1. Request table from TableManager
2. Wait if no table available
3. Sit and browse menu (sleep 1-2s)
4. Call server to place order
5. Wait for food and eat (sleep 3-5s)
6. Pay at cashier
7. Release table

**Synchronization:**
- Use `wait()` when no tables available
- Wake up on `notifyAll()` when table freed

---

## ENGINEER 2: Order Queue System

### Responsibilities
- **OrderQueue** (Monitor class)
- **Server** (Actor/Thread)
- **Cook** (Actor/Thread)
- **Chef** (Actor/Thread - priority consumer)
- **Order** (Entity)
- **Dish** (Entity)

### Implementation Tasks

#### 1. Entity: Order.java
```java
public class Order implements Comparable<Order> {
    private final int orderId;
    private final Dish dish;
    private final int priority; // 1=URGENT, 2=NORMAL, 3=SLOW

    @Override
    public int compareTo(Order other) {
        return Integer.compare(this.priority, other.priority);
    }
}
```

#### 2. Entity: Dish.java
```java
public class Dish {
    private final String name;
    private final int preparationTime;
    private final List<String> requiredEquipment;
    private final Map<String, Integer> ingredients;
}
```

#### 3. Manager: OrderQueue.java
**Producer-Consumer Pattern:**
- Use `PriorityQueue<Order>` for automatic sorting
- Servers are producers, Cooks/Chef are consumers

**Critical methods:**
```java
public synchronized void addOrder(Order order);
public synchronized Order takeOrder() throws InterruptedException;
public synchronized boolean isEmpty();
```

**Synchronization:**
- `notify()` when order added (wake ONE cook)
- `wait()` when queue empty (cooks sleep)

#### 4. Actors
- **Server:** Takes client orders, adds to OrderQueue
- **Cook:** Takes orders from queue, prepares dishes
- **Chef:** Priority consumer for URGENT orders

---

## ENGINEER 3: Kitchen Equipment Management

### Responsibilities
- **KitchenManager** (Monitor class)
- **Equipment** (Entity or internal to manager)
- Deadlock prevention strategies

### Implementation Tasks

#### 1. Manager: KitchenManager.java
**Resources to manage:**
- 3 Ovens (ReentrantLock each)
- 2 Grills (ReentrantLock each)
- 1 Fryer (ReentrantLock)

**Critical methods:**
```java
public boolean acquireEquipment(List<String> needed, long timeout);
public void releaseEquipment(List<String> equipment);
```

**Deadlock Prevention:**
- **Strategy 1:** Always acquire locks in consistent order (alphabetical)
- **Strategy 2:** Use `tryLock(timeout)` with retry logic
- **Strategy 3:** Release all locks if can't acquire all needed resources

**Example Implementation:**
```java
public boolean acquireEquipment(List<String> needed, long timeout) {
    // Sort equipment names to ensure consistent order
    Collections.sort(needed);

    List<ReentrantLock> acquired = new ArrayList<>();

    try {
        for (String equipment : needed) {
            ReentrantLock lock = equipmentLocks.get(equipment);
            if (!lock.tryLock(timeout, TimeUnit.SECONDS)) {
                // Timeout - release all and return false
                releaseAcquiredLocks(acquired);
                return false;
            }
            acquired.add(lock);
        }
        return true; // All acquired
    } catch (InterruptedException e) {
        releaseAcquiredLocks(acquired);
        return false;
    }
}
```

**Testing:**
- Create deliberate deadlock scenario (DemoDeadlock.java)
- Verify prevention strategies work

---

## ENGINEER 4: Finance & Stock Management

### Responsibilities
- **FinanceManager** (Monitor class)
- **StockManager** (Monitor class)
- **Cashier** (Actor/Thread)
- **StockManagerThread** (Actor/Thread - background daemon)

### Implementation Tasks

#### 1. Manager: FinanceManager.java
**Demonstrates Race Condition:**
- Shared variable: `totalRevenue`
- Multiple cashiers updating simultaneously

**Critical methods:**
```java
public synchronized void processPayment(double amount);
public synchronized double getTotalRevenue();
public synchronized int getTotalCustomersServed();
```

**Testing:**
- Show WRONG implementation (without synchronized) losing money
- Show CORRECT implementation (with synchronized) maintaining accuracy

#### 2. Manager: StockManager.java
**Background Thread Pattern:**
- Maintains ingredient levels (Tomatoes, Cheese, Meat, Pasta)
- Dedicated background thread monitors stock
- Auto-replenishes when low

**Critical methods:**
```java
public synchronized boolean consumeIngredients(Map<String, Integer> needed);
public synchronized void replenish();
private synchronized boolean isStockLow();
```

**Flow:**
```java
// In Cook.prepareOrder():
if (!stockManager.consumeIngredients(dish.getIngredients())) {
    synchronized(stockManager) {
        stockManager.notify(); // Signal stock manager
        stockManager.wait();   // Wait for replenishment
    }
}

// StockManagerThread (runs continuously):
while (true) {
    synchronized(stockManager) {
        while (!stockManager.isStockLow()) {
            stockManager.wait(); // Sleep until notified
        }
        Thread.sleep(3000); // Simulate delivery time
        stockManager.replenish();
        stockManager.notifyAll(); // Wake waiting cooks
    }
}
```

#### 3. Actor: Cashier.java
- Process payments from clients
- Update FinanceManager

---

## CENTRAL COORDINATION: Restaurant.java (Mediator)

**All engineers contribute to this:**

```java
public class Restaurant {
    private static Restaurant instance;

    // Monitors (shared resources)
    private final TableManager tableManager;
    private final OrderQueue orderQueue;
    private final KitchenManager kitchenManager;
    private final FinanceManager financeManager;
    private final StockManager stockManager;

    private Restaurant() {
        this.tableManager = new TableManager();
        this.orderQueue = new OrderQueue();
        this.kitchenManager = new KitchenManager();
        this.financeManager = new FinanceManager();
        this.stockManager = new StockManager();
    }

    public static synchronized Restaurant getInstance() {
        if (instance == null) {
            instance = new Restaurant();
        }
        return instance;
    }

    // Getters for all managers
    public TableManager getTableManager() { return tableManager; }
    public OrderQueue getOrderQueue() { return orderQueue; }
    // ... etc
}
```

---

## DEVELOPMENT WORKFLOW

### Phase 1: Interfaces & Entities (Day 1)
- All engineers define their entity classes
- Define manager interfaces
- Agree on method signatures

### Phase 2: Independent Implementation (Days 2-3)
- Each engineer implements their monitor
- Each engineer implements their actors
- Use stubs for dependencies on other modules

### Phase 3: Integration (Day 4)
- Connect all monitors through Restaurant mediator
- Wire up actor interactions
- Replace stubs with real implementations

### Phase 4: Testing (Day 5)
- Test each module independently
- Test full system integration
- Verify concurrency correctness:
  - No deadlocks
  - No race conditions
  - Proper wait/notify behavior

---

## TESTING CHECKLIST

### Engineer 1 (Tables)
- [ ] VIP clients get priority
- [ ] VIP timeout works (fallback to normal queue)
- [ ] Clients properly wait when no tables
- [ ] notifyAll() wakes all waiting clients

### Engineer 2 (Orders)
- [ ] PriorityQueue maintains order by priority
- [ ] Cooks wait when queue empty
- [ ] notify() wakes exactly one cook
- [ ] Multiple servers don't corrupt queue

### Engineer 3 (Kitchen)
- [ ] Deadlock scenario can be demonstrated
- [ ] Prevention strategy eliminates deadlock
- [ ] tryLock timeout works correctly
- [ ] All locks released in finally blocks

### Engineer 4 (Finance & Stock)
- [ ] Race condition demonstrated (unsynchronized)
- [ ] Race condition fixed (synchronized)
- [ ] Stock replenishment triggers correctly
- [ ] Cooks wait during stock replenishment

---

## COMMUNICATION PROTOCOL

**Daily Standup Questions:**
1. What monitor/actor did you complete yesterday?
2. What are you implementing today?
3. Any blocking dependencies on other engineers?

**Integration Points to Coordinate:**
- Engineer 1 (Client) → Engineer 2 (Server order taking)
- Engineer 2 (Cook) → Engineer 3 (Equipment acquisition)
- Engineer 2 (Cook) → Engineer 4 (Stock consumption)
- Engineer 1 (Client) → Engineer 4 (Cashier payment)

**Shared Constants File:**
Create `ma.emsi.restaurant.Constants.java`:
```java
public class Constants {
    public static final int NUM_CLIENTS = 50;
    public static final int NUM_SERVERS = 4;
    public static final int NUM_COOKS = 3;
    public static final int NUM_CASHIERS = 2;
    public static final int NORMAL_TABLES = 10;
    public static final int VIP_TABLES = 5;
    public static final long VIP_TIMEOUT_SECONDS = 30;
    public static final long EQUIPMENT_TIMEOUT_SECONDS = 2;
}
```
