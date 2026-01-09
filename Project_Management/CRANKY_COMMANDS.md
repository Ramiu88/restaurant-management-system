# CRANKY - Order Queue Management Module

## Overview
You are responsible for the order queue system using the Producer-Consumer pattern. Servers are producers, Cooks/Chef are consumers. You must implement priority-based order processing.

## Classes to Implement

### 1. entities/Order.java
**Description:** Represents a customer order with priority

**Attributes:**
- `orderId` (int) - Unique order identifier
- `clientId` (int) - Which client made the order
- `dish` (Dish) - What dish was ordered
- `priority` (int) - 1=URGENT, 2=NORMAL, 3=SLOW
- `timestamp` (long) - When order was placed

**Must implement Comparable<Order>:**
```java
@Override
public int compareTo(Order other) {
    // First by priority (1 < 2 < 3)
    int priorityCompare = Integer.compare(this.priority, other.priority);
    if (priorityCompare != 0) return priorityCompare;

    // If same priority, by timestamp (FIFO)
    return Long.compare(this.timestamp, other.timestamp);
}
```

### 2. entities/Dish.java
**Description:** Represents a dish that can be ordered

**Attributes:**
- `name` (String) - Dish name
- `preparationTime` (int) - Time to cook in milliseconds
- `requiredEquipment` (List<String>) - Equipment needed from KitchenManager
- `ingredients` (Map<String, Integer>) - Ingredients from StockManager

**Example dishes:**
```java
public static final Dish DESSERT = new Dish(
    "Ice Cream",
    Constants.PREP_TIME_URGENT,
    Arrays.asList(), // No equipment needed
    Map.of("Milk", 1, "Sugar", 1)
);

public static final Dish STEAK = new Dish(
    "Grilled Steak",
    Constants.PREP_TIME_NORMAL,
    Arrays.asList("Grill1"),
    Map.of("Meat", 1)
);

public static final Dish PIZZA = new Dish(
    "Pizza Margherita",
    Constants.PREP_TIME_SLOW,
    Arrays.asList("Oven1"),
    Map.of("Dough", 1, "Cheese", 2, "Tomato", 2)
);
```

### 3. managers/OrderQueue.java
**Description:** Monitor managing the priority queue (Producer-Consumer pattern)

**Attributes:**
- `queue` (PriorityQueue<Order>) - Orders sorted by priority
- Lock object for synchronization

**Critical Methods:**
```java
public synchronized void addOrder(Order order)
public synchronized Order takeOrder() throws InterruptedException
public synchronized boolean isEmpty()
public synchronized int size()
```

## Detailed Implementation - OrderQueue.java

```java
package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Order;
import java.util.PriorityQueue;

public class OrderQueue {
    private final PriorityQueue<Order> queue;
    private int orderIdCounter = 1;

    public OrderQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Producer method: Server adds order
     * Wakes ONE cook with notify()
     */
    public synchronized void addOrder(Order order) {
        queue.add(order);
        System.out.println("[OrderQueue] Order #" + order.getOrderId() +
                          " added with priority " + order.getPriority() +
                          " (Queue size: " + queue.size() + ")");

        notify(); // Wake ONE cook (not all)
    }

    /**
     * Consumer method: Cook/Chef takes order
     * Waits if queue is empty
     */
    public synchronized Order takeOrder() throws InterruptedException {
        while (queue.isEmpty()) {
            System.out.println("[" + Thread.currentThread().getName() +
                             "] Waiting for orders...");
            wait(); // Sleep until notified
        }

        Order order = queue.poll();
        System.out.println("[" + Thread.currentThread().getName() +
                          "] Took Order #" + order.getOrderId());

        return order;
    }

    /**
     * Helper method to check if queue is empty
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Get current queue size
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Generate unique order ID
     */
    public synchronized int generateOrderId() {
        return orderIdCounter++;
    }
}
```

## Producer: Server Thread

### 4. actors/Server.java
**Description:** Thread that takes orders from clients and adds to queue

**Lifecycle:**
1. Wait for client to be ready to order
2. Take order from client
3. Add to OrderQueue
4. Repeat

```java
package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.entities.Dish;
import ma.emsi.restaurant.entities.Order;

public class Server implements Runnable {
    private final int serverId;

    public Server(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public void run() {
        OrderQueue orderQueue = Restaurant.getInstance().getOrderQueue();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for a client to call this server (simplified)
                Thread.sleep(1000 + (int)(Math.random() * 2000));

                // Randomly create an order (in real system, client provides this)
                Dish dish = getRandomDish();
                int priority = dish.getPreparationTime() <= 1000 ? 1 : 2;

                Order order = new Order(
                    orderQueue.generateOrderId(),
                    -1, // Client ID not important for now
                    dish,
                    priority,
                    System.currentTimeMillis()
                );

                orderQueue.addOrder(order);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Dish getRandomDish() {
        // Return random dish (simplified)
        // In real system, client chooses
        return Dish.STEAK; // Placeholder
    }
}
```

