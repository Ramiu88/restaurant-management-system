# Restaurant Management System - Context for Gemini

## Project Overview
This project is a **Java-based Restaurant Management System simulation**. Its primary purpose is to demonstrate and implement advanced **concurrency concepts** such as multi-threading, synchronization, locks, and avoiding deadlocks.

The project is structured as a **Maven** project.

**Current Status:**
The project is currently in the **initial skeleton phase**. 
- A detailed design and implementation plan exists in `restaurant_system_plan.md`.
- The source code directory (`src/main/java`) currently only contains a skeletal `Main.java`.
- The goal is to implement the system according to the plan.

## Architecture & Design (Planned)
The system is designed around 4 core modules to simulate a real restaurant environment with high concurrency (approx. 61 threads):

1.  **Module 1: Table Management** (Tables, Customers waiting)
    *   Concepts: `wait()`, `notifyAll()`, `synchronized`, `ReentrantLock`, `tryLock`.
2.  **Module 2: Order Queue** (Producers/Consumers pattern)
    *   Concepts: `PriorityQueue`, `wait()/notify()`.
3.  **Module 3: Kitchen Equipment** (Resource sharing)
    *   Concepts: `ReentrantLock`, Deadlock prevention with `tryLock`.
4.  **Module 4: Cashier & Stock**
    *   Concepts: Race conditions (`synchronized`), dedicated background threads.

**Key Actors (Threads):**
- Clients (50)
- Servers (4)
- Cooks (3)
- Chef (1)
- Cashiers (2)
- Stock Manager (1)

## Building and Running

### Prerequisites
- Java Development Kit (JDK)
- Maven

### Build Commands
```bash
# Clean and install dependencies
mvn clean install

# Compile only
mvn compile
```

### Run Command
*Note: The Main class is currently empty.*
```bash
# Run the application (once implemented)
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Main"
```

## Development Conventions

*   **Language:** Java.
*   **Documentation/Comments:** French (as seen in `restaurant_system_plan.md`).
*   **Package Structure:** `ma.emsi.restaurant`.
*   **Folder Structure (Target):**
    *   `models/` (Data classes: Commande, Table, Plat)
    *   `modules/` (Business logic per module)
    *   `threads/` (Runnable/Thread classes for actors)
    *   `utils/` (Helpers, Statistics, Dashboard)
*   **Concurrency:** Use standard Java `java.util.concurrent` package (Locks, Atomic variables) and intrinsic locks (`synchronized`, `wait`/`notify`) as specified in the plan.
