# Restaurant Management System

A Java-based restaurant simulation demonstrating advanced concurrency concepts including multi-threading, synchronization, locks, and deadlock prevention.

## Overview

The system simulates a restaurant with ~61 concurrent threads representing clients, servers, cooks, and staff. It demonstrates producer-consumer patterns, bounded buffers, priority queues, and deadlock prevention strategies.

## Concurrency Concepts Demonstrated

| Concept | Implementation |
|---------|---------------|
| Producer-Consumer | `OrderQueue` with `wait()/notify()` |
| Bounded Buffer | Queue blocks when full, notifies when space available |
| ReentrantLock | VIP table reservations with `tryLock(timeout)` |
| Deadlock Prevention | Kitchen equipment uses `tryLock()` with fallback |
| Synchronized | Race condition prevention in `FinanceManager` |
| PriorityQueue | Orders sorted by priority (1=URGENT, 2=NORMAL, 3=SLOW) |
| FIFO Ordering | Same priority sorted by timestamp |
| Thread Safety | All shared state properly synchronized |

## Architecture

```
src/main/java/ma/emsir/restaurant/
├── managers/
│   ├── FinanceManager.java    - Thread-safe payment processing
│   ├── StockManager.java       - Inventory management with wait/notify
│   ├── OrderQueue.java         - Priority-based bounded queue
│   ├── TableManager.java       - VIP priority with ReentrantLock
│   └── KitchenManager.java     - Equipment locks, deadlock prevention
├── entities/
│   ├── Order.java              - Comparable priority order
│   ├── Dish.java               - Menu items with equipment/ingredients
│   └── Table.java              - Table entity with lock
├── actors/
│   ├── Client.java             - Table acquisition, ordering, payment
│   ├── Server.java             - Producer: adds orders to queue
│   ├── Cook.java               - Consumer: takes and prepares orders
│   ├── Cashier.java            - Payment processing
│   └── StockManagerThread.java - Background stock replenishment
└── Demo files
    ├── Main.java               - Full restaurant simulation
    ├── Demo.java               - Concept-by-concept proof
    ├── TeamDemo.java           - Per-contributor showcase
    └── EdgeCaseDemo.java       - Boundary condition testing
```

## Running the Project

```bash
# Build and install
mvn clean install

# Run main simulation
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Main"

# Run concept demo
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Demo"

# Run team demo
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.TeamDemo"

# Run edge case tests
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.EdgeCaseDemo"
```

## Test Coverage

115 JUnit tests covering all modules:

| Module | Tests | Coverage |
|--------|-------|----------|
| FinanceManager | 14 | Payments, concurrency, race conditions |
| OrderQueue | 10 | Priority ordering, bounded buffer, blocking |
| StockManager | 19 | Consumption, replenishment, thread safety |
| KitchenManager | 15 | Equipment acquisition, deadlock prevention |
| Order/Dish | 42 | Entity validation, comparison, equals/hashCode |
| Cook/Server | 14 | End-to-end integration |

Run tests:
```bash
mvn test
```

## Team Contributions

| Member | Module | Key Contributions |
|--------|--------|------------------|
| Saladin | Finance & Stock | `FinanceManager`, `StockManager`, JUnit tests |
| Walid | Table Management | VIP priority with `ReentrantLock` and timeout |
| Anakin | Order Queue | `OrderQueue`, entities, `Cook`, `Server` |
| Marwan | Kitchen Equipment | `KitchenManager`, deadlock prevention |

## Requirements

- Java 11+
- Maven 3.6+