## Consumer: Cook Thread

### 5. actors/Cook.java
**Description:** Thread that takes orders and prepares dishes

**Lifecycle:**
1. Take order from OrderQueue (blocks if empty)
2. Check stock availability (Saladin's StockManager)
3. Acquire equipment (Marwan's KitchenManager)
4. Cook dish (sleep for preparationTime)
5. Release equipment
6. Repeat

```java
package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.entities.Order;

public class Cook implements Runnable {
    private final int cookId;

    public Cook(int cookId) {
        this.cookId = cookId;
    }

    @Override
    public void run() {
        OrderQueue orderQueue = Restaurant.getInstance().getOrderQueue();
        KitchenManager kitchen = Restaurant.getInstance().getKitchenManager();
        StockManager stock = Restaurant.getInstance().getStockManager();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 1. Take order (blocks if queue empty)
                Order order = orderQueue.takeOrder();

                // 2. Check stock (Saladin's module)
                if (!stock.consumeIngredients(order.getDish().getIngredients())) {
                    System.out.println("Cook " + cookId + " waiting for stock...");
                    // Wait for stock replenishment
                    continue;
                }

                // 3. Acquire equipment (Marwan's module)
                List<String> equipment = order.getDish().getRequiredEquipment();
                if (!kitchen.acquireEquipment(equipment, 2)) {
                    System.out.println("Cook " + cookId +
                                     " couldn't acquire equipment, retrying...");
                    continue;
                }

                // 4. Cook the dish
                try {
                    System.out.println("Cook " + cookId +
                                     " preparing " + order.getDish().getName());
                    Thread.sleep(order.getDish().getPreparationTime());
                    System.out.println("Cook " + cookId +
                                     " finished Order #" + order.getOrderId());
                } finally {
                    // 5. Always release equipment
                    kitchen.releaseEquipment(equipment);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

### 6. actors/Chef.java (Optional - Priority Consumer)

Similar to Cook but could have logic to prefer URGENT orders.

## Tests to Implement

1. **Test Priority Queue:** Verify urgent orders processed first
2. **Test Producer-Consumer:** Multiple servers, multiple cooks
3. **Test Wait/Notify:** Cooks wait when queue empty
4. **Test Notify (not NotifyAll):** Only ONE cook wakes per order
5. **Test FIFO within Priority:** Same priority orders processed in order

## Files to Create

```
src/main/java/ma/emsi/restaurant/
├── entities/
│   ├── Order.java      [TODO]
│   └── Dish.java       [TODO]
├── managers/
│   └── OrderQueue.java [TODO]
└── actors/
    ├── Server.java     [TODO]
    ├── Cook.java       [TODO]
    └── Chef.java       [OPTIONAL]
```

## Implementation Order

1. **First:** `Dish.java` (simple POJO) - 30 minutes
2. **Then:** `Order.java` with Comparable - 1 hour
3. **Then:** `OrderQueue.java` (critical logic) - 2 hours
4. **Then:** `Server.java` - 1 hour
5. **Finally:** `Cook.java` (uses other modules) - 2 hours

## Integration Points

- **Called by Client** (Walid): Client tells Server to place order
- **Calls KitchenManager** (Marwan): Cook acquires equipment
- **Calls StockManager** (Saladin): Cook checks ingredient availability

## notify() vs notifyAll()

**IMPORTANT:** Use `notify()` not `notifyAll()` in addOrder()!

**Why?**
- Each order needs exactly ONE cook
- `notifyAll()` would wake ALL cooks unnecessarily
- `notify()` wakes ONE cook = more efficient

**Exception:** Use `notifyAll()` only if multiple conditions (not the case here)

## Priority Levels

| Priority | Value | Examples | Prep Time |
|----------|-------|----------|-----------|
| URGENT | 1 | Desserts, Drinks | 500ms |
| NORMAL | 2 | Main dishes | 3000ms |
| SLOW | 3 | Complex dishes | 5000ms |

## Checklist

- [ ] Dish.java created with sample dishes
- [ ] Order.java with Comparable implemented
- [ ] compareTo() considers priority then timestamp
- [ ] OrderQueue.java with PriorityQueue
- [ ] addOrder() uses notify() (not notifyAll)
- [ ] takeOrder() uses wait() when empty
- [ ] Server.java implemented
- [ ] Cook.java implemented with full lifecycle
- [ ] Integration stubs for Kitchen and Stock
- [ ] Unit tests for priority ordering
- [ ] Integration with Restaurant.java completed
