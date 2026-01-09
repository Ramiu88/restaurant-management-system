# WALID - Tables Management Module

## Overview
You are responsible for table management and client waiting queue. You must implement the reservation system with VIP priority.

## Classes to Implement

### 1. entities/Table.java
**Description:** Represents a restaurant table (VIP or normal)

**Attributes:**
- `id` (int) - Unique table number
- `isVIP` (boolean) - Indicates if it's a VIP table
- `occupied` (volatile boolean) - Occupation state
- `vipLock` (ReentrantLock) - Lock for VIP tables only

**Status:** Skeleton created - needs completion

### 2. managers/TableManager.java
**Description:** Monitor that manages table allocation and waiting queue

**Attributes:**
- `tables` (Table[15]) - 10 normal + 5 VIP
- `availableNormalTables` (int) - Counter of free normal tables
- Synchronization object for waiting queue

**Critical Methods:**
```java
public synchronized Table requestTable(boolean isVIP)
public synchronized void releaseTable(Table table)
```

**Synchronization Requirements:**
- Use `wait()` when a client waits for a table
- Use `notifyAll()` when a table is freed
- Use `tryLock(30 seconds)` for VIP with fallback to normal queue

### 3. actors/Client.java
**Description:** Thread representing a customer

**Lifecycle:**
1. Request a table (VIP or normal depending on type)
2. Wait if no table available
3. Sit and browse menu (sleep 1-2s)
4. Call server to order
5. Wait and eat (sleep 3-5s)
6. Pay at cashier
7. Release table

**Synchronization Points:**
- Waits in `requestTable()` if no tables
- Woken by `notifyAll()` when table freed
- Interaction with OrderQueue (Cranky) to place order
- Interaction with FinanceManager (Saladin) to pay

## Detailed Algorithm - requestTable()

```java
public synchronized Table requestTable(boolean isVIP) throws InterruptedException {
    if (isVIP) {
        // 1. Try to acquire a VIP table for 30s
        for (Table table : vipTables) {
            if (!table.isOccupied() &&
                table.getVipLock().tryLock(30, TimeUnit.SECONDS)) {
                table.setOccupied(true);
                return table;
            }
        }
        // 2. Timeout - fallback to normal queue
        System.out.println("Client " + Thread.currentThread().getName() +
                          " VIP timeout, joining normal queue");
    }

    // 3. Normal queue
    while (availableNormalTables == 0) {
        wait(); // Wait for release
    }

    // 4. Assign a normal table
    for (Table table : normalTables) {
        if (!table.isOccupied()) {
            table.setOccupied(true);
            availableNormalTables--;
            return table;
        }
    }

    return null; // Should never reach here
}
```

## Detailed Algorithm - releaseTable()

```java
public synchronized void releaseTable(Table table) {
    if (table.isVIP()) {
        table.getVipLock().unlock();
    } else {
        availableNormalTables++;
    }
    table.setOccupied(false);
    notifyAll(); // Wake ALL waiting clients

    System.out.println("Table " + table.getId() + " released");
}
```

## Tests to Implement

1. **Test VIP Priority:** Verify VIP clients get VIP tables
2. **Test VIP Timeout:** Verify fallback after 30s
3. **Test Wait/Notify:** Verify clients wait correctly
4. **Test NotifyAll:** Verify all waiting clients are woken

## Files to Create

```
src/main/java/ma/emsi/restaurant/
├── entities/Table.java              [STARTED]
├── managers/TableManager.java       [TODO]
└── actors/Client.java               [TODO]
```

## Implementation Order

1. **First:** `Table.java` (simple POJO) - 30 minutes
2. **Then:** `TableManager.java` (critical logic) - 3 hours
3. **Finally:** `Client.java` (uses TableManager) - 2 hours

## Integration Points with Other Modules

- **Client calls OrderQueue** (Cranky): `Restaurant.getInstance().getOrderQueue().addOrder(...)`
- **Client calls FinanceManager** (Saladin): `Restaurant.getInstance().getFinanceManager().processPayment(...)`

Use temporary stubs for these calls until integration.

## Example Stub Usage

```java
// In Client.java, until Cranky's module is ready:
public void placeOrder() {
    // TODO: Replace with actual OrderQueue call
    System.out.println("Client " + id + " placing order (STUB)");
    try {
        Thread.sleep(100); // Simulate order time
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

## Checklist

- [ ] Table.java completed and tested
- [ ] TableManager.java implemented
- [ ] VIP lock mechanism working
- [ ] VIP timeout and fallback working
- [ ] wait/notifyAll mechanism working
- [ ] Client.java lifecycle implemented
- [ ] Integration stubs created
- [ ] Unit tests written
- [ ] Integration with Restaurant.java completed
