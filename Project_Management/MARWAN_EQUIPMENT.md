# MARWAN - Kitchen Equipment Management Module

## Overview
You are responsible for managing kitchen equipment (ovens, grills, fryer) and implementing deadlock prevention strategies. This is the most challenging module as it demonstrates real deadlock scenarios and their solutions.

## Classes to Implement

### 1. managers/KitchenManager.java
**Description:** Monitor that manages access to shared kitchen equipment

**Equipment Resources:**
- 3 Ovens (Oven1, Oven2, Oven3)
- 2 Grills (Grill1, Grill2)
- 1 Fryer

**Each equipment has its own ReentrantLock**

**Critical Methods:**
```java
public boolean acquireEquipment(List<String> equipmentNeeded, long timeoutSeconds)
public void releaseEquipment(List<String> equipmentNames)
public void demonstrateDeadlock()  // For testing purposes
```

## The Deadlock Problem

### Circular Deadlock Scenario

```text
Cook1: Making PIZZA
    1. Lock(Oven1) ✓
    2. Waiting for Lock(Fryer) ⏳ [held by Cook2]

Cook2: Making STEAK-FRIES
    1. Lock(Fryer) ✓
    2. Waiting for Lock(Grill1) ⏳ [held by Cook3]

Cook3: Making GRILLED-OVEN-DISH
    1. Lock(Grill1) ✓
    2. Waiting for Lock(Oven1) ⏳ [held by Cook1]

→ CIRCULAR DEADLOCK! Nobody can proceed!
```

## Deadlock Prevention Strategies

### Strategy 1: Consistent Lock Ordering (Recommended)

Always acquire locks in the same order (alphabetical):

```java
public boolean acquireEquipment(List<String> equipmentNeeded, long timeoutSeconds) {
    // Sort to ensure consistent order
    Collections.sort(equipmentNeeded);

    List<ReentrantLock> acquiredLocks = new ArrayList<>();

    try {
        for (String equipment : equipmentNeeded) {
            ReentrantLock lock = equipmentLocks.get(equipment);

            if (!lock.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                // Timeout - release all acquired locks
                releaseAcquiredLocks(acquiredLocks);
                return false; // Failed to acquire all
            }

            acquiredLocks.add(lock);
        }

        // All locks acquired successfully
        return true;

    } catch (InterruptedException e) {
        releaseAcquiredLocks(acquiredLocks);
        Thread.currentThread().interrupt();
        return false;
    }
}

private void releaseAcquiredLocks(List<ReentrantLock> locks) {
    for (ReentrantLock lock : locks) {
        lock.unlock();
    }
}
```

### Strategy 2: tryLock with Timeout and Retry

```java
public boolean acquireWithRetry(List<String> equipmentNeeded, int maxRetries) {
    for (int attempt = 0; attempt < maxRetries; attempt++) {
        if (acquireEquipment(equipmentNeeded, 2)) {
            return true; // Success
        }
        // Failed - small delay before retry
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    return false; // All retries exhausted
}
```

## Release Method

```java
public void releaseEquipment(List<String> equipmentNames) {
    for (String equipment : equipmentNames) {
        ReentrantLock lock = equipmentLocks.get(equipment);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() +
                              " released " + equipment);
        }
    }
}
```

## Deadlock Demonstration Class

Create a separate class to demonstrate the deadlock:

### DemoDeadlock.java

