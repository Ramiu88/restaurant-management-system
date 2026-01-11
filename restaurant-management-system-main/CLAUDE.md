# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Java-based Restaurant Management System simulation** designed to demonstrate advanced concurrency concepts including multi-threading, synchronization, locks, and deadlock prevention. The system simulates a restaurant with ~61 concurrent threads representing clients, servers, cooks, and other actors.

**Current Status:** Early skeleton phase. Detailed design plan exists in `restaurant_system_plan.md`.

## Build and Run Commands

```bash
# Clean and install dependencies
mvn clean install

# Compile only
mvn compile

# Run the application
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Main"

# Run tests (when implemented)
mvn test

# Run a specific test
mvn test -Dtest=ClassName#methodName
```

## Architecture

The system is organized around 4 core modules simulating restaurant operations:

### Module 1: Table Management
- **Package:** `ma.emsi.restaurant.modules.Module1_Tables`
- **Key classes:** `GestionnaireTables`, `FileAttenteClients`
- **Concurrency:** `wait()`, `notifyAll()`, `synchronized`, `ReentrantLock`, `tryLock()`
- **Handles:** 50 client threads competing for 15 tables (10 normal, 5 VIP)
- **VIP logic:** Uses `tryLock(30s)` on VIP tables, falls back to normal queue on timeout

### Module 2: Order Queue (Producer-Consumer)
- **Package:** `ma.emsi.restaurant.modules.Module2_Commandes`
- **Key classes:** `FileCommandes`
- **Concurrency:** `PriorityQueue`, `wait()/notify()`, `synchronized`
- **Handles:** 4 servers (producers) and 3 cooks + 1 chef (consumers)
- **Priority levels:** URGENTE (1), NORMALE (2), LENTE (3)

### Module 3: Kitchen Equipment (Resource Sharing)
- **Package:** `ma.emsi.restaurant.modules.Module3_Equipements`
- **Key classes:** `GestionnaireEquipements`, `DemoDeadlock`
- **Concurrency:** `ReentrantLock`, `tryLock(timeout)` for deadlock prevention
- **Resources:** 3 ovens, 2 grills, 1 fryer (each has own ReentrantLock)
- **Critical:** Demonstrates circular deadlock scenario and resolution strategies

### Module 4: Cashier & Stock
- **Package:** `ma.emsi.restaurant.modules.Module4_CaisseStock`
- **Key classes:** `Caisse`, `GestionnaireStock`
- **Concurrency:** `synchronized` for race conditions, dedicated background thread
- **Handles:** 2 cashiers updating shared revenue counter, 1 stock manager thread

## Package Structure

```
ma.emsi.restaurant/
├── models/          # Data models: Commande, Table, Plat
├── modules/         # Business logic organized by 4 modules above
├── threads/         # Runnable/Thread classes: ClientThread, ServeurThread, etc.
└── utils/           # Helpers: Statistiques, Dashboard (real-time display)
```

## Concurrency Patterns Used

- **`synchronized` blocks:** Protecting shared counters (tables, revenue), PriorityQueue access
- **`wait()/notify()/notifyAll()`:** Client waiting for tables, cooks waiting for orders, stock notifications
- **`ReentrantLock`:** VIP table reservations, kitchen equipment locks
- **`tryLock(timeout)`:** VIP table timeout fallback, deadlock prevention on equipment
- **Dedicated threads:** Stock manager runs continuously in background
- **Race condition demo:** Cashier revenue counter (with/without synchronization)

## Development Notes

- **Language:** Java (standard library concurrency: `java.util.concurrent`)
- **Documentation:** French (as seen in plan and comments)
- **Base package:** `ma.emsi.restaurant`
- **Thread count:** ~61 concurrent threads (50 clients + 11 staff)
- **Deadlock handling:** Module 3 intentionally demonstrates deadlock scenarios and fixes

## Key Implementation Details

**Table assignment flow:**
1. VIP clients attempt `tryLock(30s)` on VIP tables
2. On timeout or if normal client, enter synchronized queue
3. Use `wait()` if no tables available
4. `notifyAll()` when table freed

**Order processing flow:**
1. Servers add to synchronized PriorityQueue with `notify()`
2. Cooks/chef `wait()` when queue empty
3. Cook checks stock before preparing (may trigger stock manager)
4. Equipment acquisition uses `tryLock(2s)` with retry logic

**Stock management:**
1. Background thread continuously monitors stock levels
2. Cooks `notify()` stock manager when ingredient low
3. Stock manager `notifyAll()` cooks after replenishment (simulated 3s delay)

**Race condition prevention:**
- Cashier revenue updates must be synchronized
- All PriorityQueue operations must be synchronized
- Stock consumption/checks must be atomic