```java
package ma.emsi.restaurant.managers;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates a deliberate deadlock scenario for educational purposes
 * DO NOT USE IN PRODUCTION
 */
public class DemoDeadlock {
    private final ReentrantLock oven = new ReentrantLock();
    private final ReentrantLock fryer = new ReentrantLock();
    private final ReentrantLock grill = new ReentrantLock();

    public void demonstrateCircularDeadlock() {
        // Cook 1: Oven -> Fryer
        Thread cook1 = new Thread(() -> {
            oven.lock();
            System.out.println("Cook1: Locked Oven, waiting for Fryer...");
            sleep(100);
            fryer.lock(); // Will deadlock
            System.out.println("Cook1: Got both locks!");
            fryer.unlock();
            oven.unlock();
        }, "Cook1");

        // Cook 2: Fryer -> Grill
        Thread cook2 = new Thread(() -> {
            fryer.lock();
            System.out.println("Cook2: Locked Fryer, waiting for Grill...");
            sleep(100);
            grill.lock(); // Will deadlock
            System.out.println("Cook2: Got both locks!");
            grill.unlock();
            fryer.unlock();
        }, "Cook2");

        // Cook 3: Grill -> Oven
        Thread cook3 = new Thread(() -> {
            grill.lock();
            System.out.println("Cook3: Locked Grill, waiting for Oven...");
            sleep(100);
            oven.lock(); // Will deadlock
            System.out.println("Cook3: Got both locks!");
            oven.unlock();
            grill.unlock();
        }, "Cook3");

        cook1.start();
        cook2.start();
        cook3.start();

        // Wait a bit then show the deadlock
        sleep(2000);
        System.out.println("\n=== DEADLOCK DETECTED ===");
        System.out.println("All threads are blocked waiting for each other!");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

## Equipment Requirements per Dish

Define which equipment each dish needs:

```java
public class DishRecipes {
    public static List<String> getRequiredEquipment(String dishName) {
        switch (dishName) {
            case "PIZZA":
                return Arrays.asList("Oven1", "Fryer");
            case "STEAK":
                return Arrays.asList("Grill1");
            case "PASTA":
                return Arrays.asList("Oven2");
            case "FRIES":
                return Arrays.asList("Fryer");
            case "GRILLED_FISH":
                return Arrays.asList("Grill2", "Oven3");
            default:
                return Arrays.asList("Oven1");
        }
    }
}
```

## Usage Example (by Cook)

```java
// In Cook.java (Cranky will use your KitchenManager)
KitchenManager kitchen = Restaurant.getInstance().getKitchenManager();
List<String> needed = DishRecipes.getRequiredEquipment(order.getDish());

if (kitchen.acquireEquipment(needed, 2)) {
    try {
        // Cook the dish
        System.out.println("Cooking " + order.getDish());
        Thread.sleep(preparationTime);
    } finally {
        kitchen.releaseEquipment(needed);
    }
} else {
    System.out.println("Could not acquire equipment, retrying...");
}
```

## Tests to Implement

1. **Test Deadlock Demo:** Run DemoDeadlock and verify it deadlocks
2. **Test Prevention:** Verify sorted acquisition prevents deadlock
3. **Test Timeout:** Verify tryLock timeout works correctly
4. **Test Release:** Verify all locks released in finally block
5. **Test Multiple Cooks:** Run 3+ cooks concurrently without deadlock

## Files to Create

```
src/main/java/ma/emsi/restaurant/
├── managers/
│   ├── KitchenManager.java    [TODO]
│   └── DemoDeadlock.java      [TODO]
└── utils/
    └── DishRecipes.java       [TODO]
```

## Implementation Order

1. **First:** `KitchenManager.java` basic structure (1 hour)
2. **Then:** `DemoDeadlock.java` to understand the problem (1 hour)
3. **Then:** Implement `acquireEquipment()` with sorting (2 hours)
4. **Then:** Implement `releaseEquipment()` (30 minutes)
5. **Finally:** `DishRecipes.java` helper (30 minutes)

## Integration Points

- **Used by Cook** (Cranky): Cook calls `acquireEquipment()` before cooking
- **Always use finally:** Release must happen in finally block

## Critical Rules

1. **ALWAYS release in finally block** - Even if exception occurs
2. **Sort equipment names** - Ensures consistent lock order
3. **Check isHeldByCurrentThread()** - Before unlocking
4. **Use tryLock, not lock** - Prevents indefinite blocking

## Checklist

- [ ] KitchenManager.java structure created
- [ ] equipmentLocks Map initialized
- [ ] acquireEquipment() with sorting implemented
- [ ] releaseEquipment() with safety checks
- [ ] DemoDeadlock.java created and tested
- [ ] DishRecipes.java created
- [ ] Deadlock scenario verified
- [ ] Prevention strategy verified
- [ ] Integration with Restaurant.java completed
- [ ] Documentation for Cook usage written
